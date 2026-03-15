package fr.Nosta.ChillUHC.Inventories;

import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ScenarioInventory implements InventoryHolder {

    private static final Component TITLE = Component.text("Scenarios", NamedTextColor.DARK_GRAY);
    private static final int INVENTORY_SIZE = 27;

    private final Main plugin;
    private final Inventory inventory;

    public ScenarioInventory(Main plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, INVENTORY_SIZE, TITLE);
        initialize();
    }

    private void initialize() {
        inventory.clear();

        int slot = 10;
        for (ScenarioType scenario : ScenarioType.values()) {
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
        lore.add(Component.text("Click to toggle", NamedTextColor.AQUA));
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
        int index = slot - 10;
        ScenarioType[] scenarios = ScenarioType.values();

        if (index < 0 || index >= scenarios.length) {
            return null;
        }

        return scenarios[index];
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public static boolean isScenarioInventory(Inventory inventory) {
        return inventory.getHolder() instanceof ScenarioInventory;
    }
}
