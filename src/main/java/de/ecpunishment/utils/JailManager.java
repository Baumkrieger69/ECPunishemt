package de.ecpunishment.utils;

import de.ecpunishment.EcPunishment;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JailManager {
    
    private final EcPunishment plugin;
    private final Map<UUID, Location> jailedPlayers;
    private final Map<UUID, Location> previousLocations;
    private Location jailLocation;
    private File jailFile;
    private FileConfiguration jailConfig;
    
    public JailManager(EcPunishment plugin) {
        this.plugin = plugin;
        this.jailedPlayers = new HashMap<>();
        this.previousLocations = new HashMap<>();
        loadJailConfig();
    }
    
    private void loadJailConfig() {
        jailFile = new File(plugin.getDataFolder(), "jail.yml");
        if (!jailFile.exists()) {
            try {
                jailFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create jail.yml: " + e.getMessage());
            }
        }
        
        jailConfig = YamlConfiguration.loadConfiguration(jailFile);
        
        // Load jail location if set
        if (jailConfig.contains("jail.world")) {
            String worldName = jailConfig.getString("jail.world");
            double x = jailConfig.getDouble("jail.x");
            double y = jailConfig.getDouble("jail.y");
            double z = jailConfig.getDouble("jail.z");
            float yaw = (float) jailConfig.getDouble("jail.yaw");
            float pitch = (float) jailConfig.getDouble("jail.pitch");
            
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                jailLocation = new Location(world, x, y, z, yaw, pitch);
                plugin.getLogger().info("Jail location loaded: " + worldName + " " + x + " " + y + " " + z);
            } else {
                plugin.getLogger().warning("Jail world '" + worldName + "' not found!");
            }
        }
    }
    
    public void saveJailConfig() {
        try {
            jailConfig.save(jailFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save jail.yml: " + e.getMessage());
        }
    }
    
    public void setJailLocation(Location location) {
        this.jailLocation = location;
        
        jailConfig.set("jail.world", location.getWorld().getName());
        jailConfig.set("jail.x", location.getX());
        jailConfig.set("jail.y", location.getY());
        jailConfig.set("jail.z", location.getZ());
        jailConfig.set("jail.yaw", location.getYaw());
        jailConfig.set("jail.pitch", location.getPitch());
        
        saveJailConfig();
        plugin.getLogger().info("Jail location set to: " + location.getWorld().getName() + 
                              " " + location.getX() + " " + location.getY() + " " + location.getZ());
    }
    
    public Location getJailLocation() {
        return jailLocation;
    }
    
    public boolean hasJailLocation() {
        return jailLocation != null;
    }
    
    public void jailPlayer(Player player) {
        if (!hasJailLocation()) {
            plugin.getLogger().warning("Cannot jail player " + player.getName() + ": No jail location set!");
            return;
        }
        
        UUID playerUuid = player.getUniqueId();
        
        // Save current location
        previousLocations.put(playerUuid, player.getLocation().clone());
        
        // Add to jailed players
        jailedPlayers.put(playerUuid, jailLocation.clone());
        
        // Teleport to jail
        player.teleport(jailLocation);
        
        plugin.getLogger().info("Player " + player.getName() + " has been jailed.");
    }
    
    public void unjailPlayer(Player player) {
        UUID playerUuid = player.getUniqueId();
        
        if (!jailedPlayers.containsKey(playerUuid)) {
            return;
        }
        
        // Remove from jailed players
        jailedPlayers.remove(playerUuid);
        
        // Teleport back to previous location if available
        Location previousLocation = previousLocations.remove(playerUuid);
        if (previousLocation != null && previousLocation.getWorld() != null) {
            player.teleport(previousLocation);
        } else {
            // Fallback to spawn
            player.teleport(player.getWorld().getSpawnLocation());
        }
        
        plugin.getLogger().info("Player " + player.getName() + " has been unjailed.");
    }
    
    public boolean isJailed(UUID playerUuid) {
        return jailedPlayers.containsKey(playerUuid);
    }
    
    public boolean isJailed(Player player) {
        return isJailed(player.getUniqueId());
    }
    
    public boolean isInJailArea(Location location) {
        if (!hasJailLocation()) {
            return false;
        }
        
        // Check if player is within 10 blocks of jail location
        return location.getWorld().equals(jailLocation.getWorld()) &&
               location.distance(jailLocation) <= 10.0;
    }
    
    public void teleportToJail(Player player) {
        if (hasJailLocation() && isJailed(player)) {
            player.teleport(jailLocation);
        }
    }
    
    public Map<UUID, Location> getJailedPlayers() {
        return new HashMap<>(jailedPlayers);
    }
    
    public void loadJailedPlayer(UUID playerUuid) {
        // Called when a jailed player joins the server
        jailedPlayers.put(playerUuid, jailLocation != null ? jailLocation.clone() : null);
    }
    
    public void removeJailedPlayer(UUID playerUuid) {
        jailedPlayers.remove(playerUuid);
        previousLocations.remove(playerUuid);
    }
}
