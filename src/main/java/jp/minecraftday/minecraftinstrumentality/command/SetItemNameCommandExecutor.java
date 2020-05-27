package jp.minecraftday.minecraftinstrumentality.command;

import jp.minecraftday.minecraftinstrumentality.Main;
import jp.minecraftday.minecraftinstrumentality.core.utils.I18n;
import jp.minecraftday.minecraftinstrumentality.utils.DesignMarkDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SetItemNameCommandExecutor implements CommandExecutor, TabExecutor {
    final Main plugin;

    public SetItemNameCommandExecutor(Main ref) {
        plugin = ref;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setitem") && args.length > 0 && sender instanceof Player) {
            Player player = (Player)sender;
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (itemStack != null && !itemStack.getType().isAir()) {
                DesignMarkDatabase designMarkDb = plugin.getDesignMarkDb();
                String key = designMarkDb.getDesiginedMarkID(plugin, itemStack);
                if (key != null) {
                    player.sendMessage(I18n.tl("message.setname.registered"));
                }
                else {
                    String cmd0 = args[0].toLowerCase();
                    if (cmd0.equals("name")) {
                        setName(player, itemStack, Arrays.copyOfRange(args, 1, args.length));
                    } else if (cmd0.equals("lore")) {
                        setLore(itemStack, Arrays.copyOfRange(args, 1, args.length));
                    }
                }
            }else{
                player.sendMessage(I18n.tl("message.setname.noitem"));
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equals("setitem")) {
            List<String> list = new ArrayList(Arrays.asList("name", "lore"));

            if (args.length == 0 || args[0].length() == 0) {
                return list;
            } else if (args.length == 1) {
                for (String s : list) {
                    if (s.startsWith(args[0])) return Collections.singletonList(s);
                }
            }
        }
        return new ArrayList<>();
    }

    private void setName(Player player, ItemStack itemStack, String[] args) {
        StringBuilder builder = new StringBuilder();
        if (args != null && args.length > 0) {
            Arrays.stream(args).forEach(s -> {
                if (builder.length() > 0) builder.append(" ");
                builder.append(s);
            });
        }

        if (builder.length() > 0) {
            String name = builder.toString();
            if(name.length()>64){
                name = name.substring(0, 64) + "~";
                player.sendMessage(I18n.tl("message.setname.toomuchname"));
            }

            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            itemStack.setItemMeta(meta);
        } else {
            ItemMeta deafultMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(deafultMeta.getDisplayName());
            itemStack.setItemMeta(meta);
        }
    }

    private void setLore(ItemStack itemStack, String[] args) {
        List<String> list = new ArrayList<>();
        if (args != null && args.length > 0) {
            Arrays.stream(args).forEach(s -> {
                list.add(ChatColor.translateAlternateColorCodes('&', s));
            });
        }

        if (list.size() > 0) {
            ItemMeta meta = itemStack.getItemMeta();
            meta.setLore(list);
            itemStack.setItemMeta(meta);
        } else {
            ItemMeta deafultMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
            ItemMeta meta = itemStack.getItemMeta();
            meta.setLore(deafultMeta.getLore());
            itemStack.setItemMeta(meta);
        }
    }

}
