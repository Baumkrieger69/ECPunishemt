package de.ecpunishment.data;

import de.ecpunishment.EcPunishment;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    
    private final EcPunishment plugin;
    private Connection connection;
    
    public DatabaseManager(EcPunishment plugin) {
        this.plugin = plugin;
        initializeDatabase();
        createTables();
    }
    
    private void initializeDatabase() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            String url = "jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/database.db";
            connection = DriverManager.getConnection(url);
            
            plugin.getLogger().info("Database connected successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not connect to database: " + e.getMessage());
        }
    }
    
    private void createTables() {
        try {
            Statement stmt = connection.createStatement();
            
            // Punishments table
            String punishmentsTable = "CREATE TABLE IF NOT EXISTS punishments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_uuid TEXT NOT NULL," +
                    "player_name TEXT NOT NULL," +
                    "punisher_uuid TEXT," +
                    "punisher_name TEXT," +
                    "type TEXT NOT NULL," +
                    "reason TEXT," +
                    "timestamp BIGINT NOT NULL," +
                    "duration BIGINT NOT NULL," +
                    "active BOOLEAN DEFAULT TRUE," +
                    "expired BOOLEAN DEFAULT FALSE" +
                    ")";
            stmt.execute(punishmentsTable);
            
            // Staff actions table
            String staffActionsTable = "CREATE TABLE IF NOT EXISTS staff_actions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "staff_uuid TEXT NOT NULL," +
                    "staff_name TEXT NOT NULL," +
                    "action TEXT NOT NULL," +
                    "target_uuid TEXT," +
                    "target_name TEXT," +
                    "details TEXT," +
                    "timestamp BIGINT NOT NULL" +
                    ")";
            stmt.execute(staffActionsTable);
            
            // Player data table
            String playerDataTable = "CREATE TABLE IF NOT EXISTS player_data (" +
                    "uuid TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "is_frozen BOOLEAN DEFAULT FALSE," +
                    "is_jailed BOOLEAN DEFAULT FALSE," +
                    "jail_location TEXT," +
                    "is_softbanned BOOLEAN DEFAULT FALSE," +
                    "is_shadowmuted BOOLEAN DEFAULT FALSE," +
                    "last_seen BIGINT" +
                    ")";
            stmt.execute(playerDataTable);
            
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create tables: " + e.getMessage());
        }
    }
    
    public void savePunishment(Punishment punishment) {
        try {
            String sql = "INSERT INTO punishments (player_uuid, player_name, punisher_uuid, punisher_name, " +
                    "type, reason, timestamp, duration, active, expired) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, punishment.getPlayerUuid().toString());
            stmt.setString(2, punishment.getPlayerName());
            stmt.setString(3, punishment.getPunisherUuid() != null ? punishment.getPunisherUuid().toString() : null);
            stmt.setString(4, punishment.getPunisherName());
            stmt.setString(5, punishment.getType().name());
            stmt.setString(6, punishment.getReason());
            stmt.setLong(7, punishment.getTimestamp());
            stmt.setLong(8, punishment.getDuration());
            stmt.setBoolean(9, punishment.isActive());
            stmt.setBoolean(10, punishment.isExpired());
            
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save punishment: " + e.getMessage());
        }
    }
    
    public List<Punishment> getPunishments(UUID playerUuid) {
        List<Punishment> punishments = new ArrayList<>();
        try {
            String sql = "SELECT * FROM punishments WHERE player_uuid = ? ORDER BY timestamp DESC";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, playerUuid.toString());
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Punishment punishment = new Punishment(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("player_name"),
                        rs.getString("punisher_uuid") != null ? UUID.fromString(rs.getString("punisher_uuid")) : null,
                        rs.getString("punisher_name"),
                        Punishment.PunishmentType.valueOf(rs.getString("type")),
                        rs.getString("reason"),
                        rs.getLong("timestamp"),
                        rs.getLong("duration"),
                        rs.getBoolean("active"),
                        rs.getBoolean("expired")
                );
                punishments.add(punishment);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load punishments: " + e.getMessage());
        }
        return punishments;
    }
    
    public List<Punishment> getActivePunishments(UUID playerUuid, Punishment.PunishmentType type) {
        List<Punishment> punishments = new ArrayList<>();
        try {
            String sql = "SELECT * FROM punishments WHERE player_uuid = ? AND type = ? AND active = TRUE";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, type.name());
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Punishment punishment = new Punishment(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("player_name"),
                        rs.getString("punisher_uuid") != null ? UUID.fromString(rs.getString("punisher_uuid")) : null,
                        rs.getString("punisher_name"),
                        Punishment.PunishmentType.valueOf(rs.getString("type")),
                        rs.getString("reason"),
                        rs.getLong("timestamp"),
                        rs.getLong("duration"),
                        rs.getBoolean("active"),
                        rs.getBoolean("expired")
                );
                punishments.add(punishment);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load active punishments: " + e.getMessage());
        }
        return punishments;
    }
    
    public void updatePunishment(Punishment punishment) {
        try {
            String sql = "UPDATE punishments SET active = ?, expired = ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setBoolean(1, punishment.isActive());
            stmt.setBoolean(2, punishment.isExpired());
            stmt.setInt(3, punishment.getId());
            
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not update punishment: " + e.getMessage());
        }
    }
    
    public int getWarningCount(UUID playerUuid) {
        try {
            String sql = "SELECT COUNT(*) FROM punishments WHERE player_uuid = ? AND type = 'WARN' AND active = TRUE";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, playerUuid.toString());
            
            ResultSet rs = stmt.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            
            rs.close();
            stmt.close();
            return count;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not get warning count: " + e.getMessage());
            return 0;
        }
    }
    
    public void logStaffAction(UUID staffUuid, String staffName, String action, UUID targetUuid, String targetName, String details) {
        try {
            String sql = "INSERT INTO staff_actions (staff_uuid, staff_name, action, target_uuid, target_name, details, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, staffUuid.toString());
            stmt.setString(2, staffName);
            stmt.setString(3, action);
            stmt.setString(4, targetUuid != null ? targetUuid.toString() : null);
            stmt.setString(5, targetName);
            stmt.setString(6, details);
            stmt.setLong(7, System.currentTimeMillis());
            
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not log staff action: " + e.getMessage());
        }
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not close database connection: " + e.getMessage());
        }
    }
}
