package fr.Nosta.ChillUHC.Inventories;

import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Scenarios.AnonymousScenario;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ScenarioInventory implements InventoryHolder {

    private static final Component TITLE = Component.text("Scenarios", NamedTextColor.DARK_GRAY);
    private static final int INVENTORY_SIZE = 9;

    private final Main plugin;
    private final Inventory inventory;
    private final List<ScenarioType> orderedScenarios;

    public ScenarioInventory(Main plugin) {
        this.plugin = plugin;
        this.orderedScenarios = Arrays.stream(ScenarioType.values())
                .sorted(Comparator.comparing(ScenarioType::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        this.inventory = Bukkit.createInventory(this, INVENTORY_SIZE, TITLE);
        initialize();
    }

    private void initialize() {
        inventory.clear();

        int slot = 0;
        for (ScenarioType scenario : orderedScenarios) {
            inventory.setItem(slot++, createScenarioItem(scenario));
        }
    }

    private ItemStack createScenarioItem(ScenarioType scenario) {
        boolean enabled = plugin.getScenarioManager().isEnabled(scenario);

        ItemStack item = new ItemStack(scenario.getIcon());
        ItemMeta meta = item.getItemMeta();

        NamedTextColor statusColor = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;
        String statusText = enabled ? "Enabled" : "Disabled";

        meta.displayName(Component.text(scenario.getDisplayName(), NamedTextColor.YELLOW, TextDecoration.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Status: ", NamedTextColor.DARK_GRAY)
                .append(Component.text(statusText, statusColor)));
        lore.add(Component.text("Left click to toggle", NamedTextColor.AQUA));

        if (scenario == ScenarioType.ANONYMOUS) {
            AnonymousScenario anonymousScenario = plugin.getScenarioManager().getScenario(ScenarioType.ANONYMOUS, AnonymousScenario.class);
            String source = anonymousScenario != null ? anonymousScenario.getConfiguredSkinSource() : "";
            String skinValue = source == null || source.isBlank() ? "None" : source;
            lore.add(Component.text("Right click to set shared skin", NamedTextColor.GOLD));
            lore.add(Component.text("Skin: ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(skinValue, NamedTextColor.WHITE)));
        }

        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public void update() {
        initialize();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public ScenarioType getScenario(int slot) {
        if (slot < 0 || slot >= orderedScenarios.size()) {
            return null;
        }

        return orderedScenarios.get(slot);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public static boolean isScenarioInventory(Inventory inventory) {
        return inventory.getHolder() instanceof ScenarioInventory;
    }
}
