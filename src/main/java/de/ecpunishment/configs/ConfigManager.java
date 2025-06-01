package de.ecpunishment.configs;

import de.ecpunishment.EcPunishment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    
    private final EcPunishment plugin;
    private File configFile;
    private FileConfiguration config;
    
    public ConfigManager(EcPunishment plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }
    
    public void loadConfig() {
        if (!configFile.exists()) {
            createDefaultConfig();
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    private void createDefaultConfig() {
        plugin.getDataFolder().mkdirs();
        
        try {
            configFile.createNewFile();
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(configFile);
            
            // General settings
            defaultConfig.set("general.database.type", "sqlite");
            defaultConfig.set("general.database.file", "database.db");
            defaultConfig.set("general.language", "de");
            defaultConfig.set("general.date-format", "dd.MM.yyyy HH:mm");
            
            // Default durations (in milliseconds)
            defaultConfig.set("defaults.warn-duration", -1); // permanent
            defaultConfig.set("defaults.mute-duration", 3600000); // 1 hour
            defaultConfig.set("defaults.ban-duration", 86400000); // 1 day
            defaultConfig.set("defaults.jail-duration", 1800000); // 30 minutes
            
            // Jail settings
            defaultConfig.set("jail.world", "world");
            defaultConfig.set("jail.x", 0.0);
            defaultConfig.set("jail.y", 100.0);
            defaultConfig.set("jail.z", 0.0);
            defaultConfig.set("jail.yaw", 0.0);
            defaultConfig.set("jail.pitch", 0.0);
            
            // Staff mode settings
            defaultConfig.set("staffmode.vanish-on-enable", true);
            defaultConfig.set("staffmode.fly-on-enable", true);
            defaultConfig.set("staffmode.gamemode", "CREATIVE");
            
            // GUI settings
            defaultConfig.set("gui.punishment-gui-size", 54);
            defaultConfig.set("gui.history-gui-size", 54);
            
            defaultConfig.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create default config: " + e.getMessage());
        }
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public String getString(String path) {
        return config.getString(path);
    }
    
    public int getInt(String path) {
        return config.getInt(path);
    }
    
    public long getLong(String path) {
        return config.getLong(path);
    }
    
    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }
    
    public double getDouble(String path) {
        return config.getDouble(path);
    }
}
