package de.ecpunishment.commands;

import de.ecpunishment.EcPunishment;
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
import java.util.*;

public class ModstatsCommand implements CommandExecutor {
    
    private final EcPunishment plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    
    public ModstatsCommand(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ecpunishment.modstats")) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("general.no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            // Show general server statistics
            displayServerStats(sender);
        } else {
            // Show specific moderator statistics
            String targetMod = args[0];
            displayModeratorStats(sender, targetMod);
        }
        
        return true;
    }
    
    private void displayServerStats(CommandSender sender) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            sender.sendMessage("§8§l======= §6Server Moderations-Statistiken §8§l=======");
            sender.sendMessage("");
            
            // Total punishments
            String totalSql = "SELECT COUNT(*) as total FROM punishments";
            try (PreparedStatement stmt = conn.prepareStatement(totalSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    sender.sendMessage("§7Gesamte Bestrafungen: §e" + rs.getInt("total"));
                }
            }
            
            // Active punishments
            String activeSql = "SELECT COUNT(*) as active FROM punishments WHERE active = 1";
            try (PreparedStatement stmt = conn.prepareStatement(activeSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    sender.sendMessage("§7Aktive Bestrafungen: §e" + rs.getInt("active"));
                }
            }
            
            // Punishments by type
            sender.sendMessage("");
            sender.sendMessage("§7Bestrafungen nach Typ:");
            String typeSql = "SELECT type, COUNT(*) as count FROM punishments GROUP BY type ORDER BY count DESC";
            try (PreparedStatement stmt = conn.prepareStatement(typeSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("type");
                    int count = rs.getInt("count");
                    sender.sendMessage("  §8• §e" + type.toUpperCase() + "§7: §f" + count);
                }
            }
            
            // Top moderators
            sender.sendMessage("");
            sender.sendMessage("§7Top 5 Moderatoren:");
            String topModsSql = "SELECT staff_name, COUNT(*) as count FROM punishments WHERE staff_name != 'Console' GROUP BY staff_name ORDER BY count DESC LIMIT 5";
            try (PreparedStatement stmt = conn.prepareStatement(topModsSql);
                 ResultSet rs = stmt.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    String staffName = rs.getString("staff_name");
                    int count = rs.getInt("count");
                    sender.sendMessage("  §8" + rank + ". §b" + staffName + "§7: §f" + count + " §7Bestrafungen");
                    rank++;
                }
            }
            
            // Recent activity (last 7 days)
            sender.sendMessage("");
            long weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
            String recentSql = "SELECT COUNT(*) as recent FROM punishments WHERE created_at >= ?";
            try (PreparedStatement stmt = conn.prepareStatement(recentSql)) {
                stmt.setLong(1, weekAgo);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        sender.sendMessage("§7Bestrafungen (letzte 7 Tage): §e" + rs.getInt("recent"));
                    }
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Error displaying server stats: " + e.getMessage());
            sender.sendMessage("§cFehler beim Laden der Statistiken!");
        }
    }
    
    private void displayModeratorStats(CommandSender sender, String targetMod) {
        UUID targetUUID = getModeratorUUID(targetMod);
        if (targetUUID == null) {
            sender.sendMessage("§cModerator nicht gefunden!");
            return;
        }
        
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            sender.sendMessage("§8§l======= §6Statistiken für " + targetMod + " §8§l=======");
            sender.sendMessage("");
            
            // Total punishments by this moderator
            String totalSql = "SELECT COUNT(*) as total FROM punishments WHERE staff_uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(totalSql)) {
                stmt.setString(1, targetUUID.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        sender.sendMessage("§7Gesamte Bestrafungen: §e" + rs.getInt("total"));
                    }
                }
            }
            
            // Punishments by type for this moderator
            sender.sendMessage("");
            sender.sendMessage("§7Bestrafungen nach Typ:");
            String typeSql = "SELECT type, COUNT(*) as count FROM punishments WHERE staff_uuid = ? GROUP BY type ORDER BY count DESC";
            try (PreparedStatement stmt = conn.prepareStatement(typeSql)) {
                stmt.setString(1, targetUUID.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String type = rs.getString("type");
                        int count = rs.getInt("count");
                        sender.sendMessage("  §8• §e" + type.toUpperCase() + "§7: §f" + count);
                    }
                }
            }
            
            // First and last punishment
            sender.sendMessage("");
            String firstSql = "SELECT created_at FROM punishments WHERE staff_uuid = ? ORDER BY created_at ASC LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(firstSql)) {
                stmt.setString(1, targetUUID.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        long firstPunishment = rs.getLong("created_at");
                        sender.sendMessage("§7Erste Bestrafung: §f" + dateFormat.format(new Date(firstPunishment)));
                    }
                }
            }
            
            String lastSql = "SELECT created_at FROM punishments WHERE staff_uuid = ? ORDER BY created_at DESC LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(lastSql)) {
                stmt.setString(1, targetUUID.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        long lastPunishment = rs.getLong("created_at");
                        sender.sendMessage("§7Letzte Bestrafung: §f" + dateFormat.format(new Date(lastPunishment)));
                    }
                }
            }
            
            // Activity in last 30 days
            sender.sendMessage("");
            long monthAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L);
            String recentSql = "SELECT COUNT(*) as recent FROM punishments WHERE staff_uuid = ? AND created_at >= ?";
            try (PreparedStatement stmt = conn.prepareStatement(recentSql)) {
                stmt.setString(1, targetUUID.toString());
                stmt.setLong(2, monthAgo);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        sender.sendMessage("§7Aktivität (letzte 30 Tage): §e" + rs.getInt("recent") + " §7Bestrafungen");
                    }
                }
            }
            
            // Most punished players by this moderator
            sender.sendMessage("");
            sender.sendMessage("§7Am häufigsten bestrafte Spieler:");
            String topPlayersSql = "SELECT target_name, COUNT(*) as count FROM punishments WHERE staff_uuid = ? GROUP BY target_name ORDER BY count DESC LIMIT 3";
            try (PreparedStatement stmt = conn.prepareStatement(topPlayersSql)) {
                stmt.setString(1, targetUUID.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    int rank = 1;
                    while (rs.next()) {
                        String playerName = rs.getString("target_name");
                        int count = rs.getInt("count");
                        sender.sendMessage("  §8" + rank + ". §c" + playerName + "§7: §f" + count + " §7Bestrafungen");
                        rank++;
                    }
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Error displaying moderator stats: " + e.getMessage());
            sender.sendMessage("§cFehler beim Laden der Moderator-Statistiken!");
        }
    }
    
    private UUID getModeratorUUID(String modName) {
        Player player = Bukkit.getPlayer(modName);
        if (player != null) {
            return player.getUniqueId();
        }
        
        // Try to get from database
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "SELECT staff_uuid FROM punishments WHERE staff_name = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, modName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String uuidStr = rs.getString("staff_uuid");
                        if (uuidStr != null) {
                            return UUID.fromString(uuidStr);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting moderator UUID: " + e.getMessage());
        }
        
        return null;
    }
}
