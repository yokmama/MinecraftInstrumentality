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
import java.util.List;

public class MinigameSet implements SubCommand {
    GameCommandExecutor executor;

    public MinigameSet(GameCommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return setGame(sender, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(args.length == 0 || args[0].length() == 0) {
            return Arrays.asList("1", "3", "5", "9");
        } else if(args.length == 2) {
            return Arrays.asList("10", "15", "20", "30");
        } else if(args.length == 3) {
            return Arrays.asList("dummy", "playerKillCount", "totalKillCount", "health");
        }
        return new ArrayList<>();
    }

    public void sendInviteMessage(GameMaker gameMaker, Player player) {

        StringBuilder builder = new StringBuilder();
        builder.append("&c").append(player.getName()).append(" &6がゲーム参加の要求を送信しています。\n");
        builder.append("ゲームに参加するなら &c/mg join ").append(gameMaker.getHostplayer())
                .append(" &6を使用してください。\n");

        gameMaker.sendMessage(player.getName(), builder.toString());
    }

    public boolean setGame(CommandSender sender, String[] args){
        int time = -1;
        int max = 6; //定員の初期値は6
        String rule = "dummy";
        Player hostplayer = (Player)sender;


        if (args.length > 0) {
            try {
                time = Integer.parseInt(args[0]);
            } catch (Exception e) {
                hostplayer.sendMessage("ゲーム時間は数字でいれてください");
                return true;
            }
        }
        if(args.length> 1){
            try {
                max = Integer.parseInt(args[1]);
            } catch (Exception e) {
                hostplayer.sendMessage("定員は数字でいれてください");
                return true;
            }
        }
        if(args.length> 2){
            rule = args[2];
        }

        GameMaker gameMaker = executor.getGameMaker(hostplayer.getName());
        if (gameMaker != null && gameMaker.isInGame()) {
            hostplayer.sendMessage("すでにゲームがスタートしています。一度キャンセルしましょう");
            return false;
        } else if (gameMaker == null) {
            gameMaker = new GameMaker(executor, hostplayer, rule);
        }

        gameMaker.setTime(time);
        gameMaker.setMaxPlayer(max);

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
