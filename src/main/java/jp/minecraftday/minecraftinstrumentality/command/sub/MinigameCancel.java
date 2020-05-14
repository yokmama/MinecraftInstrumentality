package jp.minecraftday.minecraftinstrumentality.command.sub;

import jp.minecraftday.minecraftinstrumentality.GameMaker;
import jp.minecraftday.minecraftinstrumentality.command.GameCommandExecutor;
import jp.minecraftday.minecraftinstrumentality.core.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MinigameCancel implements SubCommand {
    GameCommandExecutor executor;

    public MinigameCancel(GameCommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String getName() {
        return "cancel";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return cancelGame((Player)sender);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }

    private boolean cancelGame(Player player) {
        GameMaker gameMaker = executor.getGameMaker(player.getName());
        if (gameMaker != null) {
            if(gameMaker.isInGame()) {
                if(gameMaker.cancel()){
                    gameMaker.showTitle("ゲーム中止！", "");
                }
            }else{
                player.sendMessage("ゲームがスタートしていないのでキャンセルは必要ないです");
            }
        } else {
            player.sendMessage("あなたはゲーム作成者ではありません");
        }

        return true;
    }


}
