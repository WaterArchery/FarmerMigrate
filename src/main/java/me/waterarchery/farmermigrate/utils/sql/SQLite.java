package me.waterarchery.farmermigrate.utils.sql;

import me.waterarchery.farmermigrate.FarmerMigrate;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;



public class SQLite extends Database{

    String dbname;

    public SQLite(FarmerMigrate instance){
        super(instance);
        dbname = "Database";
    }

    public Connection getSQLConnection() {
        File dataFile = new File(instance.getDataFolder(), dbname + ".db");
        File folder = new File(instance.getDataFolder(), "");
        if (!folder.exists()) { folder.mkdir(); }
        if (!dataFile.exists()){
            Bukkit.getConsoleSender().sendMessage("§cEski Çiftçi v5 Database.db dosyanızı FarmerMigrate klasörünün içerisine atın.");
            return null;
        }
        try {
            if(connection != null && !connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFile);
            return connection;
        } catch (SQLException ex) {
            instance.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            instance.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    public void load() {
        connection = getSQLConnection();
        initialize();
    }
}
