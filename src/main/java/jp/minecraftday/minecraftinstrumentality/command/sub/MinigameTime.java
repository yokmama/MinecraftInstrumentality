package jp.minecraftday.minecraftinstrumentality.command.sub;

import jp.minecraftday.minecraftinstrumentality.GameMaker;
import jp.minecraftday.minecraftinstrumentality.command.GameCommandExecutor;
import jp.minecraftday.minecraftinstrumentality.core.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MinigameTime implements SubCommand {
    GameCommandExecutor executor;

    public MinigameTime(GameCommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String getName() {
        return "time";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        GameMaker gameMaker = executor.getGameMaker(sender.getName());
        if (gameMaker != null) {
            if(gameMaker.isInGame()) {
                sender.sendMessage("すでにゲームがスタートしてるゲームの時間は変更できません");
                return true;
            }
            if(args.length>0 && args[0].length()>0){
                try {
                    int time = Integer.parseInt(args[0]);
                    int org = gameMaker.getTime();
                    gameMaker.setTime(time);
                    sender.sendMessage("時間を"+org+"分から"+time+"分に変更しました");
                } catch (Exception e) {
                    sender.sendMessage("ゲーム時間は数字でいれてください");
                }
            }
        } else {
            sender.sendMessage("このコマンドはゲームの作成者しかつかえません");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
