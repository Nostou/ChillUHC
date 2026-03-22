package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.Spreader;
import fr.Nosta.ChillUHC.Utils.TeamUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReviveManager {

    private static final int MIN_SPAWN_DISTANCE = 50;

    private final Main plugin;
    private final Set<UUID> deadPlayers = ConcurrentHashMap.newKeySet();

    public ReviveManager(Main plugin) {
        this.plugin = plugin;
    }

    public void recordDeath(Player player) {
        deadPlayers.add(player.getUniqueId());
    }

    public boolean hasDeathState(Player player) {
        return deadPlayers.contains(player.getUniqueId());
    }

    public boolean revive(Player player) {
        if (!deadPlayers.remove(player.getUniqueId())) return false;

        Location respawnLocation = getRespawnLocation(player);
        plugin.getGameManager().resetPlayer(player);
        player.teleport(respawnLocation);
        player.setGameMode(GameMode.SURVIVAL);

        return true;
    }

    private Location getRespawnLocation(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        List<Player> livingTeammates = getLivingTeammates(team, player);

        if (!livingTeammates.isEmpty()) {
            int index = (int) (Math.random() * livingTeammates.size());
            return livingTeammates.get(index).getLocation().clone();
        }

        return getRandomSpawnLocation();
    }

    private List<Player> getLivingTeammates(Team team, Player revivedPlayer) {
        if (team == null) return List.of();

        return TeamUtils.getPlayers(team).stream()
                .filter(teammate -> !teammate.getUniqueId().equals(revivedPlayer.getUniqueId()))
                .filter(teammate -> teammate.getGameMode() == GameMode.SURVIVAL)
                .toList();
    }

    private Location getRandomSpawnLocation() {
        World world = plugin.getWorld();
        Location center = plugin.getSpawnLocation();
        int radius = plugin.getBorderManager().getCurrentRadius();
        List<Location> locations = Spreader.generate(center, radius, 1, MIN_SPAWN_DISTANCE);

        if (!locations.isEmpty()) return locations.getFirst();

        int y = world.getHighestBlockYAt(center);
        return center.clone().set(center.getX(), y + 1, center.getZ());
    }
}
