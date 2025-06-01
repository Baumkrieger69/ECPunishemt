package de.ecpunishment.data;

import java.util.UUID;

public class Punishment {
    
    private int id;
    private UUID playerUuid;
    private String playerName;
    private UUID punisherUuid;
    private String punisherName;
    private PunishmentType type;
    private String reason;
    private long timestamp;
    private long duration;
    private boolean active;
    private boolean expired;
    
    public Punishment(UUID playerUuid, String playerName, UUID punisherUuid, String punisherName, 
                     PunishmentType type, String reason, long duration) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.punisherUuid = punisherUuid;
        this.punisherName = punisherName;
        this.type = type;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
        this.duration = duration;
        this.active = true;
        this.expired = false;
    }
    
    public Punishment(int id, UUID playerUuid, String playerName, UUID punisherUuid, String punisherName,
                     PunishmentType type, String reason, long timestamp, long duration, boolean active, boolean expired) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.punisherUuid = punisherUuid;
        this.punisherName = punisherName;
        this.type = type;
        this.reason = reason;
        this.timestamp = timestamp;
        this.duration = duration;
        this.active = active;
        this.expired = expired;
    }
    
    public boolean isExpired() {
        if (duration == -1) return false; // Permanent punishment
        return System.currentTimeMillis() > (timestamp + duration);
    }
    
    public long getRemainingTime() {
        if (duration == -1) return -1; // Permanent
        long remaining = (timestamp + duration) - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    public boolean isPermanent() {
        return duration == -1;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public UUID getPlayerUuid() { return playerUuid; }
    public void setPlayerUuid(UUID playerUuid) { this.playerUuid = playerUuid; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public UUID getPunisherUuid() { return punisherUuid; }
    public void setPunisherUuid(UUID punisherUuid) { this.punisherUuid = punisherUuid; }
    
    public String getPunisherName() { return punisherName; }
    public void setPunisherName(String punisherName) { this.punisherName = punisherName; }
    
    public PunishmentType getType() { return type; }
    public void setType(PunishmentType type) { this.type = type; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public void setExpired(boolean expired) { this.expired = expired; }
    
    public boolean getExpired() { return expired; }
    
    public long getCreatedAt() { return timestamp; }
    
    public String getStaffName() { return punisherName; }
    
    public enum PunishmentType {
        WARN,
        MUTE,
        BAN,
        KICK,
        JAIL,
        FREEZE,
        SOFTBAN,
        SHADOWMUTE
    }
}
