package jp.minecraftday.minecraftinstrumentality.command;

import jp.minecraftday.minecraftinstrumentality.Main;
import jp.minecraftday.minecraftinstrumentality.Threading;
import jp.minecraftday.minecraftinstrumentality.utils.Configuration;
import jp.minecraftday.minecraftinstrumentality.utils.UserConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

public class VoteCommandExecutor implements CommandExecutor, TabExecutor {
    final Main plugin;
    Timer theTimer = null;
    VoteTask currentTask = null;

    public VoteCommandExecutor(Main ref) {
        plugin = ref;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("vote") && args.length > 0 && sender instanceof Player) {
            String cmd0 = args[0].toLowerCase();
            if (cmd0.equals("jail") && args.length>1) {
                if(sender.hasPermission("minecraftday.vote.jail")){
                    voteJail((Player) sender, Arrays.copyOfRange(args, 1, args.length));
                }
            } else if (cmd0.equals("mute") && args.length>1) {
                if(sender.hasPermission("minecraftday.vote.mute")) {
                    voteMute((Player) sender, Arrays.copyOfRange(args, 1, args.length));
                }
            } else if (cmd0.equals("unmute") && args.length>1) {
                voteUnMute((Player) sender, Arrays.copyOfRange(args, 1, args.length));
            } else if (cmd0.equals("unjail") && args.length>1) {
                voteUnJail((Player) sender, Arrays.copyOfRange(args, 1, args.length));
            } else if (cmd0.equals("day")) {
                voteDay((Player) sender);
            } else if (cmd0.equals("night")) {
                voteNight((Player) sender);
            } else if (cmd0.equals("sun")) {
                voteSun((Player) sender);
            } else if (cmd0.equals("rain")) {
                voteRain((Player) sender);
            } else if (cmd0.equals("yes")) {
                voteYes((Player) sender);
            } else if (cmd0.equals("no")) {
                voteNo((Player) sender);
            }

            return true;
        }else if(command.getName().equalsIgnoreCase("voteyes") && sender instanceof Player){
            voteYes((Player) sender);
            return true;
        }else if(command.getName().equalsIgnoreCase("voteno") && sender instanceof Player){
            voteNo((Player) sender);
            return true;
        }else if(command.getName().equalsIgnoreCase("voteauto") && sender instanceof Player){
            voteAuto((Player) sender, args);
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equals("vote")) {
            List<String> list = new ArrayList(Arrays.asList("day","night","sun","rain", "unjail", "ummute","yes","no"));

            if(sender.hasPermission("minecraftday.vote.jail")){
                list.add("jail");
            }
            if(sender.hasPermission("minecraftday.vote.mute")){
                list.add("mute");
            }

            if (args.length == 0 || args[0].length() == 0) {
                return list;
            } else if(args.length == 1){
                for(String s : list){
                    if(s.startsWith(args[0])) return Collections.singletonList(s);
                }
            }
        } else if (command.getName().equals("voteauto")){
            List<String> list = new ArrayList(Arrays.asList("yes","no","off"));
            if (args.length == 0 || args[0].length() == 0) {
                return list;
            } else if(args.length == 1){
                for(String s : list){
                    if(s.startsWith(args[0])) return Collections.singletonList(s);
                }
            }
        }
        return null;
    }

    private void voteDay(Player player) {
        vote(player, new VoteTask(player, null, "時間を朝にする", true) {
            @Override
            void execute() {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set day");

            }
        });
    }

    private void voteNight(Player player) {
        vote(player, new VoteTask(player, null, "時間を夜にする", true) {
            @Override
            void execute() {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set night");

            }
        });
    }

    private void voteSun(Player player) {
        vote(player, new VoteTask(player, null, "天気を晴れにする", false) {
            @Override
            void execute() {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:weather clear");

            }
        });
    }

    private void voteRain(Player player) {
        vote(player, new VoteTask(player, null, "天気を雨にする", false) {
            @Override
            void execute() {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:weather rain");

            }
        });
    }

    private void voteMute(Player player, String[] args) {
        Player target = null;
        if(args.length>0){
            target = Bukkit.getPlayerExact(args[0]);
        }

        if(target!=null) {
            Boolean isMuted = plugin.isMuted(target);
            if(isMuted!=null) {
                if (!isMuted) {
                    vote(player, new VoteTask(player, target, args[0] + " をおくちチャックする", 3, true) {
                        @Override
                        void execute() {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mute " + args[0]);
                        }
                    });
                } else {
                    player.sendMessage("そのプレイヤーはしゃべれません");
                }
            }
        }else{
            player.sendMessage("そのプレイヤーはいません");
        }
    }

    private void voteUnMute(Player player, String[] args) {
        Player target = null;
        if(args.length>0){
            target = Bukkit.getPlayerExact(args[0]);
        }

        if(target!=null) {
            Boolean isMuted = plugin.isMuted(target);
            if(isMuted!=null) {
                if (isMuted) {
                    vote(player, new VoteTask(player, target, args[0] + " をしゃべれるようにする", true) {
                        @Override
                        void execute() {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "unmute " + args[0]);
                        }
                    });
                } else {
                    player.sendMessage("そのプレイヤーはしゃべれます");
                }
            }
        }else{
            player.sendMessage("そのプレイヤーはいません");
        }
    }

    private void voteJail(Player player, String[] args) {
        Player target = null;
        if(args.length>0){
            target = Bukkit.getPlayerExact(args[0]);
        }

        if(target!=null) {
            Boolean isJailed = false;//plugin.isJailed(target);
            if(isJailed!=null) {
                if (!isJailed) {
                    vote(player, new VoteTask(player, target, args[0] + " をろうやに入れる", 3, true) {
                        @Override
                        void execute() {
                            String jailname = plugin.getConfig().getString("vote.jailname");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "jail " + args[0] + " " + jailname + " 30m");
                        }
                    });
                } else {
                    player.sendMessage("そのプレイヤーはろうやにいます");
                }
            }
        }else{
            player.sendMessage("そのプレイヤーはいません");
        }
    }

    private void voteUnJail(Player player, String[] args) {
        Player target = null;
        if(args.length>0){
            target = Bukkit.getPlayerExact(args[0]);
        }
        if(target!=null) {
            Boolean isJailed = plugin.isJailed(target);
            if(isJailed!=null) {
                if (isJailed) {
                    vote(player, new VoteTask(player, target, args[0] + " をろうやから出す", true) {
                        @Override
                        void execute() {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "unjail " + args[0]);
                        }
                    });
                } else {
                    player.sendMessage("そのプレイヤーはろうやにいません");
                }
            }
        }else{
            player.sendMessage("そのプレイヤーはいません");
        }
    }

    private void voteYes(Player player) {
        if (currentTask != null && currentTask.isVotingRight(player)) {
            currentTask.numSupporters++;
            currentTask.voters.remove(player.getName());
            Player sender = Bukkit.getPlayerExact(currentTask.senderName);
            if(sender!=null){
                sender.sendMessage(player.getName()+"が投票しました");
            }
            if (currentTask.isFinish()) {
                currentTask.cancel();
                currentTask.run();
            }
        }else{
            player.sendMessage("投票がありません");
        }
    }

    private void voteNo(Player player) {
        if (currentTask != null && currentTask.isVotingRight(player)) {
            currentTask.numOpponents++;
            currentTask.voters.remove(player.getName());
            Player sender = Bukkit.getPlayerExact(currentTask.senderName);
            if(sender!=null){
                sender.sendMessage(player.getName()+"が投票しました");
            }
            if (currentTask.isFinish()) {
                currentTask.cancel();
                currentTask.run();
            }
        }else{
            player.sendMessage("投票がありません");
        }
    }

    private void voteAuto(Player player, String[] args) {
        String yesOrNo = "";
        if(args.length>0){
            yesOrNo = args[0].toLowerCase();
        }

        if(!yesOrNo.equals("yes") && !yesOrNo.equals("no") && !yesOrNo.equals("off")){
            player.sendMessage("Usage: /autovote [yes|no|off]");
            return;
        }

        Configuration conf = plugin.getUserConfiguration(player);

        if(yesOrNo.equals("yes")) {
            player.sendMessage("今後投票は自動でいいね！を返事するようになりました");
        }else if(yesOrNo.equals("no")){
            player.sendMessage("今後投票は自動でだめだよ！を返事するようになりました");
        }else if(yesOrNo.equals("off")){
            player.sendMessage("今後投票は毎回自分で選択するようになりました");
        }else if(yesOrNo.equals("")){
            String now = conf.getString("autovote");
            if(now.equals("yes"))
                player.sendMessage("現在自動でいいね！を選択するようになっています");
            else if(now.equals("no"))
                player.sendMessage("現在自動でだめだよ！を選択するようになっています");
            else
                player.sendMessage("現在自動で選択するようになっていません");
            return;
        }

        conf.set("autovote", yesOrNo);
        conf.save();
    }


    private void vote(Player player, VoteTask task) {
        if (theTimer != null) {
            player.sendMessage("前の投票がおわっていません");
            return;
        }

        if(task.minimumRequired>0){
            if(task.getPlayers().size()<task.minimumRequired){
                player.sendMessage("その投票は最低 " + task.minimumRequired + "人いないと始めれません");
                return;
            }
        }

        task.getPlayers().forEach(p -> {
            if (!task.senderName.equals(p.getName()) && (task.targetName == null ||!task.targetName.equals(p.getName()))) {
                Configuration configuration = plugin.getUserConfiguration(p);
                String yesOrNo = "";
                if(configuration!=null && configuration.getString("autovote")!=null){
                    yesOrNo = configuration.getString("autovote").toLowerCase();
                }
                if(task.minimumRequired < 3 && yesOrNo!=null && (yesOrNo.equals("yes") || yesOrNo.equals("no"))){
                    StringBuilder builder = new StringBuilder();
                    builder.append("&c").append(player.getName()).append(" &6の「").append(task.question()).append("」の投票に\n");
                    if(yesOrNo.equals("yes")) {
                        task.numSupporters++;
                        builder.append("いいよ！に票をいれました！\n");
                    }
                    else {
                        task.numOpponents++;
                        builder.append("だめだよ！に票をいれました！\n");
                    }
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', builder.toString()));
                    task.numVotersCount++;
                }else {
                    StringBuilder builder = new StringBuilder();
                    builder.append("&c").append(player.getName()).append(" &6が「").append(task.question()).append("」の投票を始めました！\n");
                    builder.append("いいよ！に票を入れるには&c/voteyes &6を使って投票しましょう！\n");
                    builder.append("だめだよ！に票を入れるには &c/voteno &6を使って投票しましょう！\n");
                    builder.append("&c120秒&6以内に答えてね！。 投票をしないといいよ！になるよ。");
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', builder.toString()));
                    task.numVotersCount++;
                    task.voters.add(p.getName());
                }
            }
        });

        //投票者に送信
        StringBuilder msg = new StringBuilder();
        msg.append("&c").append(player.getName()).append(" &6が「").append(task.question()).append("」の投票を始めました！\n");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg.toString()));

        if (task.numVotersCount == 0 || task.isFinish()) {
            task.run();
        } else {
            currentTask = task;
            theTimer = new Timer();
            theTimer.schedule(task, 1000 * 60 * 2);
        }
    }

    void sendMessage(Player p, String msg) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    abstract class VoteTask extends TimerTask {
        int numVotersCount = 0;
        int numSupporters = 0;
        int numOpponents = 0;
        int minimumRequired = 0;
        String senderName;
        String targetName = null;
        Set<String> voters = new HashSet<>();
        boolean isVoteEveryone;

        String question = "";

        VoteTask(Player player, Player target, String question, boolean isVoteEveryone) {
            senderName = player.getName();
            if (target != null) targetName = target.getName();
            this.question = question;
            this.isVoteEveryone = isVoteEveryone;
        }

        VoteTask(Player player, Player target, String question, int minimumRequired, boolean isVoteEveryone) {
            senderName = player.getName();
            this.minimumRequired = minimumRequired;
            if (target != null) targetName = target.getName();
            this.question = question;
            this.isVoteEveryone = isVoteEveryone;
        }

        String question() {
            return question;
        }

        boolean isFinish() {
            return numVotersCount == 0 || ((double) (numSupporters + numOpponents) / numVotersCount) > 0.8;
        }

        boolean isVotingRight(Player p) {
            return voters.contains(p.getName());
        }

        abstract void execute();

        Collection<? extends Player> getPlayers(){
            if(isVoteEveryone){
                //もし全員投票なら全部のワールドのプレイヤー返却する
                return Bukkit.getOnlinePlayers();
            }else {
                //そうでなければそのプレイヤーのいるワールドのプレイヤーを返却する
                Player player = Bukkit.getPlayerExact(senderName);
                return player.getWorld().getPlayers();
            }
        }


        @Override
        public void run() {
            try {
                int fixNumSupporters = numSupporters + (numVotersCount - (numSupporters + numOpponents));
                boolean check = numVotersCount == 0 || ((double) fixNumSupporters / numVotersCount) > 0.8;
                if (check) {
                    getPlayers().forEach(p -> sendMessage(p, "&6"+question+"の投票はいいね！にきまりました"));
                    Threading.postToServerThread(new Threading.Task(plugin) {
                        @Override
                        public void execute() {
                            VoteTask.this.execute();
                        }
                    });
                } else {
                    getPlayers().forEach(p -> sendMessage(p, "&6"+question+"の投票はだめ！にきまりました"));
                }
            } finally {
                theTimer = null;
                currentTask = null;
            }
        }
    }

}
