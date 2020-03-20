package jp.minecraftday.minecraftinstrumentality.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class UserConfiguration {
    final File userFolder;
    final Map<String, Configuration> map = new HashMap<>();

    public UserConfiguration(File folder){
        userFolder = new File(folder, "userdata");
        if(!userFolder.exists()){
            userFolder.mkdirs();
        }
    }

    public Configuration getUserConfig(final Player player) {
        String uuid = player.getUniqueId().toString();
        if(map.containsKey(uuid)) return map.get(uuid);

        Configuration conf = new Configuration(new File(userFolder, uuid+".yml"));
        conf.load();
        map.put(uuid, conf);
        return conf;
    }
}
