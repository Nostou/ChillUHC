package fr.Nosta.ChillUHC.Listeners;

import fr.Nosta.ChillUHC.Enums.GameState;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Managers.GameManager;
import fr.Nosta.ChillUHC.Managers.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnexionListener implements Listener {

    private final Main plugin;

    public ConnexionListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getManager(PlayerManager.class).addChillPlayer(player);

        if (plugin.getManager(GameManager.class).getState() != GameState.PLAYING) {
            player.teleport(plugin.getSpawnLocation());
            player.setGameMode(GameMode.ADVENTURE);
        }

        AttributeInstance attackSpeed = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attackSpeed != null) attackSpeed.setBaseValue(1024.0);

        Component message = Component.text("[", NamedTextColor.DARK_GRAY)
                .append(Component.text("+", NamedTextColor.GREEN))
                .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                .append(Component.text(player.getName(), NamedTextColor.WHITE));

        event.joinMessage(message);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getManager(PlayerManager.class).removeChillPlayer(player);

        Component message = Component.text("[", NamedTextColor.DARK_GRAY)
                .append(Component.text("-", NamedTextColor.RED))
                .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                .append(Component.text(player.getName(), NamedTextColor.WHITE));

        event.quitMessage(message);
    }
}
