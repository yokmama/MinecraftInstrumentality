package jp.minecraftday.minecraftinstrumentality.command.sub;

import jp.minecraftday.minecraftinstrumentality.core.MainPlugin;
import jp.minecraftday.minecraftinstrumentality.core.SubCommand;
import jp.minecraftday.minecraftinstrumentality.core.utils.I18n;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AddNGWord implements SubCommand {
    private MainPlugin plugin;
    public AddNGWord(MainPlugin plugin){
        this.plugin = plugin;
    }
    @Override
    public String getName() {
        return "ng";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = plugin.getConfig().getStringList("ng.words");
        if(args.length>0 && args[0].length()>0){
            if(list.contains(args[0])!=true){
                list.add(args[0]);
                plugin.getConfig().set("ng.words", list);
                plugin.saveConfig();
                sender.sendMessage(I18n.tl("message.md.ngword.add",args[0]));
            }else{
                sender.sendMessage(I18n.tl("message.md.ngword.err"));
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }

}
