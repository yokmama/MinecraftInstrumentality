package jp.minecraftday.minecraftinstrumentality.core;

import jp.minecraftday.minecraftinstrumentality.core.utils.Configuration;
import jp.minecraftday.minecraftinstrumentality.core.utils.I18n;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public abstract class MainPlugin extends JavaPlugin {
    protected static final Logger LOGGER = Logger.getLogger(MainPlugin.class.getSimpleName());
    private transient Configuration configuration;
    private transient I18n i18n;

    @Override
    public void onEnable() {

        if (getDataFolder().exists() != true) {
            getDataFolder().mkdirs();
        }

        this.i18n = new I18n(this);
        this.i18n.onEnable();
        this.i18n.updateLocale("ja");

        this.configuration = new Configuration(new File(getDataFolder(), "config.yml"));
        this.configuration.load();
   }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (this.configuration != null) {
            this.configuration.save();
        }

        if (this.i18n != null) {
            this.i18n.onDisable();
        }
    }

    @Override
    public FileConfiguration getConfig() {
        return configuration;
    }

    @Override
    public void saveConfig() {
        configuration.save();
    }


    @Override
    public void reloadConfig() {
        configuration.load();
    }

}
