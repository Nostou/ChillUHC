package fr.Nosta.ChillUHC.Scenarios;

import fr.Nosta.ChillUHC.Enums.GameState;
import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class ChillReviveScenario implements Scenario, Listener {

    private static final int AUTO_REVIVE_DELAY_SECONDS = 5;
    private static final double AUTO_REVIVE_HEALTH = 10.0;
    private static final List<ItemStack> AUTO_REVIVE_REWARDS = List.of(
            new ItemStack(Material.DIAMOND, 1),
            new ItemStack(Material.GOLD_INGOT, 2)
    );

    private final Main plugin;
    private BukkitTask meetupTask;

    public ChillReviveScenario(Main plugin) {
        this.plugin = plugin;
        plugin.getGameManager().onGameStart.addListener((ignored) -> onGameStart());
        plugin.getGameManager().onGameStop.addListener((ignored) -> reset());
    }

    @Override
    public ScenarioType getType() {
        return ScenarioType.CHILL_REVIVE;
    }

    public boolean isAutomaticReviveActive() {
        if (!plugin.getScenarioManager().isEnabled(getType())) return false;
        if (plugin.getGameManager().getState() != GameState.PLAYING) return false;

        return plugin.getGameManager().getElapsedSeconds() < plugin.getBorderManager().getMeetupDuration();
    }

    public void handleAutomaticDeath(Player player) {
        plugin.getReviveManager().recordDeath(player);
        scheduleAutomaticRevive(player);
    }

    private void onGameStart() {
        reset();
        if (!plugin.getScenarioManager().isEnabled(getType())) return;

        World world = plugin.getWorld();
        world.setGameRule(GameRules.KEEP_INVENTORY, true);

        long meetupTicks = plugin.getBorderManager().getMeetupDuration() * 20L;
        meetupTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getGameManager().getState() != GameState.PLAYING) return;
            if (!plugin.getScenarioManager().isEnabled(getType())) return;

            world.setGameRule(GameRules.KEEP_INVENTORY, false);
        }, meetupTicks);
    }

    private void scheduleAutomaticRevive(Player player) {
        UUID playerId = player.getUniqueId();

        new BukkitRunnable() {
            private int remainingSeconds = AUTO_REVIVE_DELAY_SECONDS;

            @Override
            public void run() {
                Player target = plugin.getServer().getPlayer(playerId);
                if (target == null || !plugin.getReviveManager().hasDeathState(target)) {
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

    private void reviveAutomatically(Player player) {
        plugin.getReviveManager().revive(player);
        player.setHealth(Math.min(plugin.getReviveManager().getPlayerMaxHealth(player), AUTO_REVIVE_HEALTH));
        plugin.getScoreboardManager().updateHealth(player);
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 1f);
        distributeAutoReviveRewards(player);
    }

    private void showCountdownTitle(Player player, int remainingSeconds) {
        Title title = Title.title(
                Component.text("Respawn in " + remainingSeconds, NamedTextColor.YELLOW),
                Component.text("Get ready...", NamedTextColor.GRAY),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1100), Duration.ofMillis(100))
        );

        player.showTitle(title);
    }

    private void distributeAutoReviveRewards(Player deadPlayer) {
        Team deadPlayerTeam = plugin.getTeamManager().getTeam(deadPlayer);

        Component message = CustomMessage.prefix("Chill Revive")
                .append(Component.text(deadPlayer.getName(), plugin.getTeamManager().getColor(deadPlayer)))
                .append(Component.text(" has been revived! ", NamedTextColor.GREEN))
                .append(Component.text("[+1 diamond] ", NamedTextColor.AQUA))
                .append(Component.text("[2 golds]", NamedTextColor.GOLD));

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getUniqueId().equals(deadPlayer.getUniqueId())) continue;
            if (player.getGameMode() != GameMode.SURVIVAL) continue;
            if (deadPlayerTeam != null && deadPlayerTeam.equals(plugin.getTeamManager().getTeam(player))) continue;

            CustomMessage.send(player, message);
            player.give(AUTO_REVIVE_REWARDS);
        }
    }

    private void reset() {
        if (meetupTask != null) {
            meetupTask.cancel();
            meetupTask = null;
        }

        plugin.getWorld().setGameRule(GameRules.KEEP_INVENTORY, false);
    }
}
