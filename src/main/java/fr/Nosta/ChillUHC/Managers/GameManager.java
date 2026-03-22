package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Enums.GameState;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Tasks.CompassTask;
import fr.Nosta.ChillUHC.Tasks.StartGameTask;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import fr.Nosta.ChillUHC.Utils.SimpleEvent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class GameManager {

    private final Main plugin;
    public final SimpleEvent<Runnable> onGameStart = new SimpleEvent<>();
    public final SimpleEvent<Runnable> onGameStop = new SimpleEvent<>();

    private GameState currentState = GameState.WAITING;
    public GameState getState() { return currentState; }

    private StartGameTask startTask;
    private CompassTask compassTask;
    private long gameStartTimestamp;

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
        gameStartTimestamp = System.currentTimeMillis();

        World world = plugin.getWorld();
        world.setDifficulty(Difficulty.EASY);
        world.setGameRule(GameRules.PVP, true);
        world.setGameRule(GameRules.ADVANCE_TIME, true);

        ItemStack book = new ItemStack(Material.BOOK, 1);
        ItemStack food = new ItemStack(Material.COOKED_BEEF, 5);
        List<ItemStack> starterItems = Arrays.asList(book, food);

        List<PotionEffect> starterEffects = Arrays.asList(
                new PotionEffect(PotionEffectType.RESISTANCE, 30 * 20, 4, false, false),
                new PotionEffect(PotionEffectType.ABSORPTION, 1200 * 20, 4, false, false)
        );
        for (Player player : Bukkit.getOnlinePlayers()) {
            resetPlayer(player);
            player.give(starterItems);
            player.setGameMode(GameMode.SURVIVAL);
            player.addPotionEffects(starterEffects);
        }

        compassTask = new CompassTask(plugin);
        compassTask.start();
        onGameStart.invoke(() -> {});
    }

    public void stopGame() {
        if (startTask != null) startTask.cancel();
        if (compassTask != null) compassTask.cancel();
        onGameStop.invoke(() -> {});

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(plugin.getSpawnLocation());
            resetPlayer(player);
            player.setGameMode(GameMode.ADVENTURE);
        }

        reset();

        CustomMessage.errorAll("Forced stop of the game by an operator.");
        currentState = GameState.WAITING;
    }

    private void reset() {
        gameStartTimestamp = 0L;
        World world = plugin.getWorld();
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setGameRule(GameRules.PVP, false);
        world.setGameRule(GameRules.ADVANCE_TIME, false);
        world.setTime(1000L);
    }

    public long getElapsedSeconds() {
        if (currentState != GameState.PLAYING || gameStartTimestamp == 0L) {
            return 0L;
        }

        return Math.max(0L, (System.currentTimeMillis() - gameStartTimestamp) / 1000L);
    }

    public void resetPlayer(Player player) {
        player.getInventory().clear();
        player.clearActivePotionEffects();
        resetPlayerMaxHealth(player);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setExp(0.0f);
        player.setLevel(0);
        player.setTotalExperience(0);
    }

    private void resetPlayerMaxHealth(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) return;

        attribute.setBaseValue(20.0);
        if (player.getHealth() > 20.0) {
            player.setHealth(20.0);
        }
    }
}
