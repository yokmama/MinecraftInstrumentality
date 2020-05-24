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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MinigameTeam implements SubCommand {
    GameCommandExecutor executor;

    public MinigameTeam(GameCommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String getName() {
        return "team";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        GameMaker gameMaker = executor.getJoiningGame(sender.getName());
        if(gameMaker == null){
            sender.sendMessage("あなたはゲームに参加していません");
            return true;
        }

        if(args.length>0 && args[0].length()>0){
            if(gameMaker.setTeam((Player)sender, args[0])) {
                sender.sendMessage("チームを"+args[0]+"に変更しました");
            }else{
                sender.sendMessage("チームを変更できませんでした");
            }
        }else{
            sender.sendMessage("チームを指定してください");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = Arrays.stream(GameMaker.teamColors).map(chatColor -> chatColor.name()).collect(Collectors.toList());
        if(args.length == 0 || args[0].length() == 0) {
            return list;
        } else if (args.length == 1) {
            return list.stream().filter(s->s.startsWith(args[0])).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
