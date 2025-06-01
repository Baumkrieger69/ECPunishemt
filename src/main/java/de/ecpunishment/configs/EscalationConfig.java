package de.ecpunishment.configs;

import de.ecpunishment.EcPunishment;
import de.ecpunishment.data.Punishment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class EscalationConfig {
    
    private final EcPunishment plugin;
    private File escalationFile;
    private FileConfiguration escalation;
    
    public EscalationConfig(EcPunishment plugin) {
        this.plugin = plugin;
        this.escalationFile = new File(plugin.getDataFolder(), "escalation.yml");
    }
    
    public void loadConfig() {
        if (!escalationFile.exists()) {
            createDefaultEscalation();
        }
        
        escalation = YamlConfiguration.loadConfiguration(escalationFile);
    }
    
    private void createDefaultEscalation() {
        plugin.getDataFolder().mkdirs();
        
        try {
            escalationFile.createNewFile();
            FileConfiguration defaultEscalation = YamlConfiguration.loadConfiguration(escalationFile);
            
            // Escalation settings
            defaultEscalation.set("escalation.enabled", true);
            defaultEscalation.set("escalation.reset-time", 604800000L); // 7 days in milliseconds
            
            // Warning escalation
            defaultEscalation.set("escalation.warn.3.action", "MUTE");
            defaultEscalation.set("escalation.warn.3.duration", 3600000L); // 1 hour
            defaultEscalation.set("escalation.warn.3.reason", "3 Warnungen erreicht - Automatische Eskalation");
            
            defaultEscalation.set("escalation.warn.5.action", "BAN");
            defaultEscalation.set("escalation.warn.5.duration", 86400000L); // 1 day
            defaultEscalation.set("escalation.warn.5.reason", "5 Warnungen erreicht - Automatische Eskalation");
            
            defaultEscalation.set("escalation.warn.10.action", "BAN");
            defaultEscalation.set("escalation.warn.10.duration", -1L); // permanent
            defaultEscalation.set("escalation.warn.10.reason", "10 Warnungen erreicht - Permanenter Ban");
            
            // Mute escalation
            defaultEscalation.set("escalation.mute.3.action", "BAN");
            defaultEscalation.set("escalation.mute.3.duration", 86400000L); // 1 day
            defaultEscalation.set("escalation.mute.3.reason", "3 Mutes erreicht - Automatische Eskalation");
            
            defaultEscalation.set("escalation.mute.5.action", "BAN");
            defaultEscalation.set("escalation.mute.5.duration", -1L); // permanent
            defaultEscalation.set("escalation.mute.5.reason", "5 Mutes erreicht - Permanenter Ban");
            
            // Kick escalation
            defaultEscalation.set("escalation.kick.3.action", "MUTE");
            defaultEscalation.set("escalation.kick.3.duration", 1800000L); // 30 minutes
            defaultEscalation.set("escalation.kick.3.reason", "3 Kicks erreicht - Automatische Eskalation");
            
            defaultEscalation.set("escalation.kick.5.action", "BAN");
            defaultEscalation.set("escalation.kick.5.duration", 3600000L); // 1 hour
            defaultEscalation.set("escalation.kick.5.reason", "5 Kicks erreicht - Automatische Eskalation");
            
            defaultEscalation.save(escalationFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create default escalation config: " + e.getMessage());
        }
    }
    
    public boolean isEscalationEnabled() {
        return escalation.getBoolean("escalation.enabled", true);
    }
    
    public long getResetTime() {
        return escalation.getLong("escalation.reset-time", 604800000L);
    }
    
    public EscalationRule getEscalationRule(Punishment.PunishmentType type, int count) {
        String path = "escalation." + type.name().toLowerCase() + "." + count;
        
        if (!escalation.contains(path + ".action")) {
            return null;
        }
        
        String action = escalation.getString(path + ".action");
        long duration = escalation.getLong(path + ".duration");
        String reason = escalation.getString(path + ".reason");
        
        return new EscalationRule(Punishment.PunishmentType.valueOf(action), duration, reason);
    }
    
    public static class EscalationRule {
        private final Punishment.PunishmentType action;
        private final long duration;
        private final String reason;
        
        public EscalationRule(Punishment.PunishmentType action, long duration, String reason) {
            this.action = action;
            this.duration = duration;
            this.reason = reason;
        }
        
        public Punishment.PunishmentType getAction() { return action; }
        public long getDuration() { return duration; }
        public String getReason() { return reason; }
    }
}
