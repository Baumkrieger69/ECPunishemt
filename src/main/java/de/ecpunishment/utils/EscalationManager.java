package de.ecpunishment.utils;

import de.ecpunishment.EcPunishment;
import de.ecpunishment.configs.EscalationConfig;
import de.ecpunishment.data.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EscalationManager {
    
    private final EcPunishment plugin;
    
    public EscalationManager(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    public void checkEscalation(UUID playerUuid, String playerName, Punishment.PunishmentType type) {
        if (!plugin.getEscalationConfig().isEscalationEnabled()) {
            return;
        }
        
        // Get count of this punishment type
        int count = getPunishmentCount(playerUuid, type);
        
        // Check if there's an escalation rule for this count
        EscalationConfig.EscalationRule rule = plugin.getEscalationConfig().getEscalationRule(type, count);
        
        if (rule != null) {
            // Apply escalation
            applyEscalation(playerUuid, playerName, rule);
            
            // Notify staff
            String message = "§c[Eskalation] §e" + playerName + " §7hat §e" + count + " " + type.name() + 
                    " §7erreicht. Automatische Eskalation: §c" + rule.getAction().name();
            
            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("ecpunishment.notify")) {
                    staff.sendMessage(message);
                }
            }
            
            plugin.getLogger().info("Escalation applied for " + playerName + ": " + rule.getAction().name() + 
                    " (Reason: " + count + " " + type.name() + ")");
        }
    }
    
    private int getPunishmentCount(UUID playerUuid, Punishment.PunishmentType type) {
        switch (type) {
            case WARN:
                return plugin.getDatabaseManager().getWarningCount(playerUuid);
            default:
                // For other types, we would need to implement similar counting methods
                return plugin.getDatabaseManager().getActivePunishments(playerUuid, type).size();
        }
    }
    
    private void applyEscalation(UUID playerUuid, String playerName, EscalationConfig.EscalationRule rule) {
        switch (rule.getAction()) {
            case WARN:
                plugin.getPunishmentManager().warnPlayer(playerUuid, playerName, null, "System", rule.getReason());
                break;
            case MUTE:
                plugin.getPunishmentManager().mutePlayer(playerUuid, playerName, null, "System", rule.getReason(), rule.getDuration());
                break;
            case BAN:
                plugin.getPunishmentManager().banPlayer(playerUuid, playerName, null, "System", rule.getReason(), rule.getDuration());
                break;
            case KICK:
                plugin.getPunishmentManager().kickPlayer(playerUuid, playerName, null, "System", rule.getReason());
                break;
            case JAIL:
                plugin.getPunishmentManager().jailPlayer(playerUuid, playerName, null, "System", rule.getDuration());
                break;
            case FREEZE:
                plugin.getPunishmentManager().freezePlayer(playerUuid, playerName, null, "System");
                break;
            case SOFTBAN:
                plugin.getPunishmentManager().softbanPlayer(playerUuid, playerName, null, "System");
                break;
            case SHADOWMUTE:
                plugin.getPunishmentManager().shadowmutePlayer(playerUuid, playerName, null, "System");
                break;
        }
    }
}
