package jp.minecraftday.minecraftinstrumentality.command;

import com.onarandombox.MultiverseCore.MultiverseCore;
import jp.minecraftday.minecraftinstrumentality.Main;
import jp.minecraftday.minecraftinstrumentality.plugin.WorldEditHandler;
import jp.minecraftday.minecraftinstrumentality.utils.ArgumentParser;
import jp.minecraftday.minecraftinstrumentality.utils.Region;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class EstateCommandExecutor implements CommandExecutor, TabExecutor {
    protected static final Logger LOGGER = Logger.getLogger("MI");
    final Main plugin;
    public EstateCommandExecutor(Main main) {
        this.plugin = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!command.getName().equalsIgnoreCase("estate")) return false;
        if(!(sender instanceof Player)) return false;
        if(args.length < 1) return false;

        String subCommand = args[0];
        if(subCommand.equalsIgnoreCase("price")) {

            if(plugin.getMultiverseCore() == null) return false;
            if(plugin.getWorldEdit() == null) return false;

            WorldEditHandler worldEditHandler =  new WorldEditHandler(plugin.getWorldEdit());


            try {
                BlockVector base = new ArgumentParser(Arrays.copyOfRange(args, 1, args.length))
                        .add(BlockVector.class)
                        .getBlockVector();

                Region region = worldEditHandler.getSelection((Player) sender);
                //Location spawn = multiverseHandler.getSpawnPosition(((Player) sender).getWorld().getName());
                BlockVector center = region.getCenter();
                int a = center.getBlockX() - base.getBlockX();
                int b = center.getBlockZ() - base.getBlockZ();

                double d = Math.sqrt(a * a + b * b);
                int area = region.getArea();
                double r = d/1000.0;
                if(r > 1.0) r = 1.0;
                double prise = 40.0 - r*40.0;
                double area_price = area * prise;

                StringBuilder builder = new StringBuilder();

                builder.append("&d");
                builder.append("広さ:" + area).append("\n");
                builder.append("距離:" + String.format("%.2f", d)).append("\n");
                builder.append("BLK:" + String.format("%.2f", prise)).append("\n");
                builder.append("価格:" + String.format("%.2f", area_price));

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',builder.toString()));


                return true;
            } catch (ArgumentParser.ArgumentParseException e) {
                sender.sendMessage("起点となる座標のパラメータがまちがっています。 /estate price X Y Z");
                LOGGER.info("Error EstateCommandExecutor::onCommand:"+e.getMessage());
            } catch (Exception e) {
                sender.sendMessage("宅地となるエリアを選択してください。");
                LOGGER.info("Error EstateCommandExecutor::onCommand:"+e.getMessage());
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(args.length < 2){
            return Arrays.asList("price");
        }

        return new ArrayList<>();
    }
}
