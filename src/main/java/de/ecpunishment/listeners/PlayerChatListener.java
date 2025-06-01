package de.ecpunishment.listeners;

import de.ecpunishment.EcPunishment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {
    
    private final EcPunishment plugin;
    
    public PlayerChatListener(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Check if player is muted
        if (plugin.getPunishmentManager().isMuted(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessagesConfig().getMessage("mute.chat-blocked"));
            return;
        }
        
        // Check if player is shadowmuted
        if (plugin.getPunishmentManager().isShadowmuted(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            // Don't notify the player - they think their message went through
            return;
        }
        
        // Check if player is softbanned
        if (plugin.getPunishmentManager().isSoftbanned(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessagesConfig().getMessage("softban.action-blocked"));
            return;
        }
    }
}
