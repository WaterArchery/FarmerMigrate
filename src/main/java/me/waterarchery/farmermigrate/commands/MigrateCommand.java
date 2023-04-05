package me.waterarchery.farmermigrate.commands;

import me.waterarchery.farmermigrate.FarmerMigrate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MigrateCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (cmd.getName().equalsIgnoreCase("farmermigrate")) {
            if (args.length == 0) {
                if (sender.hasPermission("farmermigrate.admin")) {
                    FarmerMigrate.getOldDatabase().migrateDatabase();
                }
            }
        }
        return false;
    }
}
