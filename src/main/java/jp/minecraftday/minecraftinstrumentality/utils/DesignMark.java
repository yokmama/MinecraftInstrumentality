package jp.minecraftday.minecraftinstrumentality.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

public class DesignMark extends Configuration{
    public DesignMark(File configFile) {
        super(configFile);
        load();
    }

    public String getDesiginedMark(Plugin plugin, ItemStack itemStack){
        ItemMeta meta = itemStack.getItemMeta();
        if(meta!=null){
            String key = meta.getPersistentDataContainer().get(
                    new NamespacedKey(plugin, "designMark"),
                    PersistentDataType.STRING);

            return key;
        }

        return null;
    }

    public List<String> getData(Player player, ItemStack stack){
        final List<String> list = new ArrayList<>();
        list.add("crafted:"+player.getDisplayName());
        list.add("date:"+Calendar.getInstance().getTimeInMillis());
        list.add("type:"+stack.getType().toString());
//        list.add("amount:"+stack.getAmount());
//        list.add("damage:"+stack.getDurability());
        Map<Enchantment, Integer> enchantments = stack.getEnchantments();
        if (!enchantments.isEmpty()) {
            Map<String, Integer> enchant = new HashMap<>();
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                list.add(entry.getKey().getName().toLowerCase(Locale.ENGLISH)+":"+entry.getValue());
            }
        }
        return list;
    }

    public ItemStack registrationDesign(Player player, Plugin plugin, ItemStack itemStack){
        String check = getDesiginedMark(plugin, itemStack);
        if(check != null) return null;

        ItemMeta meta = itemStack.getItemMeta();
        long now = Calendar.getInstance().getTimeInMillis();
        String key = Long.toHexString(now);
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "designMark"),
                PersistentDataType.STRING,
                key);
        itemStack.setItemMeta(meta);

        setProperty(key, getData(player, itemStack));
        save();

        return itemStack;
    }

    public boolean checkDesignMark(Player player, Plugin plugin, ItemStack itemStack1){
        String key = getDesiginedMark(plugin, itemStack1);
        if(key == null) return false;

        List<String> list1 = getStringList(key);
        if(list1 == null) return false;

        List<String> list2 = getData(player, itemStack1);
        if(list2.size() != list1.size()) return false;

        for(int i=2; i<list2.size(); i++){
            String s1 = list1.get(i);
            String s2 = list2.get(i);
            if(s1.equals(s2)!=true) return  false;
        }

        return true;
    }

    public List<String> getDesignMark(Plugin plugin, ItemStack itemStack1) {
        String key = getDesiginedMark(plugin, itemStack1);
        if (key == null) return new ArrayList<>();

        return getStringList(key);
    }

}
