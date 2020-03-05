package jp.minecraftday.minecraftinstrumentality.command;

import jp.minecraftday.minecraftinstrumentality.Main;
import jp.minecraftday.minecraftinstrumentality.utils.Configuration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TranslateCommandExecutor implements CommandExecutor {
    final Main plugin;
    public TranslateCommandExecutor(Main main) {
        this.plugin = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("hiragana") && sender instanceof Player) {
            toggleHiragana((Player) sender);
            return true;
        }
        return false;
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
}
