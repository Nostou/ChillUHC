package fr.Nosta.ChillUHC.Scenarios;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import fr.Nosta.ChillUHC.Utils.NmsSkinApplier;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class AnonymousScenario implements Scenario, Listener {

    private static final String CONFIG_PATH = "anonymous.skin.";
    private static final String TAB_MASK = "???";
    private static final String TEXTURES_PROPERTY = "textures";

    private final Main plugin;
    private final NmsSkinApplier nmsSkinApplier;
    private final Map<UUID, SkinSnapshot> originalSkins = new HashMap<>();
    private final Set<UUID> pendingSkinInputs = new java.util.HashSet<>();

    public AnonymousScenario(Main plugin) {
        this.plugin = plugin;
        this.nmsSkinApplier = new NmsSkinApplier(plugin);
        ensureConfigDefaults();
        plugin.getTeamManager().onTeamChanged.addListener(this::onTeamChanged);
    }

    @Override
    public ScenarioType getType() {
        return ScenarioType.ANONYMOUS;
    }

    @Override
    public void onEnable() {
        plugin.getTeamManager().ModifyAllTeams(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        forEachOnlinePlayer(this::applyPlayerState);
    }

    @Override
    public void onDisable() {
        plugin.getTeamManager().ModifyAllTeams(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        forEachOnlinePlayer(this::applyPlayerState);
    }

    public void openSkinEditor(Player player) {
        pendingSkinInputs.add(player.getUniqueId());
        player.closeInventory();
        CustomMessage.info(player, "Type the skin name in chat. Type 'clear' to remove it or 'cancel' to abort.");
    }

    public String getConfiguredSkinSource() {
        return plugin.getConfig().getString(CONFIG_PATH + "source", "");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getScenarioManager().isEnabled(getType())) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            applyPlayerState(event.getPlayer());
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSkinInputChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!pendingSkinInputs.contains(player.getUniqueId())) return;

        pendingSkinInputs.remove(player.getUniqueId());
        event.setCancelled(true);

        String input = normalizeSkinName(PlainTextComponentSerializer.plainText().serialize(event.message()));
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (input.equalsIgnoreCase("cancel")) {
                CustomMessage.info(player, "Anonymous skin update cancelled.");
                return;
            }

            if (input.equalsIgnoreCase("clear")) {
                clearSharedSkin(player);
                return;
            }

            if (input.isBlank()) {
                CustomMessage.error(player, "Please type a valid skin name, 'clear', or 'cancel'.");
                return;
            }

            setSharedSkinSource(player, input);
        });
    }

    private void ensureConfigDefaults() {
        if (!plugin.getConfig().contains(CONFIG_PATH + "source")) {
            plugin.getConfig().set(CONFIG_PATH + "source", "");
        }
        if (!plugin.getConfig().contains(CONFIG_PATH + "texture")) {
            plugin.getConfig().set(CONFIG_PATH + "texture", "");
        }
        if (!plugin.getConfig().contains(CONFIG_PATH + "signature")) {
            plugin.getConfig().set(CONFIG_PATH + "signature", "");
        }
        plugin.saveConfig();
    }

    private void applyPlayerState(Player player) {
        updateTabState(player);

        if (plugin.getScenarioManager().isEnabled(getType())) {
            applyConfiguredSkin(player);
        } else {
            restoreOriginalSkin(player);
        }
    }

    private void onTeamChanged(Player player) {
        if (!plugin.getScenarioManager().isEnabled(getType())) {
            return;
        }

        updateTabState(player);
    }

    private void setSharedSkinSource(Player sender, String sourceName) {
        CustomMessage.info(sender, "Resolving skin from " + sourceName + "...");

        Bukkit.createProfile(sourceName).update().thenAccept(profile ->
                Bukkit.getScheduler().runTask(plugin, () -> applyResolvedSkin(sender, sourceName, (PlayerProfile) profile))
        ).exceptionally(throwable -> {
            Bukkit.getScheduler().runTask(plugin, () ->
                    CustomMessage.error(sender, "Unable to resolve that skin source."));
            return null;
        });
    }

    private void clearSharedSkin(Player sender) {
        plugin.getConfig().set(CONFIG_PATH + "source", "");
        plugin.getConfig().set(CONFIG_PATH + "texture", "");
        plugin.getConfig().set(CONFIG_PATH + "signature", "");
        plugin.saveConfig();

        if (plugin.getScenarioManager().isEnabled(getType())) {
            forEachOnlinePlayer(this::restoreOriginalSkin);
        }

        plugin.getInventoryManager().refreshScenarioInventory();
        CustomMessage.success(sender, "Anonymous shared skin cleared.");
    }

    private void applyResolvedSkin(Player sender, String sourceName, PlayerProfile sourceProfile) {
        ProfileProperty textures = getTexturesProperty(sourceProfile).orElse(null);
        if (textures == null) {
            CustomMessage.error(sender, "That player has no usable skin.");
            return;
        }

        plugin.getConfig().set(CONFIG_PATH + "source", sourceName);
        plugin.getConfig().set(CONFIG_PATH + "texture", textures.getValue());
        plugin.getConfig().set(CONFIG_PATH + "signature", textures.getSignature() == null ? "" : textures.getSignature());
        plugin.saveConfig();

        if (plugin.getScenarioManager().isEnabled(getType())) {
            forEachOnlinePlayer(this::applyConfiguredSkin);
        }

        plugin.getInventoryManager().refreshScenarioInventory();
        CustomMessage.success(sender, "Anonymous shared skin updated to " + sourceName + ".");
    }

    private void applyConfiguredSkin(Player player) {
        String textureValue = plugin.getConfig().getString(CONFIG_PATH + "texture", "");
        if (textureValue.isBlank()) return;

        originalSkins.computeIfAbsent(player.getUniqueId(), ignored -> SkinSnapshot.capture(player.getPlayerProfile()));
        String textureSignature = plugin.getConfig().getString(CONFIG_PATH + "signature", "");
        nmsSkinApplier.applySkin(player, textureValue, textureSignature);
    }

    private void restoreOriginalSkin(Player player) {
        SkinSnapshot snapshot = originalSkins.get(player.getUniqueId());
        if (snapshot == null || snapshot.textureValue == null) {
            return;
        }

        nmsSkinApplier.applySkin(player, snapshot.textureValue, snapshot.textureSignature);
        originalSkins.remove(player.getUniqueId());
    }

    private void updateTabState(Player player) {
        if (plugin.getScenarioManager().isEnabled(getType())) {
            player.playerListName(Component.text(TAB_MASK, plugin.getTeamManager().getColor(player)));
        } else {
            player.playerListName(null);
        }

        refreshTabEntry(player);
        Bukkit.getScheduler().runTask(plugin, () -> refreshTabEntry(player));
    }

    private void refreshTabEntry(Player target) {
        Collection<? extends Player> viewers = Bukkit.getOnlinePlayers();
        for (Player viewer : viewers) {
            if (viewer.equals(target)) {
                continue;
            }

            viewer.unlistPlayer(target);
            viewer.listPlayer(target);
        }
    }

    private String normalizeSkinName(String input) {
        return input == null ? "" : input.trim();
    }

    private void forEachOnlinePlayer(Consumer<Player> action) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            action.accept(player);
        }
    }

    private Optional<ProfileProperty> getTexturesProperty(PlayerProfile profile) {
        if (profile == null) {
            return Optional.empty();
        }

        return profile.getProperties().stream()
                .filter(property -> property.getName().equals(TEXTURES_PROPERTY))
                .findFirst();
    }

    private record SkinSnapshot(String textureValue, String textureSignature) {
        private static SkinSnapshot capture(PlayerProfile profile) {
            return profile.getProperties().stream()
                    .filter(property -> property.getName().equals(TEXTURES_PROPERTY))
                    .findFirst()
                    .map(property -> new SkinSnapshot(property.getValue(), property.getSignature()))
                    .orElse(new SkinSnapshot(null, null));
        }
    }
}
