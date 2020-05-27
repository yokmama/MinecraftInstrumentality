package jp.minecraftday.minecraftinstrumentality.login;

import jp.minecraftday.minecraftinstrumentality.Main;
import jp.minecraftday.minecraftinstrumentality.core.utils.I18n;
import jp.minecraftday.minecraftinstrumentality.utils.Configuration;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
        if (main.getEcon() != null) {
            Configuration configuration = main.getUserConfiguration(event.getPlayer());
            String received_income = configuration.getString("received_income");
            String thisMonth = format.format(Calendar.getInstance().getTime());
            if (!thisMonth.equals(received_income)) {
                //pay
                int money = main.getConfig().getInt("basicincome.money");
                EconomyResponse r = main.getEcon().depositPlayer(event.getPlayer(), money);
                event.getPlayer().sendMessage(I18n.tl("message.economy.withdrow", money));

                configuration.set("received_income", thisMonth);
                configuration.save();

            }
        }

    }
}
