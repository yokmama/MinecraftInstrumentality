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
import java.util.Collections;
import java.util.List;

public class MinigameJoin implements SubCommand {
    GameCommandExecutor executor;

    public MinigameJoin(GameCommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return joinGame(player, args);

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = GameCommandExecutor.getGames();
        if (args[0].length() == 0) {
            return list;
        } else if (args.length == 1) {
            for (String s : list) {
                if (s.startsWith(args[0])) return Collections.singletonList(s);
            }
        }
        return new ArrayList<>();
    }

    public boolean joinGame(CommandSender player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("ゲーム作成者を入力してください");
        }

        String gameID = args[0];

        GameMaker gameMaker = executor.getGameMaker(gameID);
        if (gameMaker == null || gameMaker.isInGame()) {
            player.sendMessage("指定されたゲームがないか、すでにスタートしているゲームです");
            return true;
        }

        if((gameMaker.getPlayers().size()+1)>gameMaker.getMaxPlayer()){
            player.sendMessage("そのゲームはすでに定員オーバーです");
            return true;
        }

        GameMaker hostingGame = executor.getGameMaker(player.getName());
        if (hostingGame != null && !hostingGame.getHostplayer().equals(gameMaker.getHostplayer())) {
            if(hostingGame.getPlayers().size()>0){
                player.sendMessage("ゲームの作成者は他のゲームには参加できません");
                return true;
            }else {
                hostingGame.finish();
            }
        }


        GameMaker ogm = executor.getJoiningGame(player.getName());
        if (ogm != null) {
            ogm.removePlayer((Player)player);
        }

        gameMaker.addPlayer((Player)player);

        gameMaker.sendMessage(gameMaker.getHostplayer(), "&c" + player.getName() + "&6がゲームに参加しました");
        gameMaker.sendMessage(player.getName(), "&6ゲームに参加しました");

        return true;
    }
}
