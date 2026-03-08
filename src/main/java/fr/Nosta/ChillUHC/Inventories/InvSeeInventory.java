package fr.Nosta.ChillUHC.Inventories;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class InvSeeInventory implements InventoryHolder {

    private final Inventory inventory;

    public InvSeeInventory(Player target) {
        Component title = Component.text(target.getName(), NamedTextColor.DARK_GRAY);
        this.inventory = Bukkit.createInventory(this, 45, title);
        fillInventory(target);
    }

    private void fillInventory(Player target) {
        PlayerInventory playerInv = target.getInventory();

        ItemStack[] contents = playerInv.getContents();
        for (int i = 0; i < 36; i++) {
            inventory.setItem(i, contents[i]);
        }

        ItemStack[] armor = playerInv.getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            inventory.setItem(36 + i, armor[i]);
        }

        inventory.setItem(40, playerInv.getItemInOffHand());
    }

    public void open(Player viewer) {
        viewer.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
    public static boolean isInvSeeInventory(Inventory inventory) {
        return inventory.getHolder() instanceof InvSeeInventory;
    }
}