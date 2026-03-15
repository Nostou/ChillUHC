package fr.Nosta.ChillUHC.Scenarios;

import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

import java.util.List;

public class CutCleanScenario implements Scenario, Listener {

    private final Main plugin;

    public CutCleanScenario(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public ScenarioType getType() {
        return ScenarioType.CUTCLEAN;
    }

    @EventHandler
    public void onBlockDrop(BlockDropItemEvent event) {
        if (!plugin.getScenarioManager().isEnabled(getType())) return;

        List<Item> droppedItems = event.getItems();
        if (droppedItems.isEmpty()) return;

        int xpToDrop = 0;
        Player player = event.getPlayer();
        Material toolType = player.getInventory().getItemInMainHand().getType();

        for (Item itemEntity : droppedItems) {
            ItemStack original = itemEntity.getItemStack();
            ItemStack result = getBlockDropResult(original, toolType);
            if (result == null) continue;

            itemEntity.remove();
            event.getBlock().getWorld().dropItemNaturally(itemEntity.getLocation(), result);

            if (isOreDrop(original.getType())) xpToDrop++;
        }

        spawnExperience(event.getBlock(), xpToDrop);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!plugin.getScenarioManager().isEnabled(getType())) return;

        List<ItemStack> drops = event.getDrops();
        for (int i = 0; i < drops.size(); i++) {
            ItemStack smelted = getEntityDropResult(drops.get(i));
            if (smelted != null) {
                drops.set(i, smelted);
            }
        }

        ensureGuaranteedDrops(event);
    }

    private ItemStack getBlockDropResult(ItemStack input, Material toolType) {
        if (input == null || input.getType().isAir()) return null;

        Material resultType = switch (input.getType()) {
            case RAW_IRON -> Material.IRON_INGOT;
            case RAW_GOLD -> Material.GOLD_INGOT;
            case RAW_COPPER -> Material.COPPER_INGOT;
            case GRAVEL -> isShovel(toolType) ? Material.FLINT : null;
            default -> null;
        };

        if (resultType == null) return null;
        return new ItemStack(resultType, input.getAmount());
    }

    private ItemStack getEntityDropResult(ItemStack input) {
        if (input == null || input.getType().isAir()) return null;

        Material resultType = switch (input.getType()) {
            case BEEF -> Material.COOKED_BEEF;
            case PORKCHOP -> Material.COOKED_PORKCHOP;
            case CHICKEN -> Material.COOKED_CHICKEN;
            case MUTTON -> Material.COOKED_MUTTON;
            case RABBIT -> Material.COOKED_RABBIT;
            case COD -> Material.COOKED_COD;
            case SALMON -> Material.COOKED_SALMON;
            default -> null;
        };

        if (resultType == null) return null;
        return new ItemStack(resultType, input.getAmount());
    }

    private boolean isOreDrop(Material material) {
        return material == Material.RAW_IRON
                || material == Material.RAW_GOLD
                || material == Material.RAW_COPPER;
    }

    private boolean isShovel(Material material) {
        return material == Material.WOODEN_SHOVEL
                || material == Material.STONE_SHOVEL
                || material == Material.IRON_SHOVEL
                || material == Material.GOLDEN_SHOVEL
                || material == Material.DIAMOND_SHOVEL
                || material == Material.NETHERITE_SHOVEL;
    }

    private void ensureGuaranteedDrops(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        switch (entity.getType()) {
            case COW, HORSE -> ensureDrop(event.getDrops(), Material.LEATHER);
            case CHICKEN -> ensureDrop(event.getDrops(), Material.FEATHER);
        }
    }

    private void ensureDrop(List<ItemStack> drops, Material material) {
        for (ItemStack drop : drops) {
            if (drop != null && drop.getType() == material && drop.getAmount() > 0) {
                return;
            }
        }

        drops.add(new ItemStack(material, 1));
    }

    private void spawnExperience(Block block, int amount) {
        if (amount <= 0) return;

        Location location = block.getLocation().add(0.5, 0.5, 0.5);
        ExperienceOrb orb = block.getWorld().spawn(location, ExperienceOrb.class);
        orb.setExperience(amount);
    }
}
