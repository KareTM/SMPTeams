package kare.smpteams;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Command implements CommandExecutor {

    SMPTeams plugin;

    Command(SMPTeams plugin) {
        this.plugin = plugin;
    }

    static boolean isValidHexaCode(String str) {
        if (str.charAt(0) != '#')
            return false;

        if (str.length() != 7)
            return false;

        for (var i = 1; i < str.length(); i++)
            if (!((str.charAt(i) >= '0' && str.charAt(i) <= 9)
                    || (str.charAt(i) >= 'a' && str.charAt(i) <= 'f')
                    || (str.charAt(i) >= 'A' || str.charAt(i) <= 'F')))
                return false;

        return true;
    }

    String notTeam = "Not in a Team.";
    String noPerms = "You don't have the perms to do that.";
    String playerNotOnline = "No player with that name was found. Are they online?";
    String invite = "invite";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player))
            return false;

        if (command.getName().equals("smpteams")) {
            if (args.length == 0)
                return displayTeam(sender);

            switch (args[0]) {
                case "create":
                    if (args.length < 2) {
                        sender.sendMessage(Component.text("Please select a team name.")
                                .color(NamedTextColor.RED));
                        return true;
                    } else if (args.length < 3) {
                        sender.sendMessage(Component.text("Please select a team color. Format: #000000.")
                                .color(NamedTextColor.RED));
                        return true;
                    }
                    if (args[1].length() > 16) {
                        sender.sendMessage(Component.text("Team Name may not be longer than 16 chars")
                                .color(NamedTextColor.RED));
                    } else {
                        if (!isValidHexaCode(args[2])) {
                            sender.sendMessage(Component.text("Hex Code is invalid. Format: #00AAFF. Valid values [0-9] & [A-F].")
                                    .color(NamedTextColor.RED));
                            return true;
                        }

                        var teamList = SMPTeams.getInstance().getTeamsList();
                        var p = ((Player) sender).getUniqueId();
                        if (plugin.getPlayerTeam(p) != null) {
                            sender.sendMessage(Component.text("Already in a Team. Leave your current team to create a new one.")
                                    .color(NamedTextColor.RED));
                            return true;
                        }
                        sender.sendMessage(Component.text(args[1] + " has been created.")
                                .color(NamedTextColor.GREEN));

                        String team = "[" + args[1] + "] ";

                        int color = Integer.decode(args[2]);
                        ((Player) sender).playerListName(Component.text(team + sender.getName()).color(TextColor.color(color)));
                        teamList.add(new Teams(p, args[1], Long.decode(args[2])));
                    }
                    return true;
                case "delete": {
                    var teamList = SMPTeams.getInstance().getTeamsList();
                    var p = ((Player) sender).getUniqueId();
                    var t = plugin.getPlayerTeam(p);
                    if (t == null) {
                        sender.sendMessage(Component.text(notTeam)
                                .color(NamedTextColor.RED));
                        return true;
                    }

                    if (!t.owner.equals(p.toString())) {
                        sender.sendMessage(Component.text(noPerms)
                                .color(NamedTextColor.RED));
                    } else {
                        if (!t.disband()) {
                            sender.sendMessage(Component.text("Team still has members in it.")
                                    .color(NamedTextColor.RED));
                        } else {
                            sender.sendMessage(Component.text("Team has been deleted.")
                                    .color(NamedTextColor.GREEN));
                            ((Player) sender).playerListName(Component.text(sender.getName()).color(NamedTextColor.WHITE));
                            teamList.remove(t);
                        }
                    }
                    return true;
                }
                case "help":
                    sender.sendMessage(Component.text("SMPTeams by Kare").color(NamedTextColor.RED));
                    sender.sendMessage(Component.text("Aliases: /t, /teams, /smpteams").color(NamedTextColor.RED));
                    sender.sendMessage(Component.text("------------------------------").color(NamedTextColor.RED));
                    sender.sendMessage(Component.text("/t create <name> <hexcolor>").color(NamedTextColor.RED));
                    sender.sendMessage(Component.text("/t delete").color(NamedTextColor.RED));
                    sender.sendMessage(Component.text("/t help").color(NamedTextColor.RED));
                    sender.sendMessage(Component.text("/t invite <player>").color(NamedTextColor.RED));
                    sender.sendMessage(Component.text("/t join <team>").color(NamedTextColor.RED));
                    sender.sendMessage(Component.text("/t kick <player>").color(NamedTextColor.RED));
                    sender.sendMessage(Component.text("/t leave").color(NamedTextColor.RED));
                    sender.sendMessage(Component.text("/t list").color(NamedTextColor.RED));
                    sender.sendMessage(Component.text("/t perms add/remove invite/kick <player>").color(NamedTextColor.RED));
                    sender.sendMessage(Component.text("------------------------------").color(NamedTextColor.RED));
                    return true;
                case "invite":
                    if (args.length < 2) {
                        sender.sendMessage(Component.text("Specify which player to invite.")
                                .color(NamedTextColor.RED));
                    } else {
                        var o = ((Player) sender).getUniqueId();
                        var t = plugin.getPlayerTeam(o);
                        if (nullOrNoPerms(sender, o, t, true, false)) return true;

                        var p = getUUIDOfPlayer(args[1]);

                        if (p == null) {
                            sender.sendMessage(Component.text(playerNotOnline)
                                    .color(NamedTextColor.RED));
                        } else {
                            if (p == o) {
                                sender.sendMessage(Component.text("You can't invite yourself.")
                                        .color(NamedTextColor.RED));
                            } else if (t.containsPlayer(p)) {
                                    sender.sendMessage(Component.text("You can't invite a player that already is in team.")
                                            .color(NamedTextColor.RED));
                            } else {
                                sender.sendMessage(Component.text(args[1] + " has been invited.")
                                        .color(NamedTextColor.GREEN));
                                Bukkit.getPlayer(p).sendMessage(Component.text("You have been invited to " + t.name + '.')
                                        .color(NamedTextColor.GREEN));
                                t.invitePlayer(p);

                            }
                        }
                    }
                    return true;
                case "join":
                    if (args.length < 2) {
                        sender.sendMessage(Component.text("Specify which team to join by writing their name.")
                                .color(NamedTextColor.RED));
                    } else {
                        var teamList = SMPTeams.getInstance().getTeamsList();
                        var p = ((Player) sender).getUniqueId();
                        var t = plugin.getPlayerTeam(p);
                        if (t != null) {
                            sender.sendMessage(Component.text("Already in a Team. Leave current team to join new one.")
                                    .color(NamedTextColor.RED));
                            return true;
                        }

                        for (Teams team : teamList) {
                            if (team.name.equals(args[1])) {
                                t = team;
                                break;
                            }
                        }

                        if (t == null) {
                            sender.sendMessage(Component.text("No team with that name exists.")
                                    .color(NamedTextColor.RED));
                        } else {
                            if (!t.invitedPlayer(p)) {
                                sender.sendMessage(Component.text("You are not invited to that team.")
                                        .color(NamedTextColor.RED));
                            } else {
                                sender.sendMessage(Component.text("You joined " + args[1] + '.')
                                        .color(NamedTextColor.GREEN));
                                t.getAudience().sendMessage(Component.text(sender.getName() + " has joined your team.")
                                        .color(NamedTextColor.GREEN));

                                String team = "[" + t.name + "] ";

                                int color = (int) t.color;
                                ((Player) sender).playerListName(Component.text(team + sender.getName()).color(TextColor.color(color)));
                                t.addPlayer(p);
                            }
                        }
                    }
                    return true;
                case "kick":
                    if (args.length < 2) {
                        sender.sendMessage(Component.text("Specify which player to kick.")
                                .color(NamedTextColor.RED));
                    } else {
                        var o = ((Player) sender).getUniqueId();
                        var t = plugin.getPlayerTeam(o);
                        if (nullOrNoPerms(sender, o, t, false, true)) return true;

                        var p = getUUIDOfPlayer(args[1]);

                        if (p == null) {
                            sender.sendMessage(Component.text(playerNotOnline)
                                    .color(NamedTextColor.RED));
                        } else {
                            if (p == o) {
                                sender.sendMessage(Component.text("You can't kick yourself.")
                                        .color(NamedTextColor.RED));
                            } else if (!t.containsPlayer(p)) {
                                sender.sendMessage(Component.text("Player not in your team.")
                                        .color(NamedTextColor.RED));
                            } else if (t.owner.equals(p.toString())) {
                                sender.sendMessage(Component.text("You can't kick the owner.")
                                        .color(NamedTextColor.RED));
                            } else {
                                Bukkit.getPlayer(p).sendMessage(Component.text("You have been kicked from " + t.name + ".")
                                        .color(NamedTextColor.GREEN));
                                t.removeCanKick(p);
                                t.removeCanInvite(p);
                                t.removePlayer(p);
                                t.getAudience().sendMessage(Component.text(args[1] + " has been kicked from the team.")
                                        .color(NamedTextColor.GREEN));
                                var kicked = Bukkit.getPlayer(p);
                                kicked.playerListName(Component.text(kicked.getName()).color(NamedTextColor.WHITE));
                                plugin.getToggledTeamChat().remove(p);
                            }
                        }
                    }
                    return true;
                case "leave": {
                    var p = ((Player) sender).getUniqueId();
                    var t = plugin.getPlayerTeam(p);
                    if (t == null) {
                        sender.sendMessage(Component.text(notTeam)
                                .color(NamedTextColor.RED));
                    } else {
                        if (p.toString().equals(t.owner)) {
                            sender.sendMessage(Component.text("You can't leave your team as a owner. Use delete instead.")
                                    .color(NamedTextColor.RED));
                        } else {
                            sender.sendMessage(Component.text("You left your team.")
                                    .color(NamedTextColor.GREEN));
                            t.removeCanKick(p);
                            t.removeCanInvite(p);
                            t.removePlayer(p);
                            t.getAudience().sendMessage(Component.text(sender.getName() + " has left your team.")
                                    .color(NamedTextColor.GREEN));
                            ((Player) sender).playerListName(Component.text(sender.getName()).color(NamedTextColor.WHITE));
                            plugin.getToggledTeamChat().remove(p);
                        }
                    }
                    return true;
                }
                case "list": {
                    var p = ((Player) sender).getUniqueId();
                    var t = plugin.getPlayerTeam(p);
                    if (t == null) {
                        sender.sendMessage(Component.text(notTeam)
                                .color(NamedTextColor.RED));
                    } else {
                        var owner = UUID.fromString(t.owner);
                        sender.sendMessage(Component.text("Owner: " + Bukkit.getOfflinePlayer(owner).getName())
                                .color(NamedTextColor.GREEN));
                        if (t.getPlayers().isEmpty())
                            return true;

                        var members = new StringBuilder();
                        members.append("Members: ");
                        t.getPlayers().forEach(player -> {
                            var puid = UUID.fromString(player);
                            members.append(Bukkit.getOfflinePlayer(puid).getName());
                            members.append(", ");
                        });
                        members.delete(members.lastIndexOf(","), members.length());
                        sender.sendMessage(Component.text(members.toString())
                                .color(NamedTextColor.GREEN));
                    }
                    return true;
                }
                case "perms":
                    if (args.length < 4) {
                        sender.sendMessage(Component.text("Usage: /t perms add/remove invite/kick <player>")
                                .color(NamedTextColor.RED));
                    } else {
                        var o = ((Player) sender).getUniqueId();
                        var t = plugin.getPlayerTeam(o);
                        if (nullOrNoPerms(sender, o, t, false, false)) return true;

                        var p = getUUIDOfPlayer(args[3]);

                        if (p == null) {
                            sender.sendMessage(Component.text(playerNotOnline)
                                    .color(NamedTextColor.RED));
                        } else {
                            if (p == o) {
                                sender.sendMessage(Component.text("You can't change your perms.")
                                        .color(NamedTextColor.RED));
                            } else if (!t.containsPlayer(p)) {
                                sender.sendMessage(Component.text("Player not in your team.")
                                        .color(NamedTextColor.RED));
                            } else {
                                if (args[1].equals("add")) {
                                    if (args[2].equals(invite)) {
                                        if (t.playerCanInvite(p)) {
                                            sender.sendMessage(Component.text("Player already can invite.")
                                                    .color(NamedTextColor.RED));
                                        } else {
                                            sender.sendMessage(Component.text(args[3] + " can invite people to the team.")
                                                    .color(NamedTextColor.GREEN));
                                            Bukkit.getPlayer(p).sendMessage(Component.text("You can invite people to the team.")
                                                    .color(NamedTextColor.GREEN));
                                            t.setCanInvite(p);
                                        }
                                    } else if (args[2].equals("kick")) {
                                        if (t.playerCanKick(p)) {
                                            sender.sendMessage(Component.text("Player already can kick.")
                                                    .color(NamedTextColor.RED));
                                        } else {
                                            sender.sendMessage(Component.text(args[3] + " can kick people from the team.")
                                                    .color(NamedTextColor.GREEN));
                                            Bukkit.getPlayer(p).sendMessage(Component.text("You can kick people from the team.")
                                                    .color(NamedTextColor.GREEN));
                                            t.setCanKick(p);
                                        }
                                    }
                                } else if (args[1].equals("remove")) {
                                    if (args[2].equals(invite)) {
                                        if (!t.playerCanInvite(p)) {
                                            sender.sendMessage(Component.text("Player already can't invite.")
                                                    .color(NamedTextColor.RED));
                                        } else {
                                            sender.sendMessage(Component.text(args[3] + " can no longer invite people to the team.")
                                                    .color(NamedTextColor.GREEN));
                                            Bukkit.getPlayer(p).sendMessage(Component.text("You can no longer invite people to the team.")
                                                    .color(NamedTextColor.GREEN));
                                            t.removeCanInvite(p);
                                        }
                                    } else if (args[2].equals("kick")) {
                                        if (!t.playerCanKick(p)) {
                                            sender.sendMessage(Component.text("Player already can't kick.")
                                                    .color(NamedTextColor.RED));
                                        } else {
                                            sender.sendMessage(Component.text(args[3] + " can no longer kick people from the team.")
                                                    .color(NamedTextColor.GREEN));
                                            Bukkit.getPlayer(p).sendMessage(Component.text("You can no longer kick people from the team.")
                                                    .color(NamedTextColor.GREEN));
                                            t.removeCanKick(p);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return true;
                case "reset":
                    if (!sender.isOp())
                        return false;
                    if (args.length < 2) {
                        sender.sendMessage(Component.text("Please select a team to reset.")
                                .color(NamedTextColor.RED));
                    } else {
                        var teamList = SMPTeams.getInstance().getTeamsList();
                        Teams t = null;

                        for (Teams team : teamList) {
                            if (team.name.equals(args[1])) {
                                t = team;
                                break;
                            }
                        }

                        if (t == null) {
                            sender.sendMessage(Component.text("No team with that name exists.")
                                    .color(NamedTextColor.RED));
                        } else {
                            sender.sendMessage(Component.text("You forcefully disbanded " + args[1] + '.')
                                    .color(NamedTextColor.GREEN));
                            teamList.remove(t);
                        }
                    }
                    return true;
                default:
                    return displayTeam(sender);
            }
        } else if (command.getName().equals("teamchat")) {
            var p = ((Player) sender).getUniqueId();
            var t = plugin.getPlayerTeam(p);
            if (t == null) {
                sender.sendMessage(Component.text(notTeam)
                        .color(NamedTextColor.RED));
            } else {
                if (args.length >= 1) {
                    var msg = new StringBuilder();
                    for(String s : args) {
                        msg.append(s).append(" ");
                    }

                    var message = Component.text(msg.toString());
                    t.getAudience().sendMessage(SMPTeams.getInstance().createTeamMessage(t, (Player) sender, message));
                    return true;
                }

                var toggled = SMPTeams.getInstance().getToggledTeamChat();
                if (toggled.contains(p)) {
                    sender.sendMessage(Component.text("You are talking in general chat.")
                            .color(NamedTextColor.GREEN));
                    toggled.remove(p);
                } else {
                    sender.sendMessage(Component.text("You are talking in team chat.")
                            .color(NamedTextColor.GREEN));
                    toggled.add(((Player) sender).getUniqueId());
                }


            }
        }
        return true;
    }

    private boolean nullOrNoPerms(@NotNull CommandSender sender, UUID o, Teams t, boolean invite, boolean kick) {
        if (t == null) {
            sender.sendMessage(Component.text(notTeam)
                    .color(NamedTextColor.RED));
            return true;
        }

        if (!t.owner.equals(o.toString())) {
            if (invite) {
                if (!t.playerCanInvite(o)) {
                    return announceNoPerms(sender);
                }
            } else if (kick) {
                if (!t.playerCanKick(o)) {
                    return announceNoPerms(sender);
                }
            } else {
                return announceNoPerms(sender);
            }
        }
        return false;
    }

    private boolean announceNoPerms(@NotNull CommandSender sender) {
        sender.sendMessage(Component.text(noPerms)
                .color(NamedTextColor.RED));
        return true;
    }

    @Nullable
    private UUID getUUIDOfPlayer(@NotNull String name) {
        var players = Bukkit.getOnlinePlayers();
        UUID p = null;
        for (Player player : players) {
            if (player.getName().equals(name)) {
                p = player.getUniqueId();
                break;
            }
        }
        return p;
    }

    private boolean displayTeam(@NotNull CommandSender sender) {
        var p = ((Player) sender).getUniqueId();
        var t = plugin.getPlayerTeam(p);
        if (t == null) {
            sender.sendMessage(Component.text(notTeam)
                    .color(NamedTextColor.RED));
        } else {
            sender.sendMessage(Component.text("You are in " + t.name + '.')
                    .color(NamedTextColor.GREEN));
        }
        return true;
    }
}
