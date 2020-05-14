package jp.minecraftday.minecraftinstrumentality.command.sub;

import jp.minecraftday.minecraftinstrumentality.core.MainPlugin;
import jp.minecraftday.minecraftinstrumentality.core.SubCommand;
import jp.minecraftday.minecraftinstrumentality.core.utils.I18n;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SetWelcomeMessage implements SubCommand {
    private MainPlugin plugin;
    public SetWelcomeMessage(MainPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "welcome";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        StringBuffer msg = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            String s = args[i];
            if (msg.length() > 0) msg.append(" ");
            msg.append(s);
        }

        if (msg.length()>3  && msg.charAt(0) == '[' && msg.charAt(msg.length()-1) == ']' && msg.indexOf("text")!=-1) {
            plugin.getConfig().set("welcome.message", msg.toString());
            sender.sendMessage(I18n.tl("message.md.setwelcome1"));
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("[").append("{\"text\":\"").append(ChatColor.translateAlternateColorCodes('&', msg.toString())).append("\"}]");
            plugin.getConfig().set("welcome.message", builder.toString());
            sender.sendMessage(I18n.tl("message.md.setwelcome2"));
        }

        plugin.saveConfig();
        plugin.reloadConfig();
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
