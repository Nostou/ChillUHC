package fr.Nosta.ChillUHC.Utils;

import fr.Nosta.ChillUHC.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class NmsSkinApplier {

    private final Main plugin;

    public NmsSkinApplier(Main plugin) {
        this.plugin = plugin;
    }

    public boolean applySkin(Player player, String textureValue, String textureSignature) {
        if (textureValue == null || textureValue.isBlank()) {
            return false;
        }

        try {
            Object handle = getHandle(player);
            replaceGameProfile(handle, textureValue, textureSignature);

            refreshRemoteViewers(player, handle);
            refreshSelf(player, handle);
            return true;
        } catch (ReflectiveOperationException exception) {
            Throwable cause = exception instanceof InvocationTargetException invocationTargetException
                    ? invocationTargetException.getCause()
                    : exception.getCause();

            String details = exception.getClass().getSimpleName();
            if (cause != null) {
                details += " -> " + cause.getClass().getSimpleName();
                if (cause.getMessage() != null) {
                    details += ": " + cause.getMessage();
                }
            } else if (exception.getMessage() != null) {
                details += ": " + exception.getMessage();
            }

            plugin.getLogger().log(Level.WARNING, "Unable to apply anonymous skin through NMS for " + player.getName() + " (" + details + ")", exception);
            return false;
        }
    }

    private void refreshRemoteViewers(Player target, Object targetHandle) throws ReflectiveOperationException {
        Object removeInfoPacket = createPlayerInfoRemovePacket(target.getUniqueId());
        Object addInfoPacket = createPlayerInfoInitializePacket(targetHandle);
        Object trackedEntity = getTrackedEntity(targetHandle);
        Object serverEntity = getField(trackedEntity, "serverEntity");
        Class<?> serverPlayerClass = classForName("net.minecraft.server.level.ServerPlayer");

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) {
                continue;
            }

            Object viewerHandle = getHandle(viewer);
            sendPacket(viewer, removeInfoPacket);
            invoke(serverEntity, "removePairing", serverPlayerClass.cast(viewerHandle));
            sendPacket(viewer, addInfoPacket);
            invoke(serverEntity, "addPairing", serverPlayerClass.cast(viewerHandle));
        }
    }

    private void refreshSelf(Player player, Object handle) throws ReflectiveOperationException {
        Object level = invoke(handle, "level");
        Object connection = getField(handle, "connection");
        Object playerList = getPlayerList();

        sendPacket(player, createPlayerInfoRemovePacket(player.getUniqueId()));
        sendPacket(player, createPlayerInfoInitializePacket(handle));
        sendPacket(player, createRespawnPacket(handle, level));

        invoke(playerList, "sendLevelInfo", handle, level);
        invoke(connection, "teleport", player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
        invoke(handle, "onUpdateAbilities");
        invoke(playerList, "sendActivePlayerEffects", handle);
        invoke(playerList, "sendPlayerPermissionLevel", handle);
        invoke(playerList, "sendAllPlayerInfo", handle);

        sendPacket(player, createHeldSlotPacket(player.getInventory().getHeldItemSlot()));
        sendPacket(player, createExperiencePacket(player.getExp(), player.getTotalExperience(), player.getLevel()));
        sendPacket(player, createHealthPacket((float) player.getHealth(), player.getFoodLevel(), player.getSaturation()));

        player.updateInventory();
        teleportSelf(player);
    }

    private void teleportSelf(Player player) {
        Location location = player.getLocation().clone();
        Bukkit.getScheduler().runTask(plugin, () -> player.teleport(location));
    }

    private Object getTrackedEntity(Object handle) throws ReflectiveOperationException {
        Object level = invoke(handle, "level");
        Object chunkSource = invoke(level, "getChunkSource");
        Object chunkMap = getField(chunkSource, "chunkMap");
        Object entityMap = getField(chunkMap, "entityMap");
        int entityId = (int) invoke(handle, "getId");
        return invoke(entityMap, "get", entityId);
    }

    private Object getPlayerList() throws ReflectiveOperationException {
        Object minecraftServer = invoke(Bukkit.getServer(), "getServer");
        return invoke(minecraftServer, "getPlayerList");
    }

    private void replaceGameProfile(Object handle, String textureValue, String textureSignature) throws ReflectiveOperationException {
        Object currentProfile = invoke(handle, "getGameProfile");
        Object updatedProfile = createGameProfile(currentProfile, textureValue, textureSignature);
        setField(handle, "gameProfile", updatedProfile);
    }

    private Object createGameProfile(Object currentProfile, String textureValue, String textureSignature) throws ReflectiveOperationException {
        Class<?> gameProfileClass = classForName("com.mojang.authlib.GameProfile");
        Constructor<?> constructor = gameProfileClass.getConstructor(UUID.class, String.class, classForName("com.mojang.authlib.properties.PropertyMap"));
        UUID uniqueId = (UUID) invokeAny(currentProfile, "id", "getId");
        String name = (String) invokeAny(currentProfile, "name", "getName");
        return constructor.newInstance(uniqueId, name, createPropertyMap(textureValue, textureSignature));
    }

    private Object createPropertyMap(String textureValue, String textureSignature) throws ReflectiveOperationException {
        Class<?> linkedHashMultimapClass = classForName("com.google.common.collect.LinkedHashMultimap");
        Method createMethod = linkedHashMultimapClass.getMethod("create");
        Object multimap = createMethod.invoke(null);
        invoke(multimap, "put", "textures", createTextureProperty(textureValue, textureSignature));

        Class<?> propertyMapClass = classForName("com.mojang.authlib.properties.PropertyMap");
        Constructor<?> constructor = propertyMapClass.getConstructor(classForName("com.google.common.collect.Multimap"));
        return constructor.newInstance(multimap);
    }

    private Object createTextureProperty(String value, String signature) throws ReflectiveOperationException {
        Class<?> propertyClass = classForName("com.mojang.authlib.properties.Property");

        if (signature == null || signature.isBlank()) {
            Constructor<?> constructor = propertyClass.getConstructor(String.class, String.class);
            return constructor.newInstance("textures", value);
        }

        Constructor<?> constructor = propertyClass.getConstructor(String.class, String.class, String.class);
        return constructor.newInstance("textures", value, signature);
    }

    private Object createPlayerInfoRemovePacket(UUID uniqueId) throws ReflectiveOperationException {
        Class<?> packetClass = classForName("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket");
        Constructor<?> constructor = packetClass.getConstructor(List.class);
        return constructor.newInstance(List.of(uniqueId));
    }

    private Object createPlayerInfoInitializePacket(Object handle) throws ReflectiveOperationException {
        Class<?> packetClass = classForName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
        Class<?> serverPlayerClass = classForName("net.minecraft.server.level.ServerPlayer");
        Method factory = packetClass.getMethod("createSinglePlayerInitializing", serverPlayerClass, boolean.class);
        return factory.invoke(null, serverPlayerClass.cast(handle), true);
    }

    private Object createRespawnPacket(Object handle, Object level) throws ReflectiveOperationException {
        Class<?> respawnPacketClass = classForName("net.minecraft.network.protocol.game.ClientboundRespawnPacket");
        Object spawnInfo = invoke(handle, "createCommonSpawnInfo", level);
        byte keepAllData = (byte) getStaticField(respawnPacketClass, "KEEP_ALL_DATA");
        Constructor<?> constructor = respawnPacketClass.getConstructor(classForName("net.minecraft.network.protocol.game.CommonPlayerSpawnInfo"), byte.class);
        return constructor.newInstance(spawnInfo, keepAllData);
    }

    private Object createHeldSlotPacket(int heldSlot) throws ReflectiveOperationException {
        Class<?> packetClass = classForName("net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket");
        Constructor<?> constructor = packetClass.getConstructor(int.class);
        return constructor.newInstance(heldSlot);
    }

    private Object createExperiencePacket(float progress, int totalExperience, int level) throws ReflectiveOperationException {
        Class<?> packetClass = classForName("net.minecraft.network.protocol.game.ClientboundSetExperiencePacket");
        Constructor<?> constructor = packetClass.getConstructor(float.class, int.class, int.class);
        return constructor.newInstance(progress, totalExperience, level);
    }

    private Object createHealthPacket(float health, int foodLevel, float saturation) throws ReflectiveOperationException {
        Class<?> packetClass = classForName("net.minecraft.network.protocol.game.ClientboundSetHealthPacket");
        Constructor<?> constructor = packetClass.getConstructor(float.class, int.class, float.class);
        return constructor.newInstance(health, foodLevel, saturation);
    }

    private void sendPacket(Player player, Object packet) throws ReflectiveOperationException {
        Object handle = getHandle(player);
        Object connection = getField(handle, "connection");
        invoke(connection, "send", packet);
    }

    private Object getHandle(Player player) throws ReflectiveOperationException {
        return invoke(player, "getHandle");
    }

    private Object getStaticField(Class<?> owner, String fieldName) throws ReflectiveOperationException {
        Field field = owner.getField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

    private Object getField(Object instance, String fieldName) throws ReflectiveOperationException {
        Class<?> type = instance.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(instance);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }

        throw new NoSuchFieldException(fieldName);
    }

    private void setField(Object instance, String fieldName, Object value) throws ReflectiveOperationException {
        Class<?> type = instance.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(instance, value);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }

        throw new NoSuchFieldException(fieldName);
    }

    private Object invoke(Object instance, String methodName, Object... args) throws ReflectiveOperationException {
        Method method = findMethod(instance.getClass(), methodName, args);
        method.setAccessible(true);
        return method.invoke(instance, args);
    }

    private Object invokeAny(Object instance, String firstMethodName, String secondMethodName, Object... args) throws ReflectiveOperationException {
        try {
            return invoke(instance, firstMethodName, args);
        } catch (NoSuchMethodException ignored) {
            return invoke(instance, secondMethodName, args);
        }
    }

    private Method findMethod(Class<?> type, String methodName, Object... args) throws NoSuchMethodException {
        Class<?> current = type;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(methodName)) {
                    continue;
                }

                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != args.length) {
                    continue;
                }

                if (parametersMatch(parameterTypes, args)) {
                    return method;
                }
            }
            current = current.getSuperclass();
        }

        throw new NoSuchMethodException(methodName);
    }

    private boolean parametersMatch(Class<?>[] parameterTypes, Object[] args) {
        for (int index = 0; index < parameterTypes.length; index++) {
            Object argument = args[index];
            if (argument == null) {
                continue;
            }

            Class<?> expectedType = wrap(parameterTypes[index]);
            if (!expectedType.isAssignableFrom(argument.getClass())) {
                return false;
            }
        }

        return true;
    }

    private Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }

    private Class<?> classForName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }
}
