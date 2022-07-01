package kare.smpteams;

import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Teams implements Serializable{
    String owner;
    String name;
    long color;
    ArrayList<String> players = new ArrayList<>();
    ArrayList<String> canInvite = new ArrayList<>();
    ArrayList<String> canKick = new ArrayList<>();
    transient ArrayList<String> invited;

    Teams(UUID owner, String name, long color){
        this.owner = owner.toString();
        this.name = name;
        this.color = color;
        this.invited = new ArrayList<>();
    }

    public void addPlayer(UUID p) {
        if (invited == null)
            invited = new ArrayList<>();
        var pstring = p.toString();
        if (invited.contains(pstring)) {
            players.add(pstring);
            invited.remove(pstring);
        }
    }


    public List<String> getPlayers() {
        return players;
    }

    public void setCanInvite(UUID p) {
        canInvite.add(p.toString());
    }

    public void removeCanInvite(UUID p) {
        canInvite.remove(p.toString());
    }

    public void setCanKick(UUID p) {
        canKick.add(p.toString());
    }

    public void removeCanKick(UUID p) {
        canKick.remove(p.toString());
    }

    public void invitePlayer(UUID p) {
        if (invited == null)
            invited = new ArrayList<>();
        invited.add(p.toString());
    }

    public boolean containsPlayer(UUID p) {
        var pstring = p.toString();
        return pstring.equals(owner) || players.contains(pstring);
    }

    public boolean playerCanInvite(UUID p) {
        var pstring = p.toString();
        return canInvite.contains(pstring);
    }
    public boolean playerCanKick(UUID p) {
        var pstring = p.toString();
        return canKick.contains(pstring);
    }


    public boolean invitedPlayer(UUID p) {
        if (invited == null)
            invited = new ArrayList<>();
        return invited.contains(p.toString());
    }

    public void removePlayer(UUID p) {
        players.remove(p.toString());
    }

    public boolean disband() {
        return players.isEmpty();
    }

    public Audience getAudience() {
        List<Audience> audiences = new ArrayList<>();

        var own = Bukkit.getPlayer(UUID.fromString(owner));
        if (own != null && own.isOnline())
            audiences.add(own);

        players.forEach(p -> {
            var player = Bukkit.getPlayer(UUID.fromString(p));
            if (player != null && player.isOnline())
                audiences.add(player);
        });

        return Audience.audience(audiences);
    }
}
