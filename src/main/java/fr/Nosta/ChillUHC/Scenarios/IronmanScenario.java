package fr.Nosta.ChillUHC.Scenarios;

import fr.Nosta.ChillUHC.Enums.GameState;
import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.ScenarioMessage;
import fr.Nosta.ChillUHC.Utils.TimeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IronmanScenario implements Scenario, Listener {

    private final Main plugin;
    private final Set<UUID> playerList = new LinkedHashSet<>();

    private boolean tracking;

    public IronmanScenario(Main plugin) {
        this.plugin = plugin;
        plugin.getGameManager().onGameStart.addListener((ignored) -> startTracking());
        plugin.getGameManager().onGameStop.addListener((ignored) -> reset());
    }

    @Override
    public ScenarioType getType() {
        return ScenarioType.IRONMAN;
    }

    public void startTracking() {
        if (!plugin.getScenarioManager().isEnabled(getType())) return;

        reset();
        tracking = true;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() != GameMode.SURVIVAL) continue;
            playerList.add(player.getUniqueId());
        }

        checkForWinner();
    }

    public void reset() {
        playerList.clear();
        tracking = false;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!tracking) return;
        if (plugin.getGameManager().getState() != GameState.PLAYING) return;
        if (!plugin.getScenarioManager().isEnabled(getType())) return;
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getFinalDamage() <= 0) return;
        if (!playerList.remove(player.getUniqueId())) return;

        IronmanReward reward = IronmanReward.fromRemainingCount(playerList.size());
        if (reward != null) applyReward(player, reward);

        Bukkit.broadcast(buildDamageMessage(player, reward));
        checkForWinner();
    }

    private void checkForWinner() {
        if (!tracking) return;
        if (playerList.size() != 1) return;

        Player winner = Bukkit.getPlayer(playerList.iterator().next());
        if (winner == null) return;

        tracking = false;
        applyReward(winner, IronmanReward.FIRST);
        Bukkit.broadcast(buildWinnerMessage(winner));
    }

    private void applyReward(Player player, IronmanReward reward) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute != null) attribute.setBaseValue(attribute.getBaseValue() + (reward.bonusHearts * 2.0));

        player.give(List.of(new ItemStack(Material.GOLDEN_APPLE, reward.goldenApples)));
    }

    private Component buildDamageMessage(Player player, IronmanReward reward) {
        String timeElapsed = TimeUtils.formatToCompactMinutesSeconds(plugin.getGameManager().getElapsedSeconds());
        Component message = ScenarioMessage.prefix("Ironman")
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                .append(Component.text(player.getName(), plugin.getTeamManager().getColor(player)))
                .append(Component.text(" took damage!", NamedTextColor.GRAY))
                .append(Component.text(" [" + timeElapsed + "]", NamedTextColor.AQUA))
                .append(Component.text(" (" + playerList.size() + " left)", NamedTextColor.DARK_PURPLE, TextDecoration.ITALIC));

        if (reward != null) {
            message = message.append(Component.text(" [+" + reward.bonusHearts + "❤]", NamedTextColor.GREEN));
        }

        return message;
    }

    private Component buildWinnerMessage(Player player) {
        return ScenarioMessage.prefix("Ironman")
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                .append(Component.text(player.getName(), plugin.getTeamManager().getColor(player)))
                .append(Component.text(" is the ", NamedTextColor.GRAY))
                .append(Component.text("Ironman", NamedTextColor.GOLD, TextDecoration.BOLD, TextDecoration.UNDERLINED))
                .append(Component.text(" [+" + IronmanReward.FIRST.bonusHearts + "❤]", NamedTextColor.GREEN));
    }

    private enum IronmanReward {
        THIRD(1, 1),
        SECOND(2, 2),
        FIRST(3, 3);

        private final int bonusHearts;
        private final int goldenApples;

        IronmanReward(int bonusHearts, int goldenApples) {
            this.bonusHearts = bonusHearts;
            this.goldenApples = goldenApples;
        }

        private static IronmanReward fromRemainingCount(int remaining) {
            return switch (remaining) {
                case 2 -> THIRD;
                case 1 -> SECOND;
                default -> null;
            };
        }
    }
}
