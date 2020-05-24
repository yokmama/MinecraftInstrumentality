package jp.minecraftday.minecraftinstrumentality.command;

import jp.minecraftday.minecraftinstrumentality.Main;
import jp.minecraftday.minecraftinstrumentality.command.sub.AddNGWord;
import jp.minecraftday.minecraftinstrumentality.command.sub.MainConfigReload;
import jp.minecraftday.minecraftinstrumentality.command.sub.SetWelcomeMessage;
import jp.minecraftday.minecraftinstrumentality.core.MainCommandExecutor;

public class MineCraftDayCommandExecutor extends MainCommandExecutor {

    public MineCraftDayCommandExecutor(Main ref) {
        super(ref);
        addSubCommand(new MainConfigReload(ref));
        addSubCommand(new SetWelcomeMessage(ref));
        addSubCommand(new AddNGWord(ref));
    }

    @Override
    public String getName() {
        return "minecraftday";
    }

    @Override
    public String getPermission() {
        return "minecraftday.md";
    }
}
