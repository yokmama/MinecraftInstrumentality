package jp.minecraftday.minecraftinstrumentality.core;

import jp.minecraftday.minecraftinstrumentality.core.utils.I18n;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class MainCommandExecutor implements SubCommand {
    protected static final Logger LOGGER = Logger.getLogger(MainCommandExecutor.class.getSimpleName());
    protected final MainPlugin plugin;
    protected Map<String, SubCommand> subCommands = new HashMap<>();

    public MainCommandExecutor(MainPlugin plugin) {
        this.plugin = plugin;
    }

    public List<String> getSubCommands() {
        return subCommands.keySet().stream().collect(Collectors.toList());
    }

    protected void addSubCommand(SubCommand subCommand){
        subCommands.put(subCommand.getName(), subCommand);
    }

    public boolean onCommandImpl(CommandSender sender, Command command, String label, String[] args){
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (getPermission() != null && !sender.hasPermission(getPermission())) {
            sender.sendMessage(I18n.tl("error.command.permission"));
            return false;
        }

        if (args.length < 1 || !getSubCommands().contains(args[0])) {
            return onCommandImpl(sender, command, label, args);
        }

        SubCommand subCommand = subCommands.get(args[0]);
        if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
            sender.sendMessage(I18n.tl("error.command.permission"));
            return false;
        }

        return subCommand.onCommand(sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> commands = getSubCommands();

        if (args.length == 0 || args[0].length() == 0) {
            return commands;
        } else if (args.length == 1) {
            for (String s : commands) {
                if (s.startsWith(args[0])) return Collections.singletonList(s);
            }
        } else {
            SubCommand subCommand = subCommands.get(args[0]);
            if(subCommand != null) return subCommand.onTabComplete(sender, command, args[0], Arrays.copyOfRange(args, 1, args.length));
        }
        return new ArrayList<>();
    }


}
