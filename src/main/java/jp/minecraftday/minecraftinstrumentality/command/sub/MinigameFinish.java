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

public class MinigameFinish implements SubCommand {
    GameCommandExecutor executor;

    public MinigameFinish(GameCommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String getName() {
        return "finish";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return finishGame((Player)sender);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }

    private boolean finishGame(Player player) {
        GameMaker gameMaker = executor.getGameMaker(player.getName());
        if (gameMaker != null) {
            gameMaker.finish();
            executor.remove(player.getName());
        } else {
            GameMaker joiningGame = executor.getJoiningGame(player.getName());
            if (joiningGame!=null) {
                joiningGame.removePlayer(player);
                joiningGame.sendMessage(player.getName(), "&6ゲームから抜けました");
                joiningGame.sendMessage(gameMaker.getHostplayer(), "&c" + player.getName() + "&6がゲームから抜けました");
            } else {
                player.sendMessage("参加しているゲームがありません");
            }
        }

        return true;
    }


}
