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

public class RegisterDesignMark implements SubCommand {
    MainPlugin plugin;
    public RegisterDesignMark(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "register";
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
            int amount = itemStack.getAmount();
            if(amount != 1) {
                player.sendMessage(I18n.tl("message.register.amount"));
                return true;
            }
            int damage = itemStack.getDurability();
            if(damage != 0) {
                player.sendMessage(I18n.tl("message.register.damage"));
                return true;
            }

            DesignMark designMark = ((Main)plugin).getDesignMark();
            String key = designMark.getDesiginedMark(plugin, itemStack);
            if(key!=null){
                player.sendMessage(I18n.tl("message.register.registered"));
            }else{
                designMark.registrationDesign(player, plugin, itemStack);
                player.sendMessage(I18n.tl("message.register.completion"));
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
