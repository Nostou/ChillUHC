package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Enums.GameState;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Tasks.CompassTask;
import fr.Nosta.ChillUHC.Tasks.StartGameTask;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class GameManager {

    private static final int START_HEALTH = 20;
    private static final int START_FOOD_LEVEL = 20;
    private static final float START_SATURATION = 20f;
    private static final int START_BOOK_AMOUNT = 1;
    private static final int START_FOOD_AMOUNT = 10;
    private static final int RESISTANCE_DURATION_TICKS = 30 * 20;
    private static final int RESISTANCE_AMPLIFIER = 4;
    private static final int ABSORPTION_DURATION_TICKS = 1200 * 20;
    private static final int ABSORPTION_AMPLIFIER = 4;
    private static final long RESET_WORLD_TIME = 1000L;

    private final Main plugin;

    private GameState currentState = GameState.WAITING;
    public GameState getState() { return currentState; }

    private StartGameTask startTask;
    private CompassTask compassTask;

    public GameManager(Main plugin) {
        this.plugin = plugin;
    }

    public void initGame() {
        reset();

        //All time settings
        World world = plugin.getWorld();
        world.setGameRule(GameRules.ADVANCE_WEATHER, false);
        world.setGameRule(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS, false);
        world.setGameRule(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0);
        world.setGameRule(GameRules.IMMEDIATE_RESPAWN, true);
        world.setGameRule(GameRules.LOCATOR_BAR, false);
        world.setGameRule(GameRules.NATURAL_HEALTH_REGENERATION, false);
        world.setGameRule(GameRules.SHOW_ADVANCEMENT_MESSAGES, false);
        world.setGameRule(GameRules.SPAWN_PHANTOMS, false);
        world.setGameRule(GameRules.SPAWN_PATROLS, false);
        world.setGameRule(GameRules.SPAWN_WANDERING_TRADERS, false);

        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(Integer.MAX_VALUE);

        plugin.getBorderManager().initialize();
        plugin.getScoreboardManager().initialize();
        plugin.getTabManager().start();

        plugin.getLogger().warning("Game initialized.");
    }

    public void startGame() {
        currentState = GameState.STARTING;
        startTask = new StartGameTask(plugin);
        startTask.start();
        startTask.onCompleted.addListener((runnable) -> onGameStart());
    }

    public void onGameStart() {
        currentState = GameState.PLAYING;

        plugin.getBorderManager().startShrink();

        World world = plugin.getWorld();
        world.setDifficulty(Difficulty.EASY);
        world.setGameRule(GameRules.PVP, true);
        world.setGameRule(GameRules.ADVANCE_TIME, true);

        ItemStack book = new ItemStack(Material.BOOK, START_BOOK_AMOUNT);
        ItemStack food = new ItemStack(Material.COOKED_BEEF, START_FOOD_AMOUNT);
        List<ItemStack> starterItems = Arrays.asList(book, food);

        List<PotionEffect> starterEffects = Arrays.asList(
                new PotionEffect(PotionEffectType.RESISTANCE, RESISTANCE_DURATION_TICKS, RESISTANCE_AMPLIFIER, false, false),
                new PotionEffect(PotionEffectType.ABSORPTION, ABSORPTION_DURATION_TICKS, ABSORPTION_AMPLIFIER, false, false)
        );
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.give(starterItems);
            player.setHealth(START_HEALTH);
            player.setFoodLevel(START_FOOD_LEVEL);
            player.setSaturation(START_SATURATION);
            player.setGameMode(GameMode.SURVIVAL);
            player.addPotionEffects(starterEffects);
        }

        compassTask = new CompassTask(plugin);
        compassTask.start();
    }

    public void stopGame() {
        if (startTask != null) startTask.cancel();
        if (compassTask != null) compassTask.cancel();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(plugin.getSpawnLocation());
            player.getInventory().clear();
            player.clearActivePotionEffects();
            player.setGameMode(GameMode.ADVENTURE);
        }

        reset();

        CustomMessage.errorAll("Forced stop of the game by an operator.");
        currentState = GameState.WAITING;

        plugin.getBorderManager().reset();
    }

    private void reset() {
        World world = plugin.getWorld();
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setGameRule(GameRules.PVP, false);
        world.setGameRule(GameRules.ADVANCE_TIME, false);
        world.setTime(RESET_WORLD_TIME);
    }
}
