package fr.Nosta.ChillUHC.Enums;

import org.bukkit.Material;

public enum ScenarioType {
    CUTCLEAN("cutclean", "CutClean", Material.IRON_INGOT),
    TIMBER("timber", "Timber", Material.IRON_AXE),
    HASTEY_BOYS("hastey_boys", "Hastey Boys", Material.GOLDEN_PICKAXE);

    private final String key;
    private final String displayName;
    private final Material icon;

    ScenarioType(String key, String displayName, Material icon) {
        this.key = key;
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }
}
