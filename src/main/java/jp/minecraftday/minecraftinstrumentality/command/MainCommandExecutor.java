package jp.minecraftday.minecraftinstrumentality.command;

import jp.minecraftday.minecraftinstrumentality.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class MainCommandExecutor implements CommandExecutor {
    private final Main plugin;

    public MainCommandExecutor(Main ref) {
        plugin = ref;

        if(!plugin.getConfig().isSet("tntcanceller.worlds")) {
            plugin.getConfig().set("tntcanceller.worlds", new ArrayList<>());
            plugin.saveConfig();
        }
        if(!plugin.getConfig().isSet("welcome.message")) {
            plugin.getConfig().set("welcome.message", "Welcome to the MinecraftDay Server!");
            plugin.saveConfig();
        }

    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("minecraftday")) {
            String cmd0 = args[0].toLowerCase();
            if (cmd0.equals("reload")) {
                plugin.reloadConfig();
                sender.sendMessage("config.yml reloaded");
            }
            else if (cmd0.equals("welcome")) {
                StringBuffer msg = new StringBuffer();
                for(int i=1; i<args.length; i++){
                    String s = args[i];
                    if(msg.length()>0) msg.append(" ");
                    msg.append(s);
                }
                plugin.getConfig().set("welcome.message", msg.toString());
                plugin.saveConfig();
                plugin.reloadConfig();
            }

            return true;
        }
        return false;
    }
}
