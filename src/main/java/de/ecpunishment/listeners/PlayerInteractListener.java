package de.ecpunishment.listeners;

import de.ecpunishment.EcPunishment;
import de.ecpunishment.commands.StaffmodeCommand;
import de.ecpunishment.gui.PunishmentGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerInteractListener implements Listener {
    
    private final EcPunishment plugin;
    private final Map<UUID, Boolean> vanishedPlayers;
    
    public PlayerInteractListener(EcPunishment plugin) {
        this.plugin = plugin;
        this.vanishedPlayers = new HashMap<>();
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is softbanned
        if (plugin.getPunishmentManager().isSoftbanned(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§cDu bist softbanned und kannst keine Aktionen ausführen!");
            return;
        }
        
        // Check if player is frozen
        if (plugin.getPunishmentManager().isFrozen(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        
        // Handle staff mode items
        if (plugin.getStaffmodeCommand().isInStaffMode(player.getUniqueId())) {
            handleStaffModeInteract(event);
        }
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        
        // Only handle if target is a player and staff is in staff mode
        if (!(event.getRightClicked() instanceof Player) || 
            !plugin.getStaffmodeCommand().isInStaffMode(player.getUniqueId())) {
            return;
        }
        
        Player target = (Player) event.getRightClicked();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        
        event.setCancelled(true);
        
        switch (item.getType()) {
            case STICK:
                handleFreezePlayer(player, target);
                break;
            case BOOK:
                handlePlayerInfo(player, target);
                break;
            case PAPER:
                handlePunishmentGUI(player, target);
                break;
            case CLOCK:
                handleWatchPlayer(player, target);
                break;
        }
    }
    
    private void handleStaffModeInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && 
            event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        
        event.setCancelled(true);
        
        switch (item.getType()) {
            case COMPASS:
                handleRandomTeleport(player);
                break;
            case ENDER_EYE:
                handleVanishToggle(player);
                break;
            case BARRIER:
                player.performCommand("staffmode");
                break;
        }
    }
    
    private void handleFreezePlayer(Player staff, Player target) {
        if (plugin.getPunishmentManager().isFrozen(target.getUniqueId())) {
            plugin.getPunishmentManager().unfreezePlayer(target.getUniqueId());
            staff.sendMessage("§a" + target.getName() + " wurde aufgetaut!");
            target.sendMessage("§aDu wurdest aufgetaut!");
        } else {
            plugin.getPunishmentManager().freezePlayer(
                target.getUniqueId(), target.getName(),
                staff.getUniqueId(), staff.getName()
            );
            staff.sendMessage("§c" + target.getName() + " wurde eingefroren!");
            target.sendMessage("§cDu wurdest eingefroren! Bewege dich nicht!");
        }
    }
    
    private void handlePlayerInfo(Player staff, Player target) {
        staff.sendMessage("§8§l======= §6Info über " + target.getName() + " §8§l=======");
        staff.sendMessage("§7Name: §f" + target.getName());
        staff.sendMessage("§7UUID: §f" + target.getUniqueId());
        staff.sendMessage("§7IP: §f" + target.getAddress().getAddress().getHostAddress());
        staff.sendMessage("§7Gamemode: §f" + target.getGameMode());
        staff.sendMessage("§7Health: §f" + target.getHealth() + "/" + target.getMaxHealth());
        staff.sendMessage("§7Food: §f" + target.getFoodLevel());
        staff.sendMessage("§7Level: §f" + target.getLevel());
        staff.sendMessage("§7Flying: §f" + (target.isFlying() ? "Ja" : "Nein"));
        
        // Check punishment status
        boolean isBanned = plugin.getPunishmentManager().isBanned(target.getUniqueId());
        boolean isMuted = plugin.getPunishmentManager().isMuted(target.getUniqueId());
        boolean isFrozen = plugin.getPunishmentManager().isFrozen(target.getUniqueId());
        boolean isJailed = plugin.getJailManager().isJailed(target.getUniqueId());
        
        staff.sendMessage("§7Status: " + 
            (isBanned ? "§cGebannt " : "") +
            (isMuted ? "§6Stumm " : "") + 
            (isFrozen ? "§bEingefroren " : "") +
            (isJailed ? "§5Im Gefängnis " : "") +
            ((!isBanned && !isMuted && !isFrozen && !isJailed) ? "§aKeine Bestrafungen" : ""));
    }
    
    private void handlePunishmentGUI(Player staff, Player target) {
        PunishmentGUI gui = new PunishmentGUI(plugin);
        gui.openPunishmentMenu(staff, target);
    }
    
    private void handleWatchPlayer(Player staff, Player target) {
        StaffmodeCommand staffCmd = plugin.getStaffmodeCommand();
        
        Player currentlyWatched = staffCmd.getWatchedPlayer(staff.getUniqueId());
        
        if (currentlyWatched != null && currentlyWatched.equals(target)) {
            // Stop watching
            staffCmd.removeWatchedPlayer(staff.getUniqueId());
            staff.sendMessage("§7Du beobachtest §c" + target.getName() + " §7nicht mehr.");
            return;
        }
        
        // Start watching
        staffCmd.setWatchedPlayer(staff.getUniqueId(), target);
        staff.teleport(target.getLocation());
        
        // Make staff invisible to target
        target.hidePlayer(plugin, staff);
        
        staff.sendMessage("§7Du beobachtest jetzt §a" + target.getName() + "§7.");
        staff.sendMessage("§7Du bist für diesen Spieler unsichtbar.");
    }
    
    private void handleRandomTeleport(Player player) {
        List<Player> onlinePlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player) && !plugin.getStaffmodeCommand().isInStaffMode(p.getUniqueId())) {
                onlinePlayers.add(p);
            }
        }
        
        if (onlinePlayers.isEmpty()) {
            player.sendMessage("§cKeine Spieler zum Teleportieren verfügbar!");
            return;
        }
        
        Player randomPlayer = onlinePlayers.get(new Random().nextInt(onlinePlayers.size()));
        player.teleport(randomPlayer.getLocation());
        player.sendMessage("§7Teleportiert zu §a" + randomPlayer.getName() + "§7.");
    }
    
    private void handleVanishToggle(Player player) {
        boolean isVanished = vanishedPlayers.getOrDefault(player.getUniqueId(), false);
        
        if (isVanished) {
            // Show player to everyone
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(plugin, player);
            }
            vanishedPlayers.put(player.getUniqueId(), false);
            player.sendMessage("§7Du bist jetzt §asichtbar§7.");
        } else {
            // Hide player from non-staff
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!plugin.getStaffmodeCommand().isInStaffMode(p.getUniqueId())) {
                    p.hidePlayer(plugin, player);
                }
            }
            vanishedPlayers.put(player.getUniqueId(), true);
            player.sendMessage("§7Du bist jetzt §cunsichtbar§7.");
        }
    }
    
    public boolean isVanished(UUID playerUuid) {
        return vanishedPlayers.getOrDefault(playerUuid, false);
    }
    
    public void setVanished(UUID playerUuid, boolean vanished) {
        vanishedPlayers.put(playerUuid, vanished);
    }
}
