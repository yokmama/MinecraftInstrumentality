package jp.minecraftday.minecraftinstrumentality.utils;

import jp.minecraftday.minecraftinstrumentality.core.MainPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DesignMarkDatabase {
    private static final Logger LOGGER = Logger.getLogger(DesignMarkDatabase.class.getSimpleName());

    private String sqldb;
    private Connection conn;

    public boolean connect(){
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + sqldb);
            LOGGER.info("designmark db connected");
            return true;
        }catch (Exception e){
            LOGGER.log(Level.WARNING, "jdbcが利用できない", e);
        }
        return false;
    }

    public void disconnect(){
        if(conn!=null){
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "データベースのクローズに失敗した", e);
            }
        }
    }

    private Connection getConnection(){ return conn;}

    public DesignMarkDatabase(MainPlugin plugin) {
        if(plugin.getDataFolder().exists()!=true){
            plugin.getDataFolder().mkdirs();
        }
        this.sqldb = new File(plugin.getDataFolder(), "designs.db").getAbsolutePath();

        if(connect()){
            try(Statement stmt = getConnection().createStatement()){
                StringBuilder builder = new StringBuilder();
                builder.append("CREATE TABLE IF NOT EXISTS DESIGNMARK (");
                builder.append("ID INTEGER PRIMARY KEY AUTOINCREMENT,");
                builder.append("REG_DATE INTEGER,");
                builder.append("DESIGNER TEXT,");
                builder.append("ITEM_NAME TEXT,");
                builder.append("SPEC BLOB)");
                stmt.executeUpdate(builder.toString());
                LOGGER.info("テーブルを作成した");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "テーブルの生成処理に失敗した", e);
            }
        }else{
            LOGGER.log(Level.WARNING, "jdbcをオープンできない");
        }
    }

    public class DesignMark {
        Date regDate;
        String designer;
        String itemName;
        String itemType;
        String itemLore;
        Map<String, Integer> enchantments = new HashMap<>();

        public DesignMark(ItemStack itemStack){
            regDate = Calendar.getInstance().getTime();
            itemName = itemStack.getItemMeta().getDisplayName();
            itemType = itemStack.getType().toString();
            ItemMeta meta = itemStack.getItemMeta();
            if(meta!=null){
                itemLore = getLoreString(meta);
                Map<Enchantment, Integer> enchantments = itemStack.getEnchantments();
                if (enchantments!=null && !enchantments.isEmpty()) {
                    for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                        this.enchantments.put(entry.getKey().getName().toLowerCase(Locale.ENGLISH), entry.getValue());
                    }
                }
            }
        }

        public DesignMark(ResultSet rs) throws SQLException, ParseException {
            regDate = Calendar.getInstance().getTime();
            regDate.setTime(rs.getInt("REG_DATE"));

            designer = rs.getString("DESIGNER");

            String json = rs.getString("SPEC");
            if(json!=null && json.length()>0) {
                JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);

                itemName = (String) jsonObject.get("itemName");
                itemType = (String) jsonObject.get("itemType");
                itemLore = (String) jsonObject.get("itemLore");
                JSONObject jsonObject1 = (JSONObject) jsonObject.get("enchantments");
                if(jsonObject1!=null){
                    jsonObject1.keySet().stream().forEach(key->{
                        Object o = jsonObject1.get(key);
                        if(o instanceof Number) {
                            this.enchantments.put(key.toString(), ((Number) jsonObject1.get(key)).intValue());
                        }
                    });
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof DesignMark){
                DesignMark mark = (DesignMark)obj;
                if(itemName!=null && !itemName.equals(mark.itemName)) return false;
                if(itemType!=null && !itemType.equals(mark.itemType)) return false;
                if(itemLore!=null && !itemLore.equals(mark.itemLore)) return false;
                if(getEnchantments().size() != mark.getEnchantments().size()) return false;
                for (Iterator<String> keys = getEnchantments().keySet().iterator(); keys.hasNext(); ) {
                    String key = keys.next();
                    int v1 = getEnchantments().get(key).intValue();
                    int v2 = mark.getEnchantments().get(key).intValue();
                    if (v1 != v2) return false;
                }
                return true;
            }
            return false;
        }

        public String getLoreString(ItemMeta meta){
            StringBuilder builder = new StringBuilder();
            if(meta.getLore()!=null) {
                for (int i = 0; i < meta.getLore().size(); i++) {
                    if (builder.length() > 0) builder.append(System.lineSeparator());
                    builder.append(meta.getLore().get(i));
                }
            }
            return builder.toString();
        }

        public String getSpec(){
            JSONObject jsonObject = new JSONObject();
            if(itemName!=null) {
                jsonObject.put("itemName", itemName);
            }
            if(itemType!=null) {
                jsonObject.put("itemType", itemType);
            }
            if(itemLore!=null) {
                jsonObject.put("itemLore", itemLore);
            }
            if(getEnchantments().size()>0) {
                jsonObject.put("enchantments", getEnchantments());
            }

            return jsonObject.toJSONString();
        }

        public void setDesigner(Player player){
            this.designer = player.getName();
        }

        public Date getRegDate() {
            return regDate;
        }

        public String getDesigner() {
            return designer;
        }

        public String getItemName() {
            return itemName;
        }

        public String getItemType() {
            return itemType;
        }

        public String getItemLore() {
            return itemLore;
        }

        public Map<String, Integer> getEnchantments() {
            if(enchantments == null) enchantments = new HashMap<>();
            return enchantments;
        }
    }


    public String getDesiginedMarkID(Plugin plugin, ItemStack itemStack){
        ItemMeta meta = itemStack.getItemMeta();
        if(meta!=null){
            String id = meta.getPersistentDataContainer().get(
                    new NamespacedKey(plugin, "designMark"),
                    PersistentDataType.STRING);

            return id;
        }

        return null;
    }

    private DesignMark createDesignMark(ItemStack stack){
        return new DesignMark(stack);
    }

    public boolean checkName(ItemStack itemStack){
        String name = escape(itemStack.getItemMeta().getDisplayName());
        if(getConnection()!=null){
            try(Statement stmt = getConnection().createStatement()){
                //"select * from syain where id =? ;";
                StringBuilder builder = new StringBuilder();
                builder.append("SELECT ID FROM DESIGNMARK WHERE ITEM_NAME = '"+name+"'");

                try(ResultSet rs = stmt.executeQuery(builder.toString())){
                    return rs.next() == false;
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "検索に失敗した", e);
            }
        }
        return false;
    }

    public DesignMark getDesignMark(String id){
        DesignMark mark = null;
        if(getConnection()!=null){
            try(Statement stmt = getConnection().createStatement()){
                //"select * from syain where id =? ;";
                StringBuilder builder = new StringBuilder();
                builder.append("SELECT * FROM DESIGNMARK WHERE ID = "+id);

                try(ResultSet rs = stmt.executeQuery(builder.toString())){
                    mark = new DesignMark(rs);
                } catch (ParseException e) {
                    LOGGER.log(Level.WARNING, "意匠情報の取得に失敗した", e);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "検索に失敗した", e);
            }
        }
        return mark;
    }

    public String getDesigner(String id){
        if(getConnection()!=null){
            try(Statement stmt = getConnection().createStatement()){
                //"select * from syain where id =? ;";
                StringBuilder builder = new StringBuilder();
                builder.append("SELECT DESIGNER FROM DESIGNMARK WHERE ID = "+id);

                try(ResultSet rs = stmt.executeQuery(builder.toString())){
                    return rs.getString(1);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "検索に失敗した", e);
            }
        }
        return null;
    }

    public String escape(String text){
        return JSONValue.escape(text);
    }

    private long insertDb(Player player, ItemStack stack){
        if(getConnection()!=null){
            DesignMark mark = new DesignMark(stack);
            mark.setDesigner(player);

            String sql = "INSERT INTO DESIGNMARK( REG_DATE, DESIGNER, ITEM_NAME, SPEC ) VALUES( ?, ?, ?, ?)";
            try(PreparedStatement stmt = getConnection().prepareStatement(sql)){
                stmt.setLong(1, mark.getRegDate().getTime());
                stmt.setString(2, mark.getDesigner());
                stmt.setString(3, escape(mark.getItemName()));
                stmt.setBytes(4, mark.getSpec().getBytes());
                stmt.executeUpdate();
                try(ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "データの挿入に失敗した", e);
            }
        }
        return -1;
    }

    public ItemStack registrationDesign(Player player, Plugin plugin, ItemStack itemStack){
        String check = getDesiginedMarkID(plugin, itemStack);
        if(check != null) return null;

        long id = insertDb(player, itemStack);
        if(id != -1) {
            ItemMeta meta = itemStack.getItemMeta();
            String key = Long.toString(id);
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "designMark"),
                    PersistentDataType.STRING,
                    key);
            itemStack.setItemMeta(meta);
            return itemStack;
        }

        return null;
    }

    public boolean checkDesignMark(Plugin plugin, ItemStack itemStack1){
        String key = getDesiginedMarkID(plugin, itemStack1);
        if(key == null) return false;

        DesignMark mark1 = getDesignMark(key);
        if(mark1 == null) return false;

        DesignMark mark2 = createDesignMark(itemStack1);
        if(mark2 == null) return false;

        return mark1.equals(mark2);
    }
}
