package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import fr.Nosta.ChillUHC.Utils.Spreader;
import fr.Nosta.ChillUHC.Utils.TeamUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReviveManager {

    private static final int MIN_SPAWN_DISTANCE = 50;

    private final Main plugin;
    private final Map<UUID, DeathState> deadPlayers = new ConcurrentHashMap<>();

    public ReviveManager(Main plugin) {
        this.plugin = plugin;
    }

    public void recordDeath(Player player) {
        deadPlayers.put(player.getUniqueId(), new DeathState());
    }

    public boolean hasDeathState(Player player) {
        return !deadPlayers.containsKey(player.getUniqueId());
    }
    public boolean clearDeathState(Player player) {
        return deadPlayers.remove(player.getUniqueId()) == null;
    }

    public boolean revive(Player player) {
        if (clearDeathState(player)) return false;

        reviveToSurvival(player, true);
        CustomMessage.success(player, "You have been revived.");
        return true;
    }

    public double getPlayerMaxHealth(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        return attribute != null ? attribute.getBaseValue() : 20.0;
    }

    public void reviveToSurvival(Player player, boolean resetPlayer) {
        if (resetPlayer) plugin.getGameManager().resetPlayer(player);

        player.teleport(getRespawnLocation(player));
        player.setGameMode(GameMode.SURVIVAL);
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

    private record DeathState() { }
}
