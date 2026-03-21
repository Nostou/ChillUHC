package fr.Nosta.ChillUHC.Scenarios;

import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Main;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class HasteyBoysScenario implements Scenario, Listener {

    private static final int EFFICIENCY_LEVEL = 2;
    private static final int UNBREAKING_LEVEL = 2;

    private final Main plugin;

    public HasteyBoysScenario(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public ScenarioType getType() {
        return ScenarioType.HASTEY_BOYS;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!plugin.getScenarioManager().isEnabled(getType())) return;

        ItemStack result = event.getInventory().getResult();
        if (result == null || !isEligibleTool(result.getType())) return;

        ItemStack enchantedResult = result.clone();
        enchantedResult.addUnsafeEnchantment(Enchantment.EFFICIENCY, EFFICIENCY_LEVEL);
        enchantedResult.addUnsafeEnchantment(Enchantment.UNBREAKING, UNBREAKING_LEVEL);
        event.getInventory().setResult(enchantedResult);
    }

    private boolean isEligibleTool(Material material) {
        return material == Material.WOODEN_PICKAXE
                || material == Material.STONE_PICKAXE
                || material == Material.COPPER_PICKAXE
                || material == Material.IRON_PICKAXE
                || material == Material.GOLDEN_PICKAXE
                || material == Material.DIAMOND_PICKAXE
                || material == Material.NETHERITE_PICKAXE
                || material == Material.WOODEN_AXE
                || material == Material.STONE_AXE
                || material == Material.COPPER_AXE
                || material == Material.IRON_AXE
                || material == Material.GOLDEN_AXE
                || material == Material.DIAMOND_AXE
                || material == Material.NETHERITE_AXE
                || material == Material.WOODEN_SHOVEL
                || material == Material.STONE_SHOVEL
                || material == Material.COPPER_SHOVEL
                || material == Material.IRON_SHOVEL
                || material == Material.GOLDEN_SHOVEL
                || material == Material.DIAMOND_SHOVEL
                || material == Material.NETHERITE_SHOVEL
                || material == Material.WOODEN_HOE
                || material == Material.STONE_HOE
                || material == Material.COPPER_HOE
                || material == Material.IRON_HOE
                || material == Material.GOLDEN_HOE
                || material == Material.DIAMOND_HOE
                || material == Material.NETHERITE_HOE;
    }
}
