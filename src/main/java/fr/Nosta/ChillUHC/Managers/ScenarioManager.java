package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Scenarios.Scenario;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScenarioManager {

    private static final String CONFIG_PATH = "scenarios.";

    private final Main plugin;
    private final Map<ScenarioType, Boolean> scenarioStates = new EnumMap<>(ScenarioType.class);
    private final Map<ScenarioType, Scenario> scenarios = new EnumMap<>(ScenarioType.class);

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

    public void registerScenario(Scenario scenario) {
        scenarios.put(scenario.getType(), scenario);
    }

    public Scenario getScenario(ScenarioType type) {
        return scenarios.get(type);
    }

    public <T extends Scenario> T getScenario(ScenarioType type, Class<T> scenarioClass) {
        Scenario scenario = scenarios.get(type);
        if (scenario == null) {
            return null;
        }

        return scenarioClass.cast(scenario);
    }

    public void initializeRegisteredScenarios() {
        for (Scenario scenario : scenarios.values()) {
            if (isEnabled(scenario.getType())) {
                scenario.onEnable();
            }
        }
    }

    public void setEnabled(ScenarioType scenario, boolean enabled) {
        boolean previous = isEnabled(scenario);
        if (previous == enabled) {
            return;
        }

        scenarioStates.put(scenario, enabled);
        plugin.getConfig().set(CONFIG_PATH + scenario.getKey(), enabled);
        plugin.saveConfig();

        Scenario scenarioInstance = scenarios.get(scenario);
        if (scenarioInstance != null) {
            if (enabled) {
                scenarioInstance.onEnable();
            } else {
                scenarioInstance.onDisable();
            }
        }
    }

    public boolean toggle(ScenarioType scenario) {
        boolean newValue = !isEnabled(scenario);
        setEnabled(scenario, newValue);
        return newValue;
    }

    public List<ScenarioType> getEnabledScenarios() {
        return scenarioStates.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .sorted((first, second) -> String.CASE_INSENSITIVE_ORDER.compare(first.getDisplayName(), second.getDisplayName()))
                .collect(Collectors.toList());
    }

    public Component logScenarios() {
        List<ScenarioType> enabledScenarios = getEnabledScenarios();
        if (enabledScenarios.isEmpty()) {
            return Component.text("No active scenarios", NamedTextColor.GRAY);
        }

        Component result = Component.text("\n\nScenarios » ", NamedTextColor.AQUA);
        Component separator = Component.text("/", NamedTextColor.DARK_GRAY);

        for (int i = 0; i < enabledScenarios.size(); i++) {
            if (i > 0) {
                result = result.append(separator);
            }

            ScenarioType scenario = enabledScenarios.get(i);
            result = result.append(Component.text(scenario.getDisplayName(), NamedTextColor.GREEN)
                    .hoverEvent(HoverEvent.showText(Component.text(scenario.getDescription(), NamedTextColor.GRAY))));
        }

        return result;
    }
}
