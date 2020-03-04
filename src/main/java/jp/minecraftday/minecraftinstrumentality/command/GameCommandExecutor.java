package jp.minecraftday.minecraftinstrumentality.command;

import jp.minecraftday.minecraftinstrumentality.GameMaker;
import jp.minecraftday.minecraftinstrumentality.PlayerEventListner;
import jp.minecraftday.minecraftinstrumentality.Main;
import jp.minecraftday.minecraftinstrumentality.utils.TitleSender;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class GameCommandExecutor implements CommandExecutor, PlayerEventListner, TabExecutor {
    final Main plugin;
    static AtomicInteger ids = new AtomicInteger();
    static AtomicInteger counter = new AtomicInteger();
    final ExecutorService pool;
    static Map<Integer, GameMaker> games = new ConcurrentHashMap<>();
    static Map<String, Integer> playerIDs = new ConcurrentHashMap<>();


    public GameCommandExecutor(Main ref) {
        plugin = ref;

        pool = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "MinecraftDay Thread " + ids.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    public boolean isShutdown() {
        return pool.isShutdown();
    }

    private GameMaker getGameMaker(Player player) {
        Integer playerID = playerIDs.get(player.getName());
        if (playerID == null) return null;
        return games.get(playerID);
    }

    private Integer incrementAndGetPlayerID(Player player) {
        Integer playerID = playerIDs.get(player.getName());
        if (playerID == null) {
            playerID = counter.incrementAndGet();
            playerIDs.put(player.getName(), playerID);
        }
        return playerID;
    }

    public void remove(String playerName) {
        Integer playerID = playerIDs.get(playerName);
        games.remove(playerID);
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("minigame") && args.length > 0 && sender instanceof Player) {
            String cmd0 = args[0].toLowerCase();
            if (cmd0.equals("set")) {
                initGame((Player) sender, Arrays.copyOfRange(args, 1, args.length));
            } else if (cmd0.equals("join")) {
                joinGame((Player) sender, Arrays.copyOfRange(args, 1, args.length));
            } else if (cmd0.equals("finish")) {
                finishGame((Player) sender);
            } else if (cmd0.equals("invite")) {
                invitePlayer((Player) sender, Arrays.copyOfRange(args, 1, args.length));
            } else if (cmd0.equals("list")) {
                listPlayer((Player) sender);
            } else if (cmd0.equals("start")) {
                startGame((Player) sender);
            } else if (cmd0.equals("gather")) {
                gatherPlayer((Player) sender);
            } else if (cmd0.equals("cancel")) {
                cancelGame((Player) sender);
            }

            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equals("minigame")) {
            List<String> list = new ArrayList(Arrays.asList("set", "start", "invite", "list", "cancel", "join", "gather", "finish"));

            if (args.length == 0 || args[0].length() == 0) {
                return list;
            } else if (args.length == 1) {
                for (String s : list) {
                    if (s.startsWith(args[0])) return Collections.singletonList(s);
                }
            }
        }
        return null;
    }

    private void initGame(Player hostplayer, String[] cmds) {
        final Integer playerID = incrementAndGetPlayerID(hostplayer);

        GameMaker gameMaker = games.get(playerID);
        if (gameMaker != null && gameMaker.isInGame()) {
            hostplayer.sendMessage("すでにゲームがスタートしています。一度キャンセルしましょう");
            return;
        } else if (gameMaker == null) {
            gameMaker = new GameMaker(this, hostplayer);
        }

        int time;


        if (cmds.length > 0) {
            try {
                time = Integer.parseInt(cmds[0]);
            } catch (Exception e) {
                hostplayer.sendMessage("ゲーム時間は数字でいれてください");
                return;
            }
        } else {
            hostplayer.sendMessage("mg set 分");
            return;
        }

        gameMaker.setTime(time);

        final GameMaker gm = gameMaker;
        hostplayer.getWorld().getPlayers().forEach(player -> {
            if (player.getLocation().distance(hostplayer.getLocation()) < 20) {
                sendInviteMessage(gm, player);
            }
        });

        games.put(playerID, gameMaker);
    }

    private void joinGame(Player player, String[] cmds) {
        if (cmds.length == 0) {
            player.sendMessage("ゲーム番号をいれてください");
        }

        int gameID = -1;
        try {
            gameID = Integer.parseInt(cmds[0]);
        } catch (Exception e) {
            player.sendMessage("ゲーム番号は数字でいれてください");
            return;
        }

        GameMaker gameMaker = games.get(gameID);
        if (gameMaker == null || gameMaker.isInGame()) {
            player.sendMessage("指定されたゲームがないか、すでにスタートしているゲームです");
            return;
        }

        GameMaker hostingGame = getGameMaker(player);
        if (hostingGame != null && !hostingGame.getHostplayer().equals(gameMaker.getHostplayer())) {
            player.sendMessage("ゲームの作成者は他のゲームには参加できません");
            return;
        }


        GameMaker ogm = getJoiningGame(player.getName());
        if (ogm != null) {
            ogm.removePlayer(player);
        }

        gameMaker.addPlayer(player);

        gameMaker.sendMessage(gameMaker.getHostplayer(), "&c" + player.getName() + "&6がゲームに参加しました");
        gameMaker.sendMessage(player.getName(), "&6ゲームに参加しました");

    }

    private void invitePlayer(Player player, String[] cmds) {
        if (cmds.length == 0) {
            player.sendMessage("プレイヤー名をいれてください");
        }

        GameMaker gameMaker = getJoiningGame(player.getName());
        if (gameMaker == null) {
            gameMaker = getGameMaker(player);
        }

        if (gameMaker == null) {
            player.sendMessage("ゲームに参加していないため招待できません");
            return;
        }

        for (String playerName : cmds) {
            Player invitePlayer = Bukkit.getPlayerExact(playerName);
            if (invitePlayer != null) {
                sendInviteMessage(gameMaker, invitePlayer);
            }
        }
    }

    private void sendInviteMessage(GameMaker gameMaker, Player player) {

        StringBuilder builder = new StringBuilder();
        builder.append("&c").append(player.getName()).append(" &6がゲーム参加の要求を送信しています。\n");
        builder.append("ゲームに参加するなら &c/mg join ").append(playerIDs.get(gameMaker.getHostplayer()))
                .append(" &6を使用してください。\n");

        gameMaker.sendMessage(player.getName(), builder.toString());
    }

    private void finishGame(Player player) {
        Optional<GameMaker> game = games.values().stream().filter(g -> g.isJoining(player.getName())).findFirst();
        if (game.isPresent()) {
            game.get().removePlayer(player);
            game.get().sendMessage(player.getName(), "&6ゲームを終了しました");
            game.get().sendMessage(game.get().getHostplayer(), "&c" + player.getName() + "&6がゲームから抜けました");
        } else {
            player.sendMessage("参加しているゲームがありません");
        }
    }

    private void listPlayer(Player player) {
        Optional<GameMaker> game = games.values().stream().filter(g -> g.isJoining(player.getName())).findFirst();
        if (game.isPresent()) {
            StringBuilder builder = new StringBuilder();
            game.get().getPlayers().forEach(p -> builder.append(" ").append(p));

            game.get().sendMessage(player.getName(), "&6" + builder.toString());
        } else {
            player.sendMessage("参加しているゲームがありません");
        }
    }

    private void startGame(Player player) {
        GameMaker gameMaker = getGameMaker(player);
        if (gameMaker != null) {
            gameMaker.start(pool);
        } else {
            player.sendMessage("あなたはゲームの作成者ではありません");
        }
    }

    private void gatherPlayer(Player player) {
        GameMaker gameMaker = getGameMaker(player);
        if (gameMaker != null) {
            gameMaker.gather();
        } else {
            player.sendMessage("ゲームがありません");
        }
    }

    private void cancelGame(Player player) {
        GameMaker gameMaker = getGameMaker(player);
        if (gameMaker != null) {
            gameMaker.cancel();
            remove(player.getName());
            player.sendMessage("ゲームを解散しました");
        } else {
            player.sendMessage("あなたはゲーム作成者ではありません");
        }
    }

    private GameMaker getJoiningGame(String player) {
        for (GameMaker g : games.values()) {
            if (g.isJoining(player))
                return g;
        }
        return null;
    }

    @Override
    public void onLogin(Player player) {
        GameMaker gameMaker = getJoiningGame(player.getName());
        if (gameMaker != null) {
            player.setScoreboard(gameMaker.getScoreboard());
        } else {
            gameMaker = getGameMaker(player);
            if (gameMaker != null) {
                player.setScoreboard(gameMaker.getScoreboard());
            }
        }

    }

    @Override
    public void onLogout(Player player) {

    }

}
