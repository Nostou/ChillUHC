package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
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
        if (tier > 4) tier = 1;
        setTier(player, tier);
    }

    public void decreaseTier(Player player) {
        int tier = getTier(player) - 1;
        if (tier < 1) tier = 4;
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
                .append(buildTierHeader(1, NamedTextColor.GOLD))
                .append(logTier(1))
                .append(buildTierHeader(2, NamedTextColor.LIGHT_PURPLE))
                .append(logTier(2))
                .append(buildTierHeader(3, NamedTextColor.AQUA))
                .append(logTier(3))
                .append(buildTierHeader(4, NamedTextColor.GREEN))
                .append(logTier(4))
                .append(Component.text("\n[?] » ", NamedTextColor.GRAY))
                .append(logTier(0));
    }

    private Component buildTierHeader(int tier, NamedTextColor color) {
        return Component.text("\n[" + tier + "] » ", color)
                .hoverEvent(HoverEvent.showText(getTierHoverText(tier)));
    }

    private Component getTierHoverText(int tier) {
        int maxDiamondArmorPieces = getMaxDiamondArmorPieces(tier);

        return switch (tier) {
            case 1 -> Component.text()
                    .append(Component.text("-0% damage taken", NamedTextColor.GOLD))
                    .append(Component.text("\n" + maxDiamondArmorPieces + " diamond pieces", NamedTextColor.GOLD))
                    .build();
            case 2 -> Component.text()
                    .append(Component.text("-8% damage taken", NamedTextColor.LIGHT_PURPLE))
                    .append(Component.text("\n" + maxDiamondArmorPieces + " diamond pieces", NamedTextColor.LIGHT_PURPLE))
                    .build();
            case 3 -> Component.text()
                    .append(Component.text("-16% damage taken", NamedTextColor.AQUA))
                    .append(Component.text("\n" + maxDiamondArmorPieces + " diamond pieces", NamedTextColor.AQUA))
                    .build();
            case 4 -> Component.text()
                    .append(Component.text("-24% damage taken", NamedTextColor.GREEN))
                    .append(Component.text("\n" + maxDiamondArmorPieces + " diamond pieces", NamedTextColor.GREEN))
                    .append(Component.text("\n+2❤", NamedTextColor.GREEN))
                    .build();
            default -> Component.text("No tier effect", NamedTextColor.GRAY);
        };
    }

    public double getDamageMultiplier(Player player) {
        return getDamageMultiplier(getTier(player));
    }

    public double getDamageMultiplier(int tier) {
        return switch (tier) {
            case 2 -> 0.92;
            case 3 -> 0.84;
            case 4 -> 0.76;
            default -> 1.0;
        };
    }

    public int getMaxDiamondArmorPieces(Player player) {
        return getMaxDiamondArmorPieces(getTier(player));
    }

    public int getMaxDiamondArmorPieces(int tier) {
        return switch (tier) {
            case 1 -> 2;
            case 2 -> 3;
            case 3, 4 -> 4;
            default -> 0;
        };
    }

    public double getBonusMaxHealth(Player player) {
        return getBonusMaxHealth(getTier(player));
    }

    public double getBonusMaxHealth(int tier) {
        return switch (tier) {
            case 4 -> 4.0;
            default -> 0.0;
        };
    }

    public void applyTierAttributes(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) return;

        double newMaxHealth = 20.0 + getBonusMaxHealth(player);
        attribute.setBaseValue(newMaxHealth);
        if (player.getHealth() > newMaxHealth) {
            player.setHealth(newMaxHealth);
        }
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
