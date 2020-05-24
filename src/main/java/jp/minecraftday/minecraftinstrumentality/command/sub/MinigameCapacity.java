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

public class MinigameCapacity implements SubCommand {
    GameCommandExecutor executor;

    public MinigameCapacity(GameCommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String getName() {
        return "capacity";
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
                sender.sendMessage("すでにゲームがスタートしてるゲームの定員は変更できません");
                return true;
            }
            if(args.length>0 && args[0].length()>0){
                try {
                    int max = Integer.parseInt(args[0]);
                    int org = gameMaker.getMaxPlayer();
                    gameMaker.setMaxPlayer(max);
                    sender.sendMessage("定員数を"+org+"から"+max+"に変更しました");
                } catch (Exception e) {
                    sender.sendMessage("定員数は数字でいれてください");
                }
            }
        } else {
            sender.sendMessage("このコマンドはゲームの作成者しかつかえません");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,@NotNull String[] args) {
        return new ArrayList<>();
    }
}
