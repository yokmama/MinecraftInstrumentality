package jp.minecraftday.minecraftinstrumentality.command;

import jp.minecraftday.minecraftinstrumentality.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetItemNameCommandExecutor implements CommandExecutor{
    final Main plugin;

    public SetItemNameCommandExecutor(Main ref) {
        plugin = ref;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setitem") && args.length > 0 && sender instanceof Player) {
            String cmd0 = args[0].toLowerCase();
            if (cmd0.equals("name")) {
                setName((Player) sender, Arrays.copyOfRange(args, 1, args.length));
            } else if (cmd0.equals("lore")) {
                setLore((Player) sender, Arrays.copyOfRange(args, 1, args.length));
            }

            return true;
        }
        return false;
    }

    private void setName(Player player, String[] args) {
        StringBuilder builder = new StringBuilder();
        if(args!=null && args.length>0){
            Arrays.stream(args).forEach(s -> {if(builder.length()>0) builder.append(" "); builder.append(s);});
        }

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if(itemStack != null && !itemStack.getType().isAir()){
            if(builder.length()>0){
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', builder.toString()));
                itemStack.setItemMeta(meta);
            }else{
                ItemMeta deafultMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName(deafultMeta.getDisplayName());
                itemStack.setItemMeta(meta);
            }
        }else{
            player.sendMessage("アイテムをもっていません");
        }
    }

    private void setLore(Player player, String[] args) {
        List<String> list = new ArrayList<>();
        if(args!=null && args.length>0){
            Arrays.stream(args).forEach(s -> {list.add(ChatColor.translateAlternateColorCodes('&', s));});
        }

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if(itemStack != null && !itemStack.getType().isAir()){
            if(list.size()>0){
                ItemMeta meta = itemStack.getItemMeta();
                meta.setLore(list);
                itemStack.setItemMeta(meta);
            }else{
                ItemMeta deafultMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
                ItemMeta meta = itemStack.getItemMeta();
                meta.setLore(deafultMeta.getLore());
                itemStack.setItemMeta(meta);
            }
        }else{
            player.sendMessage("アイテムをもっていません");
        }
    }

}
