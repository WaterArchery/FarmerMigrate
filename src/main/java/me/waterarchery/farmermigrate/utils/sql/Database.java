package me.waterarchery.farmermigrate.utils.sql;

import me.waterarchery.farmermigrate.FarmerMigrate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandException;
import xyz.geik.farmer.Main;
import xyz.geik.farmer.api.FarmerAPI;
import xyz.geik.farmer.api.managers.DatabaseManager;
import xyz.geik.farmer.api.managers.FarmerManager;
import xyz.geik.farmer.database.DBConnection;
import xyz.geik.farmer.model.Farmer;
import xyz.geik.farmer.modules.FarmerModule;
import xyz.geik.farmer.shades.storage.Config;
import xyz.geik.farmer.shades.xseries.XMaterial;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getServer;

public abstract class Database {

    FarmerMigrate instance;
    Connection connection;
    public String table = "Tablo";

    public Database(FarmerMigrate instance){
        this.instance = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize(){
        connection = getSQLConnection();
        PreparedStatement ps1 = null;
        try{
            final String query = "SELECT * FROM " + table;
            /*
            databaseyi klasöre atmazsa
            connection nullanıyor
            onun için kontrol
             */
            if (connection != null) {
                ps1 = connection.prepareStatement(query);
                ps1.execute();
            }
        } catch (SQLException ex) {
            instance.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
        finally {
            try {
                if (ps1 != null) { ps1.close(); }
            }
            catch (SQLException ignored){}
        }
    }

    public void migrateDatabase() {
        final String query = "SELECT * FROM " + table;
        PreparedStatement ps = null;
        Connection connection = getSQLConnection();
        try {
            /*
            klasörün içine eski db dosyasını
            atmayanlar için connection kontrolü
            null ise geri dönüyor ve uyarıyor
             */
            if (connection == null) { return; }
            ps = connection.prepareStatement(query);
            ResultSet resultSet = ps.executeQuery();
            Connection farmerConnection = DBConnection.connect();
            while (resultSet.next()) {
                HashMap<Material, Long> materialHash = new HashMap<>();
                ResultSetMetaData rsData = resultSet.getMetaData();
                for (int i = 0; i < rsData.getColumnCount(); i++) {
                    /*
                    sütun isimlerine göre
                    material varsa bunun sayısını
                    çekip çiftçiye ekliyor
                     */
                    String columnName = rsData.getColumnName(i + 1);
                    Material material = Material.getMaterial(columnName.toUpperCase());
                    if (material != null && material != Material.AIR) {
                        try {
                            long count = resultSet.getLong(columnName);
                            materialHash.put(material, count);
                        }
                        catch (Exception ignored){}
                    }
                }
                String owner = resultSet.getString("Owner");
                int level = resultSet.getInt("farmerLvl");
                int id = resultSet.getInt("farmerID");
                int autoSell = resultSet.getInt("autoSell");
                int autoCollect = resultSet.getInt("autoCollect");
                int spawnerKill = resultSet.getInt("spawnerKill");
                String locationTxt = resultSet.getString("farmerLocation");
                if (id != 96456) {
                    Location loc = buildLocation(locationTxt.split("/"));
                    String regionID = Main.getIntegration().getRegionID(loc);
                    if (regionID != null) {
                        if (FarmerAPI.getFarmerManager().hasFarmer(loc)) {
                            /*
                            regionId kayıtlı bir farmer varsa
                            sadece eşyaları ekliyor v5 üzerinden
                             */
                            boolean found = false;
                            for (Farmer farmer : FarmerManager.getFarmers().values()) {
                                if (farmer.getRegionID().equalsIgnoreCase(regionID)) {
                                    addItemsToFarmer(farmer, materialHash);
                                    Bukkit.getConsoleSender().sendMessage("§eFarmer Migrate - " + owner + " oyuncusunun sahip olduğu " + id + " idli çiftçi mevcut olduğu için sadece eşyalar eklendi.");
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                Bukkit.getConsoleSender().sendMessage("§eFarmer Migrate - " + owner + " oyuncusunun sahip olduğu " + id + " idli çiftçi mevcut olduğu için atlanıyor.");
                            }
                            continue;
                        }
                        if (!getV6Levels().contains(level)) {
                            Bukkit.getConsoleSender().sendMessage("§dFarmer Migrate - Çifti verisinde " + level + " seviyesi bulunamadı.");
                            continue;
                        }
                        Farmer farmer = new Farmer(regionID, UUID.fromString(owner), level);
                        addItemsToFarmer(farmer, materialHash);
                        /*
                        modül ekleme
                         */
                        if (autoSell == 1) {
                            farmer.changeAttribute("autoseller");
                        }
                        if (autoCollect == 1) {
                            farmer.changeAttribute("autoharvest");
                        }
                        if (spawnerKill == 1) {
                            farmer.changeAttribute("spawnerkiller");
                        }
                        /*
                        farmer bağlantısı ile
                        verileri güncelleme
                         */
                        farmer.saveFarmer(farmerConnection);
                        Bukkit.getConsoleSender().sendMessage("§aFarmer Migrate - " + owner + " oyuncusunun sahip olduğu " + id + " idli çiftçi kayıt edildi.");
                    }
                    else {
                        Bukkit.getConsoleSender().sendMessage("§cFarmer Migrate - " + owner + " oyuncusunun sahip olduğu " + id + " idli çiftçinin lokasyonu bulunamadı.");
                    }
                }
            }
        }
        catch (SQLException exception){
            exception.printStackTrace();
        }
        finally {
            try {
                if (ps != null) { ps.close(); }
            }
            catch (SQLException ignored){}
        }
    }

    public void addItemsToFarmer(Farmer farmer,  HashMap<Material, Long> materialHash) {
        for (Material material : materialHash.keySet()) {
            long amount = materialHash.get(material);
            try {
                farmer.getInv().forceSumItem(XMaterial.matchXMaterial(material), amount);
            }
            catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("§cFarmer Migrate - " + farmer.getRegionID() + " idli çiftçiye " + material.name() + " eşyası eklenemedi.");
            }
        }
    }

    public Location buildLocation(String[] parts) {
        String world = parts[0];
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        return new Location(getServer().getWorld(world), x, y, z);
    }

    ArrayList<Integer> v6Levels;
    public ArrayList<Integer> getV6Levels() {
        // Old Levels
        if (v6Levels == null) {
            v6Levels = new ArrayList<>();
            Config config = Main.getConfigFile();
            for (String key : config.getSection("levels").singleLayerKeySet()) {
                try {
                    v6Levels.add(Integer.parseInt(key));
                } catch (NumberFormatException ex) {
                    Bukkit.getConsoleSender().sendMessage("§cFarmer Migrate - Çiftçi eklentisinde bulunan seviye " + key + " bir sayı değil");
                }
            }
            return v6Levels;
        }
        return v6Levels;
    }

}