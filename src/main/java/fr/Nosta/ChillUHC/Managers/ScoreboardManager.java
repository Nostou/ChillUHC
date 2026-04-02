package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager {

    private static final String HEALTH_OBJECTIVE = "Health";
    private static final String KILLS_OBJECTIVE = "Kills";
    private static final String TIER_OBJECTIVE = "Tier";

    private final Main plugin;
    private final Scoreboard scoreboard;

    public ScoreboardManager(Main plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public void initialize() {
        removeObjective(HEALTH_OBJECTIVE);
        removeObjective(KILLS_OBJECTIVE);
        removeObjective(TIER_OBJECTIVE);

        registerHealthObjective();
        registerKillsObjective();
        registerTierObjective();
        updateAllHealth();
    }

    private void registerHealthObjective() {
        Objective health = scoreboard.registerNewObjective(HEALTH_OBJECTIVE, Criteria.DUMMY, Component.text("❤"));
        health.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }

    private void registerKillsObjective() {
        Objective kills = scoreboard.registerNewObjective(KILLS_OBJECTIVE, Criteria.PLAYER_KILL_COUNT, Component.text("Kills"));
        kills.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    private void registerTierObjective() {
        scoreboard.registerNewObjective(TIER_OBJECTIVE, Criteria.DUMMY, Component.text("Tier"));
    }

    private void removeObjective(String name) {
        Objective obj = scoreboard.getObjective(name);
        if (obj != null) obj.unregister();
    }

    public Objective getObjective(String name) {
        return scoreboard.getObjective(name);
    }

    public void updateAllHealth() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateHealth(player);
        }
    }

    public void updateHealth(Player player) {
        Objective healthObjective = getObjective(HEALTH_OBJECTIVE);
        healthObjective.getScore(player.getName()).setScore(getHealthPercent(player));
    }

    public int getHealthPercent(Player player) {
        double effectiveHealth = Math.max(0.0, player.getHealth() + player.getAbsorptionAmount());
        return (int) Math.round(effectiveHealth * 5.0);
    }
}
