package de.ecpunishment.commands;

import de.ecpunishment.EcPunishment;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SoftbanCommand implements CommandExecutor {
    
    private final EcPunishment plugin;
    
    public SoftbanCommand(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ecpunishment.softban")) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("general.no-permission"));
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("softban.usage"));
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
        
        if (sender instanceof Player) {
            Player staff = (Player) sender;
            plugin.getPunishmentManager().softbanPlayer(
                    target.getUniqueId(), target.getName(),
                    staff.getUniqueId(), staff.getName());
        } else {
            plugin.getPunishmentManager().softbanPlayer(
                    target.getUniqueId(), target.getName(),
                    null, "Console");
        }
        
        sender.sendMessage(plugin.getMessagesConfig().getMessage("softban.success")
                .replace("{player}", target.getName()));
        
        return true;
    }
}
