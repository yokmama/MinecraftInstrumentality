package jp.minecraftday.minecraftinstrumentality.command.sub;

import jp.minecraftday.minecraftinstrumentality.GameMaker;
import jp.minecraftday.minecraftinstrumentality.command.GameCommandExecutor;
import jp.minecraftday.minecraftinstrumentality.core.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MinigameForceJoin implements SubCommand {
    GameCommandExecutor executor;

    public MinigameForceJoin(GameCommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String getName() {
        return "forcejoin";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return forceJoinPlayer((Player)sender, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        //自分に所属していないプレイヤー
        List<String> list = Bukkit.getOnlinePlayers().stream()
                .filter(pl->executor.getJoiningGame(pl.getName()) == null)
                .map(pl->((Player) pl).getName()).collect(Collectors.toList());
        if(args.length == 0 || args[0].length() == 0) {
            return list;
        } else if (args.length == 1) {
            return list.stream().filter(s->s.startsWith(args[0])).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private boolean forceJoinPlayer(Player player, String[] cmds) {
        if (cmds.length == 0) {
            player.sendMessage("プレイヤー名をいれてください");
        }

        GameMaker gameMaker = executor.getGameMaker(player.getName());
        if (gameMaker == null) {
            gameMaker = executor.getGameMaker(player.getName());
        }

        if (gameMaker == null) {
            player.sendMessage("ゲームの主催者でないため追加できません");
            return true;
        }

        for (String playerName : cmds) {
            Player addPlayer = Bukkit.getPlayerExact(playerName);
            if (addPlayer != null) {
                new MinigameJoin(executor).joinGame(addPlayer, new String[]{gameMaker.getHostPlayerName()});
            }
        }

        return true;
    }

}
