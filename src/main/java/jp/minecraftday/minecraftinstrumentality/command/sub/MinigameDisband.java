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

public class MinigameDisband implements SubCommand {
    GameCommandExecutor executor;

    public MinigameDisband(GameCommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String getName() {
        return "disband";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return disbandGame((Player)sender);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }

    private boolean disbandGame(Player player) {
        GameMaker gameMaker = executor.getGameMaker(player.getName());
        if (gameMaker != null) {
            gameMaker.finish();
            executor.remove(player.getName());
        } else {
            player.sendMessage("あなたはゲームの作成者ではありません");
        }

        return true;
    }


}
