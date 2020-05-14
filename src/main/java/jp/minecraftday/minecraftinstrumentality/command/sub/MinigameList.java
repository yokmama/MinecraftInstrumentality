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
import java.util.List;

public class MinigameList implements SubCommand {
    GameCommandExecutor executor;

    public MinigameList(GameCommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return listPlayer(sender);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }

    private boolean listPlayer(CommandSender player) {
        GameMaker gameMaker = executor.getGameMaker(player.getName());
        if(gameMaker == null){
            gameMaker = executor.getJoiningGame(player.getName());
        }
        if (gameMaker!=null) {
            StringBuilder builder = new StringBuilder();
            gameMaker.getPlayers().forEach(p -> builder.append(" ").append(p));
            if(builder.length()>0) {
                gameMaker.sendMessage(player.getName(), "&6" + builder.toString());
            }else{
                gameMaker.sendMessage(player.getName(), "&6参加者が１人もいません");
            }
        } else {
            player.sendMessage("参加しているゲームがありません");
        }

        return true;
    }


}
