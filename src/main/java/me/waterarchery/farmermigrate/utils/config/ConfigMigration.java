package me.waterarchery.farmermigrate.utils.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.geik.farmer.Main;
import xyz.geik.farmer.api.FarmerAPI;
import xyz.geik.farmer.shades.storage.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.0
 * @author poyrazinan
 */
public class ConfigMigration {

    /**
     * Migrate config.yml to v6 config also saves addon settings
     *
     * @since 1.0
     * @author poyrazinan
     */
    public ConfigMigration() {
        Config config = Main.getConfigFile();
        config.set("lang", "tr");
        File cfgFile = new File("plugins/Ciftci/config.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(cfgFile);
        boolean dropCancel = cfg.getBoolean("Settings.playerDropCancel");
        List<String> worlds = cfg.getStringList("Settings.defaultWorld");
        boolean voucher = cfg.getBoolean("Settings.buyFarmer.feature");
        int farmerPrice = cfg.getInt("Settings.buyFarmer.price");
        config.set("settings.buyFarmer", voucher);
        config.set("settings.farmerPrice", farmerPrice);
        config.set("settings.ignorePlayerDrop", dropCancel);
        config.set("settings.worlds", worlds);


        // TAX SETTINGS
        double taxRate = cfg.getDouble("tax.taxRate");
        boolean deposit = cfg.getBoolean("tax.depositToAcc");
        String taxAccount = cfg.getString("tax.taxAcc");
        config.set("tax.rate", taxRate);
        config.set("tax.deposit", deposit);
        config.set("tax.depositUser", taxAccount);

        long levelPrice = -1;
        // Levels
        config.set("levels", "");
        for (String key : cfg.getConfigurationSection("FarmerLevels").getKeys(false)) {
            int capacity = cfg.getInt("FarmerLevels." + key + ".Capacity");
            double levelTax = cfg.contains("FarmerLevels." + key + ".taxRate")
                    ? cfg.getDouble("FarmerLevels." + key + ".taxRate") : -1;
            String permission = cfg.contains("FarmerLevels." + key + ".permission")
                    ? cfg.getString("FarmerLevels." + key + ".permission") : null;
            config.set("levels." + key + ".capacity", capacity);
            if (levelTax != -1)
                config.set("levels." + key + ".tax", levelTax);
            if (permission != null)
                config.set("levels." + key + ".reqPerm", permission);
            if (levelPrice != -1) {
                config.set("levels." + key + ".reqMoney", 0);
            }
            levelPrice = cfg.getLong("FarmerLevels." + key + ".nextRankMoney");
        }

        // Addons
        // SpawnerKiller
        boolean spawnerKiller = cfg.contains("AddonSettings.spawnerKiller");
        if (spawnerKiller) {
            boolean withoutFarmer = cfg.getBoolean("AddonSettings.spawnerKiller.withoutFarmer");
            boolean cookFood = cfg.getBoolean("AddonSettings.spawnerKiller.cookFood");
            boolean removeMob = cfg.getBoolean("DetailedSettings.removeMob");
            List<String> blackList = new ArrayList<>();
            if (cfg.contains("AddonSettings.spawnerKiller.blackList"))
                cfg.getStringList("AddonSettings.spawnerKiller.blackList").forEach(blackList::add);
            List<String> whiteList = new ArrayList<>();
            if (cfg.contains("AddonSettings.spawnerKiller.whiteList"))
                cfg.getStringList("AddonSettings.spawnerKiller.whiteList").forEach(whiteList::add);

            Config spawnerKillerYml = FarmerAPI.getModuleManager().getByName("SpawnerKiller").getConfig();
            spawnerKillerYml.set("settings.removeMob", removeMob);
            spawnerKillerYml.set("settings.feature", true);
            spawnerKillerYml.set("settings.requireFarmer", !withoutFarmer);
            spawnerKillerYml.set("settings.cookFoods", cookFood);
            if (!blackList.isEmpty())
                spawnerKillerYml.set("settings.blacklist", blackList);
            if (!whiteList.isEmpty())
                spawnerKillerYml.set("settings.whitelist", whiteList);

        }

        // AutoSell
        boolean autoSell = cfg.getBoolean("AddonSettings.autoSell.feature");
        FarmerAPI.getModuleManager().getByName("AutoSeller").getConfig().set("settings.feature", autoSell);

        // AutoHarvest
        boolean autoHarvest = cfg.getBoolean("AddonSettings.autoCollect.feature");
        Config autoHarvestYml = FarmerAPI.getModuleManager().getByName("AutoHarvest").getConfig();

        boolean withoutFarmer = cfg.getBoolean("AddonSettings.autoCollect.withoutFarmer");
        boolean requirePiston = cfg.getBoolean("AddonSettings.autoCollect.requirePiston");

        autoHarvestYml.set("settings.feature", autoHarvest);
        autoHarvestYml.set("settings.withoutFarmer", withoutFarmer);
        autoHarvestYml.set("settings.requirePiston", requirePiston);

        FileConfiguration itemsCfg = YamlConfiguration.loadConfiguration(new File("plugins/Ciftci/items.yml"));
        List<String> items = new ArrayList<>();
        for (String key : itemsCfg.getConfigurationSection("Items").getKeys(false)) {
            if (itemsCfg.contains("Items." + key + "autoCollect") && itemsCfg.getBoolean("Items." + key + ".autoCollect"))
                items.add(itemsCfg.getString("Items." + key + ".material"));
        }
        autoHarvestYml.set("settings.items", items);
        // TODO : Voucher item calculation maybe if it's necessary
    }


}
