package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.Map;

public class ScenarioManager {

    private static final String CONFIG_PATH = "scenarios.";

    private final Main plugin;
    private final Map<ScenarioType, Boolean> scenarioStates = new EnumMap<>(ScenarioType.class);

    public ScenarioManager(Main plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();

        for (ScenarioType scenario : ScenarioType.values()) {
            String path = CONFIG_PATH + scenario.getKey();
            boolean enabled = config.getBoolean(path, false);

            scenarioStates.put(scenario, enabled);
            if (!config.contains(path)) {
                config.set(path, false);
            }
        }

        plugin.saveConfig();
    }

    public boolean isEnabled(ScenarioType scenario) {
        return scenarioStates.getOrDefault(scenario, false);
    }

    public void setEnabled(ScenarioType scenario, boolean enabled) {
        scenarioStates.put(scenario, enabled);
        plugin.getConfig().set(CONFIG_PATH + scenario.getKey(), enabled);
        plugin.saveConfig();
    }

    public boolean toggle(ScenarioType scenario) {
        boolean newValue = !isEnabled(scenario);
        setEnabled(scenario, newValue);
        return newValue;
    }
}
