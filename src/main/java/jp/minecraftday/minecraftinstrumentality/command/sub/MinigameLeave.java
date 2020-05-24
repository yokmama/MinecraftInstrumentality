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

public class MinigameLeave implements SubCommand {
    GameCommandExecutor executor;

    public MinigameLeave(GameCommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return leaveGame((Player)sender);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }

    private boolean leaveGame(Player player) {
        GameMaker joiningGame = executor.getJoiningGame(player.getName());
        if (joiningGame!=null) {
            joiningGame.removePlayer(player);
            joiningGame.sendMessage(player.getName(), "&6ゲームから抜けました");
            joiningGame.sendMessage(joiningGame.getHostPlayerName(), "&c" + player.getName() + "&6がゲームから抜けました");
        } else {
            player.sendMessage("参加しているゲームがありません");
        }

        return true;
    }


}
