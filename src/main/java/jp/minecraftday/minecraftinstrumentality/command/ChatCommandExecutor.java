package jp.minecraftday.minecraftinstrumentality.command;

import jp.minecraftday.minecraftinstrumentality.Main;
import jp.minecraftday.minecraftinstrumentality.utils.Configuration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatCommandExecutor implements CommandExecutor, TabExecutor {
    final Main plugin;
    public ChatCommandExecutor(Main main) {
        this.plugin = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("hiragana") && sender instanceof Player) {
            toggleHiragana((Player) sender);
            return true;
        }else  if(command.getName().equalsIgnoreCase("shout") && sender instanceof Player) {
                shout((Player) sender, args);
                return true;
        }
        return false;
    }

    private void shout(Player sender, String[] args) {
        StringBuilder builder = new StringBuilder();
        Arrays.stream(args).forEach(s-> {
            if(builder.length()>0)builder.append(" ");
            builder.append(s);
        });

        plugin.broadcastMessage(sender.getPlayer(), builder.toString(), true);
    }

    private void toggleHiragana(Player player) {
        Configuration configuration = plugin.getUserConfiguration(player);
        boolean hiragana = configuration.getBoolean("hiragana");
        configuration.set("hiragana", !hiragana);
        if(!hiragana){
            player.sendMessage("ひらがな自動変換モードをオンにしました");
        }else{
            player.sendMessage("ひらがな自動変換モードをオフにしました");
        }
        configuration.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
