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

public class MinigameStart implements SubCommand {
    GameCommandExecutor executor;

    public MinigameStart(GameCommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return startGame((Player)sender);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }

    private boolean startGame(Player player) {
        GameMaker gameMaker = executor.getGameMaker(player.getName());
        if (gameMaker != null) {
            if(gameMaker.getPlayers().size()>0) {
                gameMaker.start(executor.getPool());
            }else{
                gameMaker.sendMessage(player.getName(), "&6参加者が１人もいないためスタートできません");
            }
        } else {
            player.sendMessage("あなたはゲームの作成者ではありません");
        }

        return true;
    }

}
