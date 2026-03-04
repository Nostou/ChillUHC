package fr.Nosta.ChillUHC;

import fr.Nosta.ChillUHC.Commands.*;
import fr.Nosta.ChillUHC.Listeners.*;
import fr.Nosta.ChillUHC.Managers.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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

        registerManagers();
        registerListeners();
        registerCommands();

        //Wait for other plugins to load such as UHC_GENERATION
        Bukkit.getScheduler().runTask(this, () -> {
            getManager(GameManager.class).initGame();
        });
    }

    private void registerManagers()
    {
        registerManager(new BorderManager(this));
        registerManager(new CompassManager(this));
        registerManager(new GameManager(this));
        registerManager(new InventoryManager(this));
        registerManager(new PlayerManager(this));
        registerManager(new ScoreboardManager(this));
        registerManager(new TabManager(this));
        registerManager(new TeamManager(this));
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
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new PvPListener(this), this);
    }

    private void registerCommands() {
        this.getCommand("hf").setExecutor(new GameCommands(this));
        this.getCommand("hf").setTabCompleter(new CommandCompleter(this));
    }

    public <T> T getManager(Class<T> c)
    {
        return c.cast(managers.get(c));
    }
    public World getWorld() { return world; }
    public Location getSpawnLocation() { return world.getSpawnLocation().clone().add(0.5, 0, 0.5); }
}