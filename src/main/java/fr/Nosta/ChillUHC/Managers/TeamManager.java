package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Chill.ChillPlayer;
import fr.Nosta.ChillUHC.Chill.ChillTeam;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;

public class TeamManager {

    private final Main plugin;

    private final Scoreboard scoreboard;

    private final Map<String, ChillTeam> teams = new HashMap<>();

    public TeamManager(Main plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        createTeam("Blue", NamedTextColor.BLUE);
        createTeam("Cyan", NamedTextColor.AQUA);
        createTeam("Green", NamedTextColor.GREEN);
        createTeam("Orange", NamedTextColor.GOLD);
        createTeam("Pink", NamedTextColor.LIGHT_PURPLE);
        createTeam("Purple", NamedTextColor.DARK_PURPLE);
        createTeam("Red", NamedTextColor.RED);
        createTeam("Yellow", NamedTextColor.YELLOW);
    }

    private void createTeam(String name, NamedTextColor color) {
        Team team = scoreboard.getTeam(name);
        if (team == null) team = scoreboard.registerNewTeam(name);

        team.color(color);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

        ChillTeam chillTeam = new ChillTeam(name, color, team);
        teams.put(name, chillTeam);
    }

    public ChillTeam getChillTeam(String teamName) { return teams.get(teamName); }

    public void setPlayerTeam(Player player, ChillTeam newTeam) {
        PlayerManager playerManager = plugin.getManager(PlayerManager.class);
        ChillPlayer chillPlayer = playerManager.getChillPlayer(player);

        ChillTeam previousTeam = chillPlayer.getTeam();

        if (previousTeam != null) {
            previousTeam.removePlayer(player.getUniqueId());
            previousTeam.getTeam().removeEntry(player.getName());
        }

        chillPlayer.setTeam(newTeam);

        if (newTeam != null) {
            newTeam.addPlayer(player.getUniqueId());
            newTeam.getTeam().addEntry(player.getName());
            Component msg = Component.text("You are now ", NamedTextColor.WHITE)
                    .append(Component.text(newTeam.getName(), newTeam.getColor()));
            CustomMessage.custom(player, NamedTextColor.YELLOW, msg);
        }
        else {
            Component msg = Component.text("You left your team", NamedTextColor.WHITE);
            CustomMessage.custom(player, NamedTextColor.YELLOW, msg);
        }

        player.setScoreboard(scoreboard);
    }
}