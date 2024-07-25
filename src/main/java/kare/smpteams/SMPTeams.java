package kare.smpteams;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SMPTeams extends JavaPlugin implements Listener, CommandExecutor {
    ArrayList<Teams> teamsList;
    List<UUID> inTeamChat = new ArrayList<>();
    private static SMPTeams instance;
    private final Command commandInstance = new Command(this);
    private static final Gson gson = new Gson();

    public static SMPTeams getInstance() {
        return instance;
    }

    File teamsFile = new File(getDataFolder(), "teams");
    File teamsJSON = new File(getDataFolder(), "teams.json");

    @Override
    public void onEnable() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        instance = this;
        getCommand("smpteams").setExecutor(commandInstance);
        getCommand("smpteams").setTabCompleter(new TabComplete());
        getCommand("teamchat").setExecutor(commandInstance);
        getServer().getPluginManager().registerEvents(this, this);
        // Plugin startup logic

        if (teamsJSON.exists() && teamsJSON.length() != 0) {
            try {
                var fis = new FileReader(teamsJSON);
                var jsonReader = gson.newJsonReader(fis);

                Type listType = new TypeToken<ArrayList<Teams>>(){}.getType();
                teamsList = gson.fromJson(jsonReader, listType);

                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (teamsFile.exists() && teamsFile.length() != 0) {
            try {
                var fis = new FileInputStream(teamsFile);
                var ois = new ObjectInputStream(fis);

                teamsList = (ArrayList<Teams>) ois.readObject();

                ois.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            teamsList = new ArrayList<>();
        }
    }

    @Override
    public void onDisable() {
        if (!teamsJSON.exists()) {
            try {
                teamsJSON.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            var fos = new FileOutputStream(teamsJSON);
            var pw = new PrintWriter(fos);

            pw.write(gson.toJson(teamsList));

            pw.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Teams getPlayerTeam(UUID p) {
        for (Teams t : teamsList) {
            if (t.containsPlayer(p))
                return t;
        }
        return null;
    }

    public List<Teams> getTeamsList() {
        return teamsList;
    }

    public List<UUID> getToggledTeamChat() {
        return inTeamChat;
    }

    public Component createTeamMessage(Teams t, Player p, Component message) {
        var team = "[Team] ";
        String sender = team + p.getName();

        return Component.text(sender)
                .color(TextColor.color(t != null ? (int) t.color : 0xFFFFFF))
                .append(Component.text(" » ", NamedTextColor.WHITE))
                .append(message.color(NamedTextColor.BLUE))
                .clickEvent(message.clickEvent());
    }

    public Component createPublicMessage(Teams t, Player p, Component message) {
        var team = "";
        if (t != null)
            team = "[" + t.name + "] ";

        String sender = team + p.getName();

        return Component.text(sender)
                .color(TextColor.color(t != null ? (int) t.color : 0xFFFFFF))
                .append(Component.text(" » ", NamedTextColor.WHITE))
                .append(message.color(NamedTextColor.WHITE))
                .clickEvent(message.clickEvent());
    }

    @EventHandler
    public void onAsyncChatEvent(AsyncChatEvent e) {
        UUID p = e.getPlayer().getUniqueId();
        var t = getPlayerTeam(p);

        e.setCancelled(true);

        Audience a;

        var toggleTC = inTeamChat.contains(p);
        Component msg;

        if (toggleTC && t != null) {
            a = Audience.audience(t.getAudience(), getServer().getConsoleSender());
            msg = createTeamMessage(t, e.getPlayer(), e.message());
        } else {
            a = Audience.audience(e.viewers());
            msg = createPublicMessage(t, e.getPlayer(), e.message());
        }
        a.sendMessage(msg);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        var p = e.getPlayer();
        var puid = p.getUniqueId();
        var t = getPlayerTeam(puid);

        var team = "";
        if (t != null)
            team = "[" + t.name + "] ";

        int color = t == null ? Integer.decode("0xFFFFFF") : (int) t.color;

        e.getPlayer().playerListName(Component.text(team + p.getName()).color(TextColor.color(color)));
    }
}
