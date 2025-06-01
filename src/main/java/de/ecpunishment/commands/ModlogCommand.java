package de.ecpunishment.commands;

import de.ecpunishment.EcPunishment;
import de.ecpunishment.data.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ModlogCommand implements CommandExecutor {
    
    private final EcPunishment plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    
    public ModlogCommand(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ecpunishment.modlog")) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("general.no-permission"));
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage("§cVerwendung: /modlog <Spieler> [Seite]");
            return true;
        }
        
        String targetName = args[0];
        int page = 1;
        
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                sender.sendMessage("§cUngültige Seitenzahl!");
                return true;
            }
        }
        
        // Get target UUID
        UUID targetUUID = getPlayerUUID(targetName);
        if (targetUUID == null) {
            sender.sendMessage("§cSpieler nicht gefunden!");
            return true;
        }
        
        displayModLogs(sender, targetName, targetUUID, page);
        return true;
    }
    
    private UUID getPlayerUUID(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            return player.getUniqueId();
        }
        
        // Try to get from database
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "SELECT target_uuid FROM punishments WHERE target_name = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return UUID.fromString(rs.getString("target_uuid"));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting player UUID: " + e.getMessage());
        }
        
        return null;
    }
    
    private void displayModLogs(CommandSender sender, String targetName, UUID targetUUID, int page) {
        int itemsPerPage = 10;
        int offset = (page - 1) * itemsPerPage;
        
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            // Get total count
            String countSql = "SELECT COUNT(*) FROM punishments WHERE target_uuid = ?";
            int totalPunishments = 0;
            try (PreparedStatement stmt = conn.prepareStatement(countSql)) {
                stmt.setString(1, targetUUID.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalPunishments = rs.getInt(1);
                    }
                }
            }
            
            if (totalPunishments == 0) {
                sender.sendMessage("§7Keine Bestrafungen für §c" + targetName + " §7gefunden.");
                return;
            }
            
            int totalPages = (int) Math.ceil((double) totalPunishments / itemsPerPage);
            
            // Get punishments for current page
            String sql = "SELECT * FROM punishments WHERE target_uuid = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, targetUUID.toString());
                stmt.setInt(2, itemsPerPage);
                stmt.setInt(3, offset);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    sender.sendMessage("§8§l======= §6Moderator-Logs für " + targetName + " §8§l=======");
                    sender.sendMessage("§7Seite §e" + page + "§7/§e" + totalPages + " §7(§e" + totalPunishments + " §7Einträge)");
                    sender.sendMessage("");
                    
                    while (rs.next()) {
                        String type = rs.getString("type");
                        String staffName = rs.getString("staff_name");
                        String reason = rs.getString("reason");
                        long createdAt = rs.getLong("created_at");
                        long duration = rs.getLong("duration");
                        boolean active = rs.getBoolean("active");
                        
                        String dateStr = dateFormat.format(new Date(createdAt));
                        String durationStr = duration == -1 ? "Permanent" : formatDuration(duration);
                        String statusStr = active ? "§aAktiv" : "§7Abgelaufen";
                        
                        sender.sendMessage("§8• §e" + type.toUpperCase() + " §7von §b" + staffName);
                        sender.sendMessage("  §7Grund: §f" + reason);
                        sender.sendMessage("  §7Datum: §f" + dateStr + " §7| Dauer: §f" + durationStr + " §7| Status: " + statusStr);
                        sender.sendMessage("");
                    }
                    
                    if (page < totalPages) {
                        sender.sendMessage("§7Nächste Seite: §e/modlog " + targetName + " " + (page + 1));
                    }
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Error displaying mod logs: " + e.getMessage());
            sender.sendMessage("§cFehler beim Laden der Moderator-Logs!");
        }
    }
    
    private String formatDuration(long duration) {
        if (duration <= 0) return "Permanent";
        
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m";
        } else {
            return seconds + "s";
        }
    }
}
