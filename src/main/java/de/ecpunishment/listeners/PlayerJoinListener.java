package de.ecpunishment.listeners;

import de.ecpunishment.EcPunishment;
import de.ecpunishment.data.Punishment;
import de.ecpunishment.utils.TimeUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerJoinListener implements Listener {
    
    private final EcPunishment plugin;
    
    public PlayerJoinListener(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        
        // Check for active bans first
        if (plugin.getPunishmentManager().isBanned(playerUuid)) {
            Punishment banPunishment = getActiveBan(playerUuid);
            if (banPunishment != null) {
                String banMessage = formatBanMessage(banPunishment);
                player.kickPlayer(banMessage);
                return;
            }
        }
        
        // Handle other punishments asynchronously to avoid blocking the main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                handlePlayerPunishments(player);
            }
        }.runTaskAsynchronously(plugin);
        
        // Hide vanished staff from this player
        hideVanishedStaff(player);
    }
    
    private void handlePlayerPunishments(Player player) {
        UUID playerUuid = player.getUniqueId();
        
        // Check and apply frozen status
        if (plugin.getPunishmentManager().isFrozen(playerUuid)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendMessage("§c§lDu bist eingefroren!");
                    player.sendMessage("§7Bewege dich nicht und warte auf einen Moderator.");
                }
            }.runTask(plugin);
        }
        
        // Check and apply jail status
        if (plugin.getJailManager().isJailed(playerUuid)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getJailManager().jailPlayer(player);
                    player.sendMessage("§c§lDu bist im Gefängnis!");
                    player.sendMessage("§7Du wurdest automatisch ins Gefängnis teleportiert.");
                }
            }.runTask(plugin);
        }
        
        // Check and notify about softban
        if (plugin.getPunishmentManager().isSoftbanned(playerUuid)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendMessage("§6§lDu bist softbanned!");
                    player.sendMessage("§7Du kannst nicht bauen, abbauen oder mit der Welt interagieren.");
                }
            }.runTask(plugin);
        }
        
        // Check and notify about mute
        if (plugin.getPunishmentManager().isMuted(playerUuid)) {
            Punishment mutePunishment = getActiveMute(playerUuid);
            if (mutePunishment != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        String message = "§c§lDu bist stumm geschaltet!";
                        if (mutePunishment.getDuration() != -1) {
                            long remaining = (mutePunishment.getCreatedAt() + mutePunishment.getDuration()) - System.currentTimeMillis();
                            if (remaining > 0) {
                                message += "\n§7Verbleibende Zeit: §f" + TimeUtils.formatDuration(remaining);
                            }
                        } else {
                            message += "\n§7Dauer: §fPermanent";
                        }
                        message += "\n§7Grund: §f" + mutePunishment.getReason();
                        player.sendMessage(message);
                    }
                }.runTask(plugin);
            }
        }
        
        // Execute pending offline punishments
        executePendingPunishments(playerUuid, player.getName());
    }
    
    private void executePendingPunishments(UUID playerUuid, String playerName) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "SELECT * FROM punishments WHERE player_uuid = ? AND active = 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String type = rs.getString("type");
                        int punishmentId = rs.getInt("id");
                        
                        // Mark as executed
                        markPunishmentExecuted(punishmentId);
                        
                        // Apply punishment based on type
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                switch (type.toLowerCase()) {
                                    case "ban":
                                        // Player will be kicked on next check
                                        break;
                                    case "jail":
                                        plugin.getJailManager().loadJailedPlayer(playerUuid);
                                        break;
                                    case "freeze":
                                        // Already handled above
                                        break;
                                    case "mute":
                                        // Already handled above
                                        break;
                                }
                            }
                        }.runTask(plugin);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error executing pending punishments for " + playerName + ": " + e.getMessage());
        }
    }
    
    private void markPunishmentExecuted(int punishmentId) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "UPDATE punishments SET active = 0 WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, punishmentId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error marking punishment as executed: " + e.getMessage());
        }
    }
    
    private Punishment getActiveBan(UUID playerUuid) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "SELECT * FROM punishments WHERE player_uuid = ? AND type = 'BAN' AND active = 1 LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return createPunishmentFromResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting active ban: " + e.getMessage());
        }
        return null;
    }
    
    private Punishment getActiveMute(UUID playerUuid) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "SELECT * FROM punishments WHERE player_uuid = ? AND type = 'MUTE' AND active = 1 LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return createPunishmentFromResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting active mute: " + e.getMessage());
        }
        return null;
    }
    
    private Punishment createPunishmentFromResultSet(ResultSet rs) throws SQLException {
        return new Punishment(
            rs.getInt("id"),
            UUID.fromString(rs.getString("player_uuid")),
            rs.getString("player_name"),
            rs.getString("punisher_uuid") != null ? UUID.fromString(rs.getString("punisher_uuid")) : null,
            rs.getString("punisher_name"),
            Punishment.PunishmentType.valueOf(rs.getString("type").toUpperCase()),
            rs.getString("reason"),
            rs.getLong("timestamp"),
            rs.getLong("duration"),
            rs.getBoolean("active"),
            rs.getBoolean("expired")
        );
    }
    
    private String formatBanMessage(Punishment ban) {
        StringBuilder message = new StringBuilder();
        message.append("§c§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n");
        message.append("§f§lDu wurdest vom Server gebannt!\n\n");
        message.append("§7Grund: §f").append(ban.getReason()).append("\n");
        message.append("§7Von: §f").append(ban.getStaffName()).append("\n");
        
        if (ban.getDuration() == -1) {
            message.append("§7Dauer: §fPermanent\n");
        } else {
            long remaining = (ban.getCreatedAt() + ban.getDuration()) - System.currentTimeMillis();
            if (remaining > 0) {
                message.append("§7Verbleibende Zeit: §f").append(TimeUtils.formatDuration(remaining)).append("\n");
            } else {
                message.append("§7Dauer: §fAbgelaufen (wird bald aufgehoben)\n");
            }
        }
        
        message.append("\n§7Entbannungsantrag: §fhttps://discord.gg/deinserver\n");
        message.append("§c§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        
        return message.toString();
    }
    
    private void hideVanishedStaff(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player staff : org.bukkit.Bukkit.getOnlinePlayers()) {
                    if (plugin.getStaffmodeCommand().isInStaffMode(staff.getUniqueId())) {
                        PlayerInteractListener interactListener = plugin.getPlayerInteractListener();
                        if (interactListener != null && interactListener.isVanished(staff.getUniqueId())) {
                            player.hidePlayer(plugin, staff);
                        }
                    }
                }
            }
        }.runTask(plugin);
    }
}
