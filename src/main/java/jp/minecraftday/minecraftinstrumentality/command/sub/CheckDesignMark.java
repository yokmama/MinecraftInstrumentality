package jp.minecraftday.minecraftinstrumentality.command.sub;

import jp.minecraftday.minecraftinstrumentality.Main;
import jp.minecraftday.minecraftinstrumentality.core.MainPlugin;
import jp.minecraftday.minecraftinstrumentality.core.SubCommand;
import jp.minecraftday.minecraftinstrumentality.core.utils.I18n;
import jp.minecraftday.minecraftinstrumentality.utils.DesignMarkDatabase;
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
            DesignMarkDatabase designMarkDb = ((Main)plugin).getDesignMarkDb();
            String key = designMarkDb.getDesiginedMarkID(plugin, itemStack);
            if(key==null){
                player.sendMessage(I18n.tl("message.register.unregistered"));
            }else{
                if(designMarkDb.checkDesignMark(plugin, itemStack)){
                    String designer = designMarkDb.getDesigner(key);
                    if(designer!=null) {
                        player.sendMessage(I18n.tl("message.designcheck.fine", designer));
                    }else{
                        player.sendMessage(I18n.tl("message.designcheck.error"));
                    }
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
