package de.ecpunishment.data;

import de.ecpunishment.EcPunishment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PunishmentManager {
    
    private final EcPunishment plugin;
    private final DatabaseManager databaseManager;
    private final Set<UUID> frozenPlayers;
    private final Set<UUID> jailedPlayers;
    private final Set<UUID> softbannedPlayers;
    private final Set<UUID> shadowmutedPlayers;
    private final Map<UUID, UUID> watchingPlayers; // watcher -> target
    
    public PunishmentManager(EcPunishment plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.frozenPlayers = new HashSet<>();
        this.jailedPlayers = new HashSet<>();
        this.softbannedPlayers = new HashSet<>();
        this.shadowmutedPlayers = new HashSet<>();
        this.watchingPlayers = new HashMap<>();
    }
    
    public void warnPlayer(UUID playerUuid, String playerName, UUID punisherUuid, String punisherName, String reason) {
        Punishment punishment = new Punishment(playerUuid, playerName, punisherUuid, punisherName, 
                Punishment.PunishmentType.WARN, reason, -1);
        databaseManager.savePunishment(punishment);
        
        // Log staff action
        databaseManager.logStaffAction(punisherUuid, punisherName, "WARN", playerUuid, playerName, reason);
        
        // Check for escalation
        plugin.getEscalationManager().checkEscalation(playerUuid, playerName, Punishment.PunishmentType.WARN);
        
        // Notify player if online
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            player.sendMessage(plugin.getMessagesConfig().getMessage("warn.received")
                    .replace("{reason}", reason)
                    .replace("{punisher}", punisherName));
        }
    }
    
    public void mutePlayer(UUID playerUuid, String playerName, UUID punisherUuid, String punisherName, String reason, long duration) {
        Punishment punishment = new Punishment(playerUuid, playerName, punisherUuid, punisherName, 
                Punishment.PunishmentType.MUTE, reason, duration);
        databaseManager.savePunishment(punishment);
        
        // Log staff action
        String durationStr = duration == -1 ? "permanent" : String.valueOf(duration);
        databaseManager.logStaffAction(punisherUuid, punisherName, "MUTE", playerUuid, playerName, 
                reason + " (Duration: " + durationStr + ")");
        
        // Notify player if online
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            String message = duration == -1 ? 
                    plugin.getMessagesConfig().getMessage("mute.permanent") :
                    plugin.getMessagesConfig().getMessage("mute.temporary");
            player.sendMessage(message
                    .replace("{reason}", reason)
                    .replace("{punisher}", punisherName)
                    .replace("{duration}", formatDuration(duration)));
        }
    }
    
    public void banPlayer(UUID playerUuid, String playerName, UUID punisherUuid, String punisherName, String reason, long duration) {
        Punishment punishment = new Punishment(playerUuid, playerName, punisherUuid, punisherName, 
                Punishment.PunishmentType.BAN, reason, duration);
        databaseManager.savePunishment(punishment);
        
        // Log staff action
        String durationStr = duration == -1 ? "permanent" : String.valueOf(duration);
        databaseManager.logStaffAction(punisherUuid, punisherName, "BAN", playerUuid, playerName, 
                reason + " (Duration: " + durationStr + ")");
        
        // Kick player if online
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            String message = duration == -1 ? 
                    plugin.getMessagesConfig().getMessage("ban.permanent") :
                    plugin.getMessagesConfig().getMessage("ban.temporary");
            player.kickPlayer(message
                    .replace("{reason}", reason)
                    .replace("{punisher}", punisherName)
                    .replace("{duration}", formatDuration(duration)));
        }
    }
    
    public void kickPlayer(UUID playerUuid, String playerName, UUID punisherUuid, String punisherName, String reason) {
        Punishment punishment = new Punishment(playerUuid, playerName, punisherUuid, punisherName, 
                Punishment.PunishmentType.KICK, reason, 0);
        databaseManager.savePunishment(punishment);
        
        // Log staff action
        databaseManager.logStaffAction(punisherUuid, punisherName, "KICK", playerUuid, playerName, reason);
        
        // Kick player if online
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            player.kickPlayer(plugin.getMessagesConfig().getMessage("kick.message")
                    .replace("{reason}", reason)
                    .replace("{punisher}", punisherName));
        }
    }
    
    public void jailPlayer(UUID playerUuid, String playerName, UUID punisherUuid, String punisherName, long duration) {
        Punishment punishment = new Punishment(playerUuid, playerName, punisherUuid, punisherName, 
                Punishment.PunishmentType.JAIL, "Jailed", duration);
        databaseManager.savePunishment(punishment);
        
        jailedPlayers.add(playerUuid);
        
        // Log staff action
        String durationStr = duration == -1 ? "permanent" : String.valueOf(duration);
        databaseManager.logStaffAction(punisherUuid, punisherName, "JAIL", playerUuid, playerName, 
                "Duration: " + durationStr);
        
        // Teleport player to jail if online
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            // TODO: Implement jail location teleport
            player.sendMessage(plugin.getMessagesConfig().getMessage("jail.jailed")
                    .replace("{punisher}", punisherName)
                    .replace("{duration}", formatDuration(duration)));
        }
    }
    
    public void freezePlayer(UUID playerUuid, String playerName, UUID punisherUuid, String punisherName) {
        frozenPlayers.add(playerUuid);
        
        // Log staff action
        databaseManager.logStaffAction(punisherUuid, punisherName, "FREEZE", playerUuid, playerName, "Frozen");
        
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            player.sendMessage(plugin.getMessagesConfig().getMessage("freeze.frozen")
                    .replace("{punisher}", punisherName));
        }
    }
    
    public void unfreezePlayer(UUID playerUuid, String playerName, UUID punisherUuid, String punisherName) {
        frozenPlayers.remove(playerUuid);
        
        // Log staff action
        databaseManager.logStaffAction(punisherUuid, punisherName, "UNFREEZE", playerUuid, playerName, "Unfrozen");
        
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            player.sendMessage(plugin.getMessagesConfig().getMessage("freeze.unfrozen")
                    .replace("{punisher}", punisherName));
        }
    }
    
    public void softbanPlayer(UUID playerUuid, String playerName, UUID punisherUuid, String punisherName) {
        Punishment punishment = new Punishment(playerUuid, playerName, punisherUuid, punisherName, 
                Punishment.PunishmentType.SOFTBAN, "Softbanned", -1);
        databaseManager.savePunishment(punishment);
        
        softbannedPlayers.add(playerUuid);
        
        // Log staff action
        databaseManager.logStaffAction(punisherUuid, punisherName, "SOFTBAN", playerUuid, playerName, "Softbanned");
        
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            player.sendMessage(plugin.getMessagesConfig().getMessage("softban.applied")
                    .replace("{punisher}", punisherName));
        }
    }
    
    public void shadowmutePlayer(UUID playerUuid, String playerName, UUID punisherUuid, String punisherName) {
        Punishment punishment = new Punishment(playerUuid, playerName, punisherUuid, punisherName, 
                Punishment.PunishmentType.SHADOWMUTE, "Shadowmuted", -1);
        databaseManager.savePunishment(punishment);
        
        shadowmutedPlayers.add(playerUuid);
        
        // Log staff action
        databaseManager.logStaffAction(punisherUuid, punisherName, "SHADOWMUTE", playerUuid, playerName, "Shadowmuted");
    }
    
    public void unjailPlayer(UUID playerUuid) {
        jailedPlayers.remove(playerUuid);
        
        // Deactivate jail punishment in database
        List<Punishment> jailPunishments = databaseManager.getActivePunishments(playerUuid, Punishment.PunishmentType.JAIL);
        for (Punishment punishment : jailPunishments) {
            punishment.setActive(false);
            databaseManager.updatePunishment(punishment);
        }
        
        // Use JailManager to handle location restoration
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            plugin.getJailManager().unjailPlayer(player);
        }
    }
    
    public void unfreezePlayer(UUID playerUuid) {
        frozenPlayers.remove(playerUuid);
        
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            player.sendMessage("§aDu wurdest aufgetaut! Du kannst dich wieder bewegen.");
        }
    }
    
    public boolean isMuted(UUID playerUuid) {
        List<Punishment> punishments = databaseManager.getActivePunishments(playerUuid, Punishment.PunishmentType.MUTE);
        for (Punishment punishment : punishments) {
            if (!punishment.isExpired()) {
                return true;
            } else {
                punishment.setActive(false);
                punishment.setExpired(true);
                databaseManager.updatePunishment(punishment);
            }
        }
        return false;
    }
    
    public boolean isBanned(UUID playerUuid) {
        List<Punishment> punishments = databaseManager.getActivePunishments(playerUuid, Punishment.PunishmentType.BAN);
        for (Punishment punishment : punishments) {
            if (!punishment.isExpired()) {
                return true;
            } else {
                punishment.setActive(false);
                punishment.setExpired(true);
                databaseManager.updatePunishment(punishment);
            }
        }
        return false;
    }
    
    public boolean isFrozen(UUID playerUuid) {
        return frozenPlayers.contains(playerUuid);
    }
    
    public boolean isJailed(UUID playerUuid) {
        return jailedPlayers.contains(playerUuid);
    }
    
    public boolean isSoftbanned(UUID playerUuid) {
        return softbannedPlayers.contains(playerUuid);
    }
    
    public boolean isShadowmuted(UUID playerUuid) {
        return shadowmutedPlayers.contains(playerUuid);
    }
    
    public List<Punishment> getPlayerHistory(UUID playerUuid) {
        return databaseManager.getPunishments(playerUuid);
    }
    
    public void watchPlayer(UUID watcherUuid, UUID targetUuid) {
        watchingPlayers.put(watcherUuid, targetUuid);
    }
    
    public void stopWatching(UUID watcherUuid) {
        watchingPlayers.remove(watcherUuid);
    }
    
    public boolean isWatching(UUID watcherUuid) {
        return watchingPlayers.containsKey(watcherUuid);
    }
    
    public UUID getWatchTarget(UUID watcherUuid) {
        return watchingPlayers.get(watcherUuid);
    }
    
    private String formatDuration(long duration) {
        if (duration == -1) {
            return "permanent";
        }
        
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " Tag(e)";
        } else if (hours > 0) {
            return hours + " Stunde(n)";
        } else if (minutes > 0) {
            return minutes + " Minute(n)";
        } else {
            return seconds + " Sekunde(n)";
        }
    }
}
