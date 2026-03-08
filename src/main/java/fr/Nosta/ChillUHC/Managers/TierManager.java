package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

public class TierManager {

    private final Main plugin;

    public TierManager(Main plugin) {
        this.plugin = plugin;
    }

    private Objective getObjective() {
        return plugin.getScoreboardManager().getObjective("Tier");
    }

    public int getTier(Player player) {
        return getObjective().getScore(player.getName()).getScore();
    }

    public void setTier(Player player, int tier) {
        getObjective().getScore(player.getName()).setScore(tier);
    }

    public void increaseTier(Player player) {
        int tier = getTier(player) + 1;
        if (tier > 3) tier = 1;
        setTier(player, tier);
    }

    public void decreaseTier(Player player) {
        int tier = getTier(player) - 1;
        if (tier < 1) tier = 3;
        setTier(player, tier);
    }

    public boolean hasUndefinedPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getTier(player) <= 0) return true;
        }
        return false;
    }

    public Component logTierList() {
        return Component.text("= TIERS =", NamedTextColor.GREEN)
                .append(Component.text("\n[1] » ", NamedTextColor.LIGHT_PURPLE))
                .append(logTier(1))
                .append(Component.text("\n[2] » ", NamedTextColor.AQUA))
                .append(logTier(2))
                .append(Component.text("\n[3] » ", NamedTextColor.GOLD))
                .append(logTier(3))
                .append(Component.text("\n[?] » ", NamedTextColor.GRAY))
                .append(logTier(0));
    }

    private Component logTier(int value) {

        Component result = Component.empty();
        Component separator = Component.text("/", NamedTextColor.DARK_GRAY);

        boolean first = true;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getTier(player) != value) continue;

            NamedTextColor playerColor = plugin.getTeamManager().getColor(player);
            if (!first) result = result.append(separator);
            result = result.append(Component.text(player.getName(), playerColor));
            first = false;
        }

        if (first) return Component.text("No players in this tier", NamedTextColor.GRAY);
        return result;
    }
}
