package jp.minecraftday.minecraftinstrumentality.command.sub;

import jp.minecraftday.minecraftinstrumentality.GameMaker;
import jp.minecraftday.minecraftinstrumentality.command.GameCommandExecutor;
import jp.minecraftday.minecraftinstrumentality.core.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MinigameGather implements SubCommand {
    GameCommandExecutor executor;

    public MinigameGather(GameCommandExecutor executor) {
        this.executor = executor;
    }


    @Override
    public String getName() {
        return "gather";
    }

    @Override
    public String getPermission() {
        return "minecraftday.mg.gather";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return gatherPlayer((Player)sender);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }

    private boolean gatherPlayer(Player player) {
        GameMaker gameMaker = executor.getGameMaker(player.getName());
        if (gameMaker != null) {
            gameMaker.gather();
        } else {
            player.sendMessage("ゲームがありません");
        }

        return true;
    }

}
