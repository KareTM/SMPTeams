package kare.smpteams;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TabComplete implements TabCompleter {
    List<String> arg = new ArrayList<>();

    String invite = "invite";
    String kick = "kick";

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if(arg.isEmpty()) {
            arg.add("create");
            arg.add("delete");
            arg.add("help");
            arg.add(invite);
            arg.add("join");
            arg.add(kick);
            arg.add("leave");
            arg.add("list");
            arg.add("perms");
            arg.add("color");

            if(sender.isOp()) {
                arg.add("reset");
            }
        }

        List<String> result = new ArrayList<>();
        if(args.length == 1) {
            for (String a : arg) {
                if(a.toLowerCase().startsWith(args[0].toLowerCase()))
                    result.add(a);
            }
            return result;
        } else {
            if (args[0].equals(invite) || args[0].equals(kick))
                return null;
            if (args[0].equals("perms")) {
                arg.clear();

                if (args.length < 3) {
                    arg.add("add");
                    arg.add("remove");

                    for (String a : arg) {
                        if(a.toLowerCase().startsWith(args[1].toLowerCase()))
                            result.add(a);
                    }
                    arg.clear();
                    return result;
                } else if (args.length < 4) {
                    arg.add(invite);
                    arg.add(kick);

                    for (String a : arg) {
                        if(a.toLowerCase().startsWith(args[2].toLowerCase()))
                            result.add(a);
                    }
                    arg.clear();
                    return result;
                } else {
                    return null;
                }
            }
        }

        return new ArrayList<>();
    }
}
