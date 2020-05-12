package jp.minecraftday.minecraftinstrumentality.plugin;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.perm.PermissionsHandler;
import net.ess3.api.MaxMoneyException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EssentialsHandler {
    Essentials essentials;
    public EssentialsHandler(JavaPlugin plugin){
        this.essentials = (Essentials)plugin;
    }

    public String getPrefix(Player base) {
        PermissionsHandler handler = essentials.getPermissionsHandler();
        return handler.getPrefix(base);
    }

    public boolean isMuted(Player player) {
        return essentials.getUser(player).isMuted();
    }

    public boolean isJailed(Player player) {
        return essentials.getUser(player).isJailed();
    }

    public String getChatColor(Player player){
        PermissionsHandler handler = essentials.getPermissionsHandler();
        String grp = handler.getGroup(player);
        if(grp.equals("gm")) return "&6";
        else if(grp.equals("sensei")) return "&6";
        return "&f";
    }

    public void pay(Player player, int money){
        User user = essentials.getUser(player);
        try {
            essentials.getUser(player).setMoney(user.getMoney().add(new BigDecimal(money)));
        } catch (MaxMoneyException e) {
            e.printStackTrace();
        }
        ;
    }

    public List<String> getWarps(){
        return new ArrayList(essentials.getWarps().getList());

    }

}
