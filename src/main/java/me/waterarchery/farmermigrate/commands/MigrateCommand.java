package me.waterarchery.farmermigrate.commands;

import me.waterarchery.farmermigrate.FarmerMigrate;
import me.waterarchery.farmermigrate.utils.config.ConfigMigration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MigrateCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (cmd.getName().equalsIgnoreCase("farmermigrate")) {
            if (args.length == 0 && sender.hasPermission("farmermigrate.admin")) {
                new ConfigMigration();
                FarmerMigrate.getOldDatabase().migrateDatabase();
            }
            else if (args.length == 1 && args[0].equalsIgnoreCase("async") && sender.hasPermission("farmermigrate.admin")) {
                new ConfigMigration();
                FarmerMigrate.getOldDatabase().migrateDatabase();
            }
        }
        return false;
    }
}
