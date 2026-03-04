package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Chill.ChillPlayer;
import fr.Nosta.ChillUHC.Main;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final Main plugin;

    private final Map<UUID, ChillPlayer> players = new HashMap<>();

    public PlayerManager(Main plugin) {
        this.plugin = plugin;
    }

    public void addChillPlayer(Player player) {
        ChillPlayer chillPlayer = new ChillPlayer(player);
        players.put(player.getUniqueId(), chillPlayer);
    }

    public void removeChillPlayer(Player player) {
        ChillPlayer chillPlayer = getChillPlayer(player);
        chillPlayer.setTeam(null);
        players.remove(player.getUniqueId());
    }

    public ChillPlayer getChillPlayer(Player player) {
        return players.get(player.getUniqueId());
    }
}