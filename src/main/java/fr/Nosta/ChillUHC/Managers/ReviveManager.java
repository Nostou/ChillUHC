package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Enums.GameState;
import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.Spreader;
import fr.Nosta.ChillUHC.Utils.TeamUtils;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReviveManager {

    private static final int MIN_SPAWN_DISTANCE = 50;
    private static final int AUTO_REVIVE_DELAY_SECONDS = 5;
    private static final double AUTO_REVIVE_HEALTH = 10.0;
    private static final double MAX_HEALTH_PENALTY = 2.0;
    private static final double MIN_MAX_HEALTH = 2.0;

    private final Main plugin;
    private final Map<UUID, DeathState> deadPlayers = new ConcurrentHashMap<>();

    public ReviveManager(Main plugin) {
        this.plugin = plugin;
    }

    public void recordDeath(Player player, boolean automaticRevive) {
        deadPlayers.put(player.getUniqueId(), new DeathState(plugin.getGameManager().getElapsedSeconds(), automaticRevive));
    }

    public boolean hasDeathState(Player player) {
        return deadPlayers.containsKey(player.getUniqueId());
    }

    public boolean revive(Player player) {
        if (deadPlayers.remove(player.getUniqueId()) == null) return false;

        Location respawnLocation = getRespawnLocation(player);
        plugin.getGameManager().resetPlayer(player);
        player.teleport(respawnLocation);
        player.setGameMode(GameMode.SURVIVAL);
        sendRevivedMessage(player);

        return true;
    }

    public boolean isAutomaticReviveActive() {
        if (!plugin.getScenarioManager().isEnabled(ScenarioType.CHILL_REVIVE)) return false;
        if (plugin.getGameManager().getState() != GameState.PLAYING) return false;

        return plugin.getGameManager().getElapsedSeconds() < plugin.getBorderManager().getMeetupDuration();
    }

    public void scheduleAutomaticRevive(Player player) {
        UUID playerId = player.getUniqueId();

        new BukkitRunnable() {
            private int remainingSeconds = AUTO_REVIVE_DELAY_SECONDS;

            @Override
            public void run() {
                Player target = plugin.getServer().getPlayer(playerId);
                DeathState deathState = deadPlayers.get(playerId);
                if (target == null || deathState == null || !deathState.automaticRevive()) {
                    cancel();
                    return;
                }

                if (remainingSeconds > 0) {
                    showCountdownTitle(target, remainingSeconds);
                    target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
                    remainingSeconds--;
                    return;
                }

                reviveAutomatically(target);
                cancel();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private Location getRespawnLocation(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        List<Player> livingTeammates = getLivingTeammates(team, player);

        if (!livingTeammates.isEmpty()) {
            int index = (int) (Math.random() * livingTeammates.size());
            return livingTeammates.get(index).getLocation().clone();
        }

        return getRandomSpawnLocation();
    }

    private List<Player> getLivingTeammates(Team team, Player revivedPlayer) {
        if (team == null) return List.of();

        return TeamUtils.getPlayers(team).stream()
                .filter(teammate -> !teammate.getUniqueId().equals(revivedPlayer.getUniqueId()))
                .filter(teammate -> teammate.getGameMode() == GameMode.SURVIVAL)
                .toList();
    }

    private Location getRandomSpawnLocation() {
        World world = plugin.getWorld();
        Location center = plugin.getSpawnLocation();
        int radius = plugin.getBorderManager().getCurrentRadius();
        List<Location> locations = Spreader.generate(center, radius, 1, MIN_SPAWN_DISTANCE);

        if (!locations.isEmpty()) return locations.getFirst();

        int y = world.getHighestBlockYAt(center);
        return center.clone().set(center.getX(), y + 1, center.getZ());
    }

    private void reviveAutomatically(Player player) {
        DeathState deathState = deadPlayers.remove(player.getUniqueId());
        if (deathState == null || !deathState.automaticRevive()) return;

        Location respawnLocation = getRespawnLocation(player);
        player.clearActivePotionEffects();
        player.teleport(respawnLocation);
        player.setGameMode(GameMode.SURVIVAL);
        applyAutoRevivePenalty(player, deathState);
        player.setHealth(Math.min(getPlayerMaxHealth(player), AUTO_REVIVE_HEALTH));
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30 * 20, 4, false, false));
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 1f);
        sendRevivedMessage(player);
    }

    private void applyAutoRevivePenalty(Player player, DeathState deathState) {
        if (deathState.elapsedSeconds() <= plugin.getBorderManager().getMeetupDuration() / 2) return;

        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) return;

        double previousMaxHealth = attribute.getBaseValue();
        double newMaxHealth = Math.max(MIN_MAX_HEALTH, previousMaxHealth - MAX_HEALTH_PENALTY);
        if (newMaxHealth >= previousMaxHealth) return;

        attribute.setBaseValue(newMaxHealth);
        CustomMessage.error(player, "-1❤ permanent max health");
    }

    private double getPlayerMaxHealth(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        return attribute != null ? attribute.getBaseValue() : 20.0;
    }

    private void showCountdownTitle(Player player, int remainingSeconds) {
        Title title = Title.title(
                Component.text("Respawn in " + remainingSeconds, NamedTextColor.YELLOW),
                Component.text("Get ready...", NamedTextColor.GRAY),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1100), Duration.ofMillis(100))
        );

        player.showTitle(title);
    }

    private void sendRevivedMessage(Player player) {
        CustomMessage.success(player, "You have been revived.");
    }

    private record DeathState(long elapsedSeconds, boolean automaticRevive) { }
}
