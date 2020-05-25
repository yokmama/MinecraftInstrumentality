package jp.minecraftday.minecraftinstrumentality.command;

import jp.minecraftday.minecraftinstrumentality.command.sub.CheckDesignMark;
import jp.minecraftday.minecraftinstrumentality.command.sub.RegisterDesignMark;
import jp.minecraftday.minecraftinstrumentality.core.MainCommandExecutor;
import jp.minecraftday.minecraftinstrumentality.core.MainPlugin;

public class DesignMarkCommandExecutor extends MainCommandExecutor {
    public DesignMarkCommandExecutor(MainPlugin plugin) {
        super(plugin);

        addSubCommand(new RegisterDesignMark(plugin));
        addSubCommand(new CheckDesignMark(plugin));
    }

    @Override
    public String getName() {
        return "designmark";
    }

    @Override
    public String getPermission() {
        return "minecraftday.designmark";
    }
}
