package fr.Nosta.ChillUHC;

import fr.Nosta.ChillUHC.Commands.*;
import fr.Nosta.ChillUHC.Listeners.*;
import fr.Nosta.ChillUHC.Managers.*;
import fr.Nosta.ChillUHC.Scenarios.BetaZombiesScenario;
import fr.Nosta.ChillUHC.Scenarios.ChillReviveScenario;
import fr.Nosta.ChillUHC.Scenarios.CutCleanScenario;
import fr.Nosta.ChillUHC.Scenarios.HasteyBoysScenario;
import fr.Nosta.ChillUHC.Scenarios.IronmanScenario;
import fr.Nosta.ChillUHC.Scenarios.AnonymousScenario;
import fr.Nosta.ChillUHC.Scenarios.TimberScenario;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class Main extends JavaPlugin
{
    private World world;
    private final Map<Class<?>, Object> managers = new HashMap<>();

    @Override
    public void onEnable() {
        world = Bukkit.getWorld("world");
        if (world == null) {
            getLogger().severe("Unable to find world 'world'. Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        registerManagers();
        registerListeners();
        if (!registerCommands()) {
            getLogger().severe("Unable to register command 'hf'. Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //Wait for other plugins to load such as UHC_GENERATION (one tick)
        Bukkit.getScheduler().runTask(this, () -> {
            getManager(GameManager.class).initGame();
            getScenarioManager().initializeRegisteredScenarios();
        });
    }

    private void registerManagers()
    {
        registerManager(new CompassManager(this));
        registerManager(new GameManager(this));
        registerManager(new BorderManager(this));
        registerManager(new InventoryManager(this));
        registerManager(new ScenarioManager(this));
        registerManager(new ReviveManager(this));
        registerManager(new ScoreboardManager(this));
        registerManager(new TabManager(this));
        registerManager(new TeamManager(this));
        registerManager(new TierManager(this));
    }

    private <T> void registerManager(T manager)
    {
        managers.put(manager.getClass(), manager);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new AppleDropListener(this), this);
        getServer().getPluginManager().registerEvents(new BorderListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ConnexionListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new DiamondListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new PvPListener(this), this);

        registerScenarioListener(new AnonymousScenario(this));
        registerScenarioListener(new BetaZombiesScenario(this));
        registerScenarioListener(new ChillReviveScenario(this));
        registerScenarioListener(new CutCleanScenario(this));
        registerScenarioListener(new HasteyBoysScenario(this));
        registerScenarioListener(new IronmanScenario(this));
        registerScenarioListener(new TimberScenario(this));
    }

    private void registerScenarioListener(fr.Nosta.ChillUHC.Scenarios.Scenario scenario) {
        getScenarioManager().registerScenario(scenario);
        if (scenario instanceof org.bukkit.event.Listener listener) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    private boolean registerCommands() {
        PluginCommand hfCommand = getCommand("hf");
        if (hfCommand == null) return false;

        hfCommand.setExecutor(new GameCommands(this));
        hfCommand.setTabCompleter(new CommandCompleter());
        return true;
    }

    public <T> T getManager(Class<T> c)
    {
        return c.cast(managers.get(c));
    }
    public BorderManager getBorderManager() { return getManager(BorderManager.class); }
    public CompassManager getCompassManager() { return getManager(CompassManager.class); }
    public GameManager getGameManager() { return getManager(GameManager.class); }
    public InventoryManager getInventoryManager() { return getManager(InventoryManager.class); }
    public ReviveManager getReviveManager() { return getManager(ReviveManager.class); }
    public ScenarioManager getScenarioManager() { return getManager(ScenarioManager.class); }
    public ScoreboardManager getScoreboardManager() { return getManager(ScoreboardManager.class); }
    public TabManager getTabManager() { return getManager(TabManager.class); }
    public TeamManager getTeamManager() { return getManager(TeamManager.class); }
    public TierManager getTierManager() { return getManager(TierManager.class); }
    public World getWorld() { return world; }
    public Location getSpawnLocation() { return world.getSpawnLocation().clone().add(0.5, 0, 0.5); }
}
