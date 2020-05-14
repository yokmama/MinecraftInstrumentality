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
import java.util.List;
import java.util.stream.Collectors;

public class MinigameKick implements SubCommand {
    GameCommandExecutor executor;

    public MinigameKick(GameCommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return kickPlayer((Player)sender, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        GameMaker gameMaker = executor.getJoiningGame(sender.getName());
        if(gameMaker == null) gameMaker = executor.getGameMaker(sender.getName());
        if(gameMaker!=null){
            return gameMaker.getPlayers().stream().collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private boolean kickPlayer(Player player, String[] cmds) {
        if (cmds.length == 0) {
            player.sendMessage("プレイヤー名をいれてください");
        }

        GameMaker hostingGame = executor.getGameMaker(player.getName());
        if (hostingGame == null) {
            player.sendMessage("kickはゲーム作成者でないと使えません");
            return true;
        }

        for (String playerName : cmds) {
            Player kickPlayer = Bukkit.getPlayerExact(playerName);
            if (kickPlayer != null && hostingGame.isJoining(kickPlayer.getName())) {
                hostingGame.removePlayer(kickPlayer);
                hostingGame.sendMessage(kickPlayer.getName(), "&6ゲームからキックされました");
                hostingGame.sendMessage(player.getName(), "&c" + kickPlayer.getName() + "&6をキックしました");
            } else {
                player.sendMessage("&c" +kickPlayer.getName()+"はゲームに参加していません");
            }
        }

        return true;
    }


}
