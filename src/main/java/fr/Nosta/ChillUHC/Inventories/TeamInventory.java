package fr.Nosta.ChillUHC.Inventories;

import fr.Nosta.ChillUHC.Chill.ChillTeam;
import fr.Nosta.ChillUHC.Managers.TeamManager;
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
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamInventory implements InventoryHolder {

    private static final Component TITLE = Component.text("Teams", NamedTextColor.DARK_GRAY);

    private final TeamManager teamManager;
    private final Inventory inventory;

    public TeamInventory(TeamManager teamManager) {
        this.teamManager = teamManager;
        this.inventory = Bukkit.createInventory(this, 9, TITLE);
        initialize();
    }

    private void initialize() {
        inventory.setItem(0, createTeamItem("Blue", Material.BLUE_WOOL));
        inventory.setItem(1, createTeamItem("Cyan", Material.LIGHT_BLUE_WOOL));
        inventory.setItem(2, createTeamItem("Green", Material.GREEN_WOOL));
        inventory.setItem(3, createTeamItem("Orange", Material.ORANGE_WOOL));
        inventory.setItem(4, createTeamItem("Pink", Material.PINK_WOOL));
        inventory.setItem(5, createTeamItem("Purple", Material.PURPLE_WOOL));
        inventory.setItem(6, createTeamItem("Red", Material.RED_WOOL));
        inventory.setItem(7, createTeamItem("Yellow", Material.YELLOW_WOOL));
        inventory.setItem(8, createLeaveItem());
    }

    private ItemStack createTeamItem(String teamName, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        ChillTeam team = teamManager.getChillTeam(teamName);
        meta.displayName(Component.text(teamName, team.getColor()));

        List<Component> lore = new ArrayList<>();
        for (UUID uuid : team.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            lore.add(Component.text("- " + p.getName(), NamedTextColor.GRAY));
        }

        meta.lore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createLeaveItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Leave Team", NamedTextColor.RED, TextDecoration.BOLD));
        item.setItemMeta(meta);
        return item;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public static boolean isTeamInventory(Inventory inventory) {
        return inventory.getHolder() instanceof TeamInventory;
    }
}