package me.waterarchery.farmermigrate;

import me.waterarchery.farmermigrate.commands.MigrateCommand;
import me.waterarchery.farmermigrate.utils.sql.Database;
import me.waterarchery.farmermigrate.utils.sql.SQLite;
import org.bukkit.plugin.java.JavaPlugin;

public final class FarmerMigrate extends JavaPlugin {

    private static FarmerMigrate instance;
    private static Database oldDatabase;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginCommand("farmermigrate").setExecutor(new MigrateCommand());
        oldDatabase = new SQLite(this);
        oldDatabase.load();
    }

    @Override
    public void onDisable() {

    }

    public static FarmerMigrate getInstance() { return instance; }
    public static Database getOldDatabase() { return oldDatabase; }

}
