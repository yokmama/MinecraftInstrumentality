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

public class MinigameCreate implements SubCommand {
    GameCommandExecutor executor;

    public MinigameCreate(GameCommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        setGame(sender, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list =  Arrays.stream(GameMaker.RULES).map(o->o[0]).collect(Collectors.toList());
        if(args.length == 0 || args[0].length() == 0) {
            return list;
        } else if (args.length == 1) {
            return list.stream().filter(s->s.startsWith(args[0])).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    public void sendInviteMessage(GameMaker gameMaker, Player player) {

        StringBuilder builder = new StringBuilder();
        builder.append("&c").append(player.getName()).append(" &6がゲーム参加の要求を送信しています。\n");
        builder.append("ゲームに参加するなら &c/mg join ").append(gameMaker.getHostPlayerName())
                .append(" &6を使用してください。\n");

        gameMaker.sendMessage(player.getName(), builder.toString());
    }

    public boolean setGame(CommandSender sender, String[] args){
        String rule = null;
        Player hostplayer = (Player)sender;

        if (args.length > 0) {
            rule = args[0];
        }

        GameMaker gameMaker = executor.getGameMaker(hostplayer.getName());
        if (gameMaker != null) {
            hostplayer.sendMessage("すでにゲームを作成しています。作り直す場合は先に解散してください");
            return false;
        } else  {
            gameMaker = new GameMaker(executor, hostplayer, rule);
        }

        final GameMaker gm = gameMaker;
        hostplayer.getWorld().getPlayers().forEach(player -> {
            if (player.getLocation().distance(hostplayer.getLocation()) <  64 && executor.getJoiningGame(player.getName()) == null) {
                sendInviteMessage(gm, player);
            }
        });

        executor.putGameMaker(hostplayer.getName(), gameMaker);

        return true;
    }
}
