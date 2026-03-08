package fr.Nosta.ChillUHC.Inventories;

import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Managers.TierManager;
import fr.Nosta.ChillUHC.Utils.TeamUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TierInventory implements InventoryHolder {

    private static final Component TITLE = Component.text("Tiers");

    private final Main plugin;
    private final Inventory inventory;

    public TierInventory(Main plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, 54, TITLE);
        initialize();
    }

    private void initialize() {
        TierManager tierManager = plugin.getTierManager();

        int slot = 0;

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.sort(Comparator.comparing((Player p) -> {
            Team team = plugin.getTeamManager().getTeam(p);
            return team != null ? team.getName() : "zzz";
        })/*.thenComparingInt(tierManager::getTier)*/);

        for (Player p : players) {

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();

            meta.setOwningPlayer(p);

            NamedTextColor color = plugin.getTeamManager().getColor(p);
            Component name = Component.text(p.getName(), color);
            meta.displayName(name);

            int tier = tierManager.getTier(p);
            String tierString = tier == 0 ? "Undefined" : "Tier "+ tier;
            NamedTextColor finalColor = NamedTextColor.GRAY;
            if (tier == 1) finalColor = NamedTextColor.LIGHT_PURPLE;
            else if (tier == 2) finalColor = NamedTextColor.AQUA;
            else if (tier == 3) finalColor = NamedTextColor.GOLD;
            meta.lore(List.of(Component.text(tierString, finalColor)));

            head.setItemMeta(meta);

            inventory.setItem(slot++, head);
        }
    }

    public void update() {
        inventory.clear();
        initialize();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
    public static boolean isTierInventory(Inventory inventory) {
        return inventory.getHolder() instanceof TierInventory;
    }
}