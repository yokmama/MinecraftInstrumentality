package jp.minecraftday.minecraftinstrumentality.login;

import jp.minecraftday.minecraftinstrumentality.Main;
import jp.minecraftday.minecraftinstrumentality.plugin.EssentialsHandler;
import jp.minecraftday.minecraftinstrumentality.utils.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BasicIncome implements Listener {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMM");

    Main main;
    public BasicIncome(Main main){
        this.main = main;
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        JavaPlugin essentials = main.getEssentials();
        if (essentials != null) {
            Configuration configuration = main.getUserConfiguration(event.getPlayer());
            String received_income = configuration.getString("received_income");
            String thisMonth = format.format(Calendar.getInstance().getTime());
            if (!thisMonth.equals(received_income)) {
                //pay
                int money = main.getConfig().getInt("basicincome.money");

                new EssentialsHandler(essentials).pay(event.getPlayer(), money);
                event.getPlayer().sendMessage("運営から今月の振り込みがありました");

                configuration.set("received_income", thisMonth);
                configuration.save();

            }
        }

    }
}
