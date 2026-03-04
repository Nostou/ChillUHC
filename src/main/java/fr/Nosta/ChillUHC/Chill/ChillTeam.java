package fr.Nosta.ChillUHC.Chill;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChillTeam {

    private final String name;
    private final NamedTextColor color;
    private final Team team;

    private final Set<UUID> players = new HashSet<>();

    public ChillTeam(String name, NamedTextColor color, Team scoreboardTeam) {
        this.name = name;
        this.color = color;
        this.team = scoreboardTeam;
    }

    public String getName() { return name; }
    public NamedTextColor getColor() { return color; }
    public Team getTeam() { return team; }
    public Set<UUID> getPlayers() { return players; }
    public void addPlayer(UUID uuid) {
        players.add(uuid);
    }
    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }
}