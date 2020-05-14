package jp.minecraftday.minecraftinstrumentality.core;

import org.bukkit.command.TabExecutor;

public interface SubCommand extends TabExecutor {
    String getName();
    String getPermission();
}
