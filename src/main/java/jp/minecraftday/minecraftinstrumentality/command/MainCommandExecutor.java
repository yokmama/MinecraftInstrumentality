package jp.minecraftday.minecraftinstrumentality.command;

import jp.minecraftday.minecraftinstrumentality.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainCommandExecutor implements CommandExecutor, TabExecutor {
    private final Main plugin;

    public MainCommandExecutor(Main ref) {
        plugin = ref;

        if (!plugin.getConfig().isSet("tntcanceller.worlds")) {
            plugin.getConfig().set("tntcanceller.worlds", new ArrayList<>());
            plugin.saveConfig();
        }
        if (!plugin.getConfig().isSet("welcome.message")) {
            StringBuilder builder = new StringBuilder();
            builder.append("[").append("{\"text\":\"").append("Welcome to the MinecraftDay Server!").append("\"}]");
            plugin.getConfig().set("welcome.message", builder.toString());
            plugin.saveConfig();
        }
        if (!plugin.getConfig().isSet("vote.jailname")) {
            plugin.getConfig().set("vote.jailname", "test");
            plugin.saveConfig();
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("md")) {
            String cmd0 = args[0].toLowerCase();
            if (cmd0.equals("reload")) {
                plugin.reloadConfig();
                sender.sendMessage("config.yml reloaded");
            } else if (cmd0.equals("welcome")) {
                StringBuffer msg = new StringBuffer();
                for (int i = 1; i < args.length; i++) {
                    String s = args[i];
                    if (msg.length() > 0) msg.append(" ");
                    msg.append(s);
                }

                if (msg.length()>3  && msg.charAt(0) == '[' && msg.charAt(msg.length()-1) == ']' && msg.indexOf("text")!=-1) {
                    plugin.getConfig().set("welcome.message", msg.toString());
                    sender.sendMessage("Welcomeメッセージ（コマンド対応)を設定しました");
                } else {
                    StringBuilder builder = new StringBuilder();
                    builder.append("[").append("{\"text\":\"").append(ChatColor.translateAlternateColorCodes('&', msg.toString())).append("\"}]");
                    plugin.getConfig().set("welcome.message", builder.toString());
                    sender.sendMessage("Welcomeメッセージを設定しました");
                }

                plugin.saveConfig();
                plugin.reloadConfig();
            }

            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equals("md")) {
            List<String> list = new ArrayList(Arrays.asList("welcome", "reload"));

            if (args.length == 0 || args[0].length() == 0) {
                return list;
            } else if (args.length == 1) {
                for (String s : list) {
                    if (s.startsWith(args[0])) return Collections.singletonList(s);
                }
            }
        }
        return null;
    }

}
