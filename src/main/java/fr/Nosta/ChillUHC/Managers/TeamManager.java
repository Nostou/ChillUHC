package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import fr.Nosta.ChillUHC.Utils.SimpleEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class TeamManager {

    public final SimpleEvent<Player> onTeamChanged = new SimpleEvent<>();

    private final Main plugin;
    private final Scoreboard scoreboard;

    private final Map<String, Team> teams = new HashMap<>();
    private final Map<String, Team> playerTeams = new HashMap<>();

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

        loadExistingTeams();
    }

    private void createTeam(String name, NamedTextColor color) {
        Team team = scoreboard.getTeam(name);
        if (team == null) team = scoreboard.registerNewTeam(name);

        team.color(color);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);

        teams.put(name, team);
    }

    private void loadExistingTeams() {
        for (Team team : scoreboard.getTeams()) {
            if (!teams.containsKey(team.getName())) continue;

            for (String entry : team.getEntries()) {
                playerTeams.put(entry, team);
            }
        }
    }

    public void setPlayerTeam(Player player, String teamName) {
        Team previousTeam = playerTeams.get(player.getName());
        Team newTeam = teamName != null ? teams.get(teamName) : null;

        if (previousTeam != null) {
            previousTeam.removeEntry(player.getName());
        }

        if (newTeam != null) {
            newTeam.addEntry(player.getName());
            playerTeams.put(player.getName(), newTeam);

            Component msg = Component.text("You are now ", NamedTextColor.WHITE)
                    .append(Component.text(newTeam.getName(), newTeam.color()));

            CustomMessage.custom(player, NamedTextColor.YELLOW, msg);
        }
        else {
            playerTeams.remove(player.getName());
            Component msg = Component.text("You left your team", NamedTextColor.WHITE);
            CustomMessage.custom(player, NamedTextColor.YELLOW, msg);
        }

        player.setScoreboard(scoreboard);
        onTeamChanged.invoke(player);
    }

    public Team getTeam(String teamName) { return teams.get(teamName); }
    public Team getTeam(Player player) { return playerTeams.get(player.getName()); }
    public List<Team> getAllTeams() { return teams.values().stream().toList(); }

    public NamedTextColor getColor(Player player) {
        Team team = playerTeams.get(player.getName());
        return team != null ? (NamedTextColor)team.color() : NamedTextColor.WHITE;
    }

    public void setNameTagsVisible(boolean visible) {
        Team.OptionStatus status = visible ? Team.OptionStatus.ALWAYS : Team.OptionStatus.NEVER;
        for (Team team : teams.values()) {
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, status);
        }
    }
}
