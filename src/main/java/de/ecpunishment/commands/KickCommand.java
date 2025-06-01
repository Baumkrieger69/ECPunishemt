package de.ecpunishment.commands;

import de.ecpunishment.EcPunishment;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand implements CommandExecutor {
    
    private final EcPunishment plugin;
    
    public KickCommand(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ecpunishment.kick")) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("general.no-permission"));
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("kick.usage"));
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
        
        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : 
                plugin.getReasonsConfig().getDefaultReason("kick");
        
        if (sender instanceof Player) {
            Player staff = (Player) sender;
            plugin.getPunishmentManager().kickPlayer(
                    target.getUniqueId(), target.getName(),
                    staff.getUniqueId(), staff.getName(), reason);
        } else {
            plugin.getPunishmentManager().kickPlayer(
                    target.getUniqueId(), target.getName(),
                    null, "Console", reason);
        }
        
        sender.sendMessage(plugin.getMessagesConfig().getMessage("kick.success")
                .replace("{player}", target.getName())
                .replace("{reason}", reason));
        
        return true;
    }
}
