package de.ecpunishment.commands;

import de.ecpunishment.EcPunishment;
import de.ecpunishment.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JailCommand implements CommandExecutor {
    
    private final EcPunishment plugin;
    
    public JailCommand(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ecpunishment.jail")) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("general.no-permission"));
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("jail.usage"));
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
        
        if (args.length > 1) {
            String timeStr = args[1];
            if (!timeStr.equalsIgnoreCase("perm") && !timeStr.equalsIgnoreCase("permanent")) {
                duration = TimeUtils.parseDuration(timeStr);
                if (duration == 0) {
                    sender.sendMessage(plugin.getMessagesConfig().getMessage("general.invalid-duration"));
                    return true;
                }
            }
        }
        
        if (sender instanceof Player) {
            Player staff = (Player) sender;
            plugin.getPunishmentManager().jailPlayer(
                    target.getUniqueId(), target.getName(),
                    staff.getUniqueId(), staff.getName(), duration);
        } else {
            plugin.getPunishmentManager().jailPlayer(
                    target.getUniqueId(), target.getName(),
                    null, "Console", duration);
        }
        
        String durationStr = duration == -1 ? "permanent" : TimeUtils.formatDuration(duration);
        sender.sendMessage(plugin.getMessagesConfig().getMessage("jail.success")
                .replace("{player}", target.getName())
                .replace("{duration}", durationStr));
        
        return true;
    }
}
