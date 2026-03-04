package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager {

    private final Main plugin;
    private final Scoreboard scoreboard;

    public ScoreboardManager(Main plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public void initialize() {
        removeObjective("health");
        removeObjective("kills");

        registerHealthObjective();
        registerKillsObjective();
    }

    private void registerHealthObjective() {
        Objective health = scoreboard.registerNewObjective("health", Criteria.HEALTH, Component.text("❤"));
        health.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }

    private void registerKillsObjective() {
        Objective kills = scoreboard.registerNewObjective("kills", Criteria.PLAYER_KILL_COUNT, Component.text("Kills"));
        kills.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    private void removeObjective(String name) {
        Objective obj = scoreboard.getObjective(name);
        if (obj != null) obj.unregister();
    }
}
