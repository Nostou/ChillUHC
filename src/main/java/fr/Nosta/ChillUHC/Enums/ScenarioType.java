package fr.Nosta.ChillUHC.Enums;

import org.bukkit.Material;

public enum ScenarioType {
    BETA_ZOMBIES("beta_zombies", "Beta Zombies", "Instead of zombies dropping Rotten Flesh, instead they drop 0-2 feathers.", Material.FEATHER),
    CUTCLEAN("cutclean", "CutClean", "All ores and animal food will be dropped in it's smelted version.", Material.IRON_INGOT),
    IRONMAN("ironman", "Ironman", "Stay damage-free as long as possible to earn bonus health and golden apples.", Material.IRON_CHESTPLATE),
    TIMBER("timber", "Timber", "When you break a tree, the entire logs of that tree will come off.", Material.IRON_AXE),
    HASTEY_BOYS("hastey_boys", "Hastey Boys", "Every tool that you craft will have Efficiency 2 and Unbreaking 2.", Material.GOLDEN_PICKAXE);

    private final String key;
    private final String displayName;
    private final String description;
    private final Material icon;

    ScenarioType(String key, String displayName, String description, Material icon) {
        this.key = key;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Material getIcon() {
        return icon;
    }
}
