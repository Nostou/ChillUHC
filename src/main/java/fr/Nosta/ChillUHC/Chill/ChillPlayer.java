package fr.Nosta.ChillUHC.Chill;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ChillPlayer {

    private final UUID uuid;
    private ChillTeam team;

    public ChillPlayer(Player player) {
        this.uuid = player.getUniqueId();
    }

    public void setTeam(ChillTeam newTeam) { team = newTeam; }
    public ChillTeam getTeam() { return team; }
    public NamedTextColor getColor() { return team != null ? team.getColor() : NamedTextColor.WHITE; }
}