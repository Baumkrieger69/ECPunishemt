package de.ecpunishment.listeners;

import de.ecpunishment.EcPunishment;
import de.ecpunishment.gui.PunishmentGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {
    
    private final EcPunishment plugin;
    private final PunishmentGUI gui;
    
    public InventoryClickListener(EcPunishment plugin) {
        this.plugin = plugin;
        this.gui = new PunishmentGUI(plugin);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Check if it's a punishment GUI
        if (title.startsWith("§4Bestrafung: §c")) {
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            
            String displayName = clicked.getItemMeta().getDisplayName();
            String targetName = title.replace("§4Bestrafung: §c", "");
            Player target = Bukkit.getPlayer(targetName);
            
            if (target == null) {
                player.sendMessage(plugin.getMessagesConfig().getMessage("general.player-not-found"));
                player.closeInventory();
                return;
            }
            
            // Handle button clicks
            switch (displayName) {
                case "§6Warnung":
                    handleWarnClick(player, target);
                    break;
                case "§6Mute":
                    gui.openDurationGUI(player, "MUTE", target);
                    break;
                case "§4Ban":
                    gui.openDurationGUI(player, "BAN", target);
                    break;
                case "§eKick":
                    handleKickClick(player, target);
                    break;
                case "§8Jail":
                    gui.openDurationGUI(player, "JAIL", target);
                    break;
                case "§bFreeze":
                    handleFreezeClick(player, target);
                    break;
                case "§cSoftban":
                    handleSoftbanClick(player, target);
                    break;
                case "§9Strafhistorie":
                    gui.openHistoryGUI(player, target.getUniqueId(), target.getName());
                    break;
            }
        }
        
        // Check if it's a duration selection GUI
        else if (title.startsWith("§6Dauer wählen: §e")) {
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            
            String displayName = clicked.getItemMeta().getDisplayName();
            
            if (displayName.equals("§cZurück")) {
                // TODO: Go back to main punishment GUI
                player.closeInventory();
                return;
            }
            
            // Handle duration selection
            if (displayName.startsWith("§e")) {
                // TODO: Apply punishment with selected duration
                player.closeInventory();
                player.sendMessage("§aDauer ausgewählt: " + displayName);
            }
        }
        
        // Check if it's a history GUI
        else if (title.startsWith("§9Historie: §b")) {
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            
            String displayName = clicked.getItemMeta().getDisplayName();
            
            if (displayName.equals("§cZurück")) {
                // TODO: Go back to main punishment GUI
                player.closeInventory();
                return;
            }
        }
    }
    
    private void handleWarnClick(Player staff, Player target) {
        String reason = plugin.getReasonsConfig().getDefaultReason("warn");
        plugin.getPunishmentManager().warnPlayer(
                target.getUniqueId(), target.getName(),
                staff.getUniqueId(), staff.getName(), reason);
        
        staff.sendMessage(plugin.getMessagesConfig().getMessage("warn.success")
                .replace("{player}", target.getName())
                .replace("{reason}", reason));
        staff.closeInventory();
    }
    
    private void handleKickClick(Player staff, Player target) {
        String reason = plugin.getReasonsConfig().getDefaultReason("kick");
        plugin.getPunishmentManager().kickPlayer(
                target.getUniqueId(), target.getName(),
                staff.getUniqueId(), staff.getName(), reason);
        
        staff.sendMessage(plugin.getMessagesConfig().getMessage("kick.success")
                .replace("{player}", target.getName())
                .replace("{reason}", reason));
        staff.closeInventory();
    }
    
    private void handleFreezeClick(Player staff, Player target) {
        if (plugin.getPunishmentManager().isFrozen(target.getUniqueId())) {
            plugin.getPunishmentManager().unfreezePlayer(
                    target.getUniqueId(), target.getName(),
                    staff.getUniqueId(), staff.getName());
            staff.sendMessage(plugin.getMessagesConfig().getMessage("freeze.unfrozen-staff")
                    .replace("{player}", target.getName()));
        } else {
            plugin.getPunishmentManager().freezePlayer(
                    target.getUniqueId(), target.getName(),
                    staff.getUniqueId(), staff.getName());
            staff.sendMessage(plugin.getMessagesConfig().getMessage("freeze.frozen-staff")
                    .replace("{player}", target.getName()));
        }
        staff.closeInventory();
    }
    
    private void handleSoftbanClick(Player staff, Player target) {
        plugin.getPunishmentManager().softbanPlayer(
                target.getUniqueId(), target.getName(),
                staff.getUniqueId(), staff.getName());
        
        staff.sendMessage(plugin.getMessagesConfig().getMessage("softban.success")
                .replace("{player}", target.getName()));
        staff.closeInventory();
    }
}
