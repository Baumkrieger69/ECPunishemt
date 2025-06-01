package de.ecpunishment.commands;

import de.ecpunishment.EcPunishment;
import de.ecpunishment.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MuteCommand implements CommandExecutor {
    
    private final EcPunishment plugin;
    
    public MuteCommand(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ecpunishment.mute")) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("general.no-permission"));
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("mute.usage"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("general.player-not-found"));
            return true;
        }
        
        if (target.hasPermission("ecpunishment.bypass") && !sender.hasPermission("ecpunishment.*")) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("general.cannot-punish"));
            return true;
        }
        
        long duration = -1; // permanent by default
        String reason = plugin.getReasonsConfig().getDefaultReason("mute");
        
        if (args.length > 1) {
            // Try to parse duration
            String timeStr = args[1];
            if (!timeStr.equalsIgnoreCase("perm") && !timeStr.equalsIgnoreCase("permanent")) {
                duration = TimeUtils.parseDuration(timeStr);
                if (duration == 0) {
                    sender.sendMessage(plugin.getMessagesConfig().getMessage("general.invalid-duration"));
                    return true;
                }
            }
            
            // Get reason if provided
            if (args.length > 2) {
                reason = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
            }
        }
        
        if (sender instanceof Player) {
            Player staff = (Player) sender;
            plugin.getPunishmentManager().mutePlayer(
                    target.getUniqueId(), target.getName(),
                    staff.getUniqueId(), staff.getName(), reason, duration);
        } else {
            plugin.getPunishmentManager().mutePlayer(
                    target.getUniqueId(), target.getName(),
                    null, "Console", reason, duration);
        }
        
        String durationStr = duration == -1 ? "permanent" : TimeUtils.formatDuration(duration);
        sender.sendMessage(plugin.getMessagesConfig().getMessage("mute.success")
                .replace("{player}", target.getName())
                .replace("{reason}", reason)
                .replace("{duration}", durationStr));
        
        return true;
    }
}
