package de.ecpunishment.listeners;

import de.ecpunishment.EcPunishment;
import de.ecpunishment.commands.StaffmodeCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    
    private final EcPunishment plugin;
    
    public PlayerMoveListener(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        // Check if player actually moved (not just head movement)
        if (to == null || (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ())) {
            return;
        }
        
        // Check if player is frozen
        if (plugin.getPunishmentManager().isFrozen(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§cDu bist eingefroren und kannst dich nicht bewegen!");
            return;
        }
        
        // Check if player is jailed and trying to leave jail area
        if (plugin.getJailManager().isJailed(player.getUniqueId())) {
            if (!plugin.getJailManager().isInJailArea(to)) {
                event.setCancelled(true);
                player.sendMessage("§cDu kannst das Gefängnis nicht verlassen!");
                plugin.getJailManager().teleportToJail(player);
                return;
            }
        }
        
        // Handle staff watching system
        handleStaffWatching(event);
    }
    
    private void handleStaffWatching(PlayerMoveEvent event) {
        Player movedPlayer = event.getPlayer();
        StaffmodeCommand staffCmd = plugin.getStaffmodeCommand();
        
        // Check if any staff member is watching this player
        for (Player staff : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (staffCmd.isInStaffMode(staff.getUniqueId())) {
                Player watchedPlayer = staffCmd.getWatchedPlayer(staff.getUniqueId());
                if (watchedPlayer != null && watchedPlayer.equals(movedPlayer)) {
                    // Teleport the watching staff to the target's new location
                    Location newLocation = event.getTo().clone();
                    newLocation.setY(newLocation.getY() + 1); // Slightly above target
                    staff.teleport(newLocation);
                }
            }
        }
    }
}
