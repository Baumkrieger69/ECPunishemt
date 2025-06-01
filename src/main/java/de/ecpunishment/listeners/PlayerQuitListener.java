package de.ecpunishment.listeners;

import de.ecpunishment.EcPunishment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    
    private final EcPunishment plugin;
    
    public PlayerQuitListener(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Stop watching if player is being watched
        plugin.getPunishmentManager().stopWatching(event.getPlayer().getUniqueId());
        
        // TODO: Save player data to database
    }
}
