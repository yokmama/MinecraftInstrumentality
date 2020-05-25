package jp.minecraftday.minecraftinstrumentality.command.sub;

import jp.minecraftday.minecraftinstrumentality.Main;
import jp.minecraftday.minecraftinstrumentality.core.MainPlugin;
import jp.minecraftday.minecraftinstrumentality.core.SubCommand;
import jp.minecraftday.minecraftinstrumentality.core.utils.I18n;
import jp.minecraftday.minecraftinstrumentality.utils.DesignMark;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CheckDesignMark implements SubCommand {
    MainPlugin plugin;
    public CheckDesignMark(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player)sender;
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack != null && !itemStack.getType().isAir()) {
            DesignMark designMark = ((Main)plugin).getDesignMark();
            String key = designMark.getDesiginedMark(plugin, itemStack);
            if(key==null){
                player.sendMessage(I18n.tl("message.register.unregistered"));
            }else{
                if(designMark.checkDesignMark(player, plugin, itemStack)){
                    List<String> list = designMark.getDesignMark(plugin, itemStack);
                    String crafted = list.get(0).substring("crafted:".length());
                    player.sendMessage(I18n.tl("message.designcheck.fine", crafted));
                }else{
                    player.sendMessage(I18n.tl("message.designcheck.error"));
                }
            }
        } else {
            player.sendMessage(I18n.tl("message.register.noitem"));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
