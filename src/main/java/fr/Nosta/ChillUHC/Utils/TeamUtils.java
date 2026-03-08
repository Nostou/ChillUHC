package fr.Nosta.ChillUHC.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public final class TeamUtils {

    public static List<Player> getPlayers(Team team) {
        List<Player> players = new ArrayList<>();

        for (String entry : team.getEntries()) {
            Player player = Bukkit.getPlayerExact(entry);
            if (player != null) players.add(player);
        }

        return players;
    }
}