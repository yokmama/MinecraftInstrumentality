package jp.minecraftday.minecraftinstrumentality.command.sub;

import jp.minecraftday.minecraftinstrumentality.GameMaker;
import jp.minecraftday.minecraftinstrumentality.command.GameCommandExecutor;
import jp.minecraftday.minecraftinstrumentality.core.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        if(args.length == 0 || args[0].length() == 0) {
            return executor.getGames();
        } else if (args.length == 1) {
            return executor.getGames().stream().filter(s->s.startsWith(args[0])).collect(Collectors.toList());
        }
        if(args.length == 1 || args[1].length() == 0) {
            return Arrays.stream(GameMaker.teamColors).map(chatColor -> chatColor.name()).collect(Collectors.toList());
        } else if (args.length == 2) {
            return Arrays.stream(GameMaker.teamColors).map(chatColor -> chatColor.name()).filter(s->s.startsWith(args[1])).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public boolean joinGame(CommandSender player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("ゲーム作成者を入力してください");
        }

        String gameID = args[0];
        String teamName = null;
        if(args.length > 1) {
            teamName = args[1];
        }

        GameMaker gameMaker = executor.getGameMaker(gameID);
        if (gameMaker == null || gameMaker.isInGame()) {
            player.sendMessage("指定されたゲームがないか、すでにスタートしているゲームです");
            return true;
        }

        GameMaker hostingGame = executor.getGameMaker(player.getName());
        if (hostingGame != null && !hostingGame.getHostPlayerName().equals(gameMaker.getHostPlayerName())) {
            if(hostingGame.getPlayers().size()>0){
                player.sendMessage("ゲームの作成者は他のゲームには参加できません");
                return true;
            }else {
                //参加しているプレイヤーがいないので、自分のゲームを終了して参加させることにする
                hostingGame.finish();
            }
        }

        if(!gameMaker.isJoining(player.getName())){
            //新規にゲームに参加する場合は定員チェックを行う
            if((gameMaker.getPlayers().size()+1)>gameMaker.getMaxPlayer()){
                player.sendMessage("そのゲームはすでに定員オーバーです");
                return true;
            }
        }

        GameMaker ogm = executor.getJoiningGame(player.getName());
        if (ogm != null) {
            ogm.removePlayer((Player)player);
        }

        gameMaker.addPlayer((Player)player, teamName);

        gameMaker.sendMessage(gameMaker.getHostPlayerName(), "&c" + player.getName() + "&6がゲームに参加しました");
        gameMaker.sendMessage(player.getName(), "&6ゲームに参加しました");

        return true;
    }
}
