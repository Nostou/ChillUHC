package fr.Nosta.ChillUHC.Scenarios;

import fr.Nosta.ChillUHC.Enums.ScenarioType;

public interface Scenario {

    ScenarioType getType();

    default void onEnable() {}

    default void onDisable() {}
}
