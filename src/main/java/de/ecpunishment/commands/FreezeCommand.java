package de.ecpunishment.commands;

import de.ecpunishment.EcPunishment;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreezeCommand implements CommandExecutor {
    
    private final EcPunishment plugin;
    
    public FreezeCommand(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ecpunishment.freeze")) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("general.no-permission"));
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("freeze.usage"));
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
        
        String staffName = sender instanceof Player ? ((Player) sender).getName() : "Console";
        
        if (plugin.getPunishmentManager().isFrozen(target.getUniqueId())) {
            // Unfreeze
            if (sender instanceof Player) {
                Player staff = (Player) sender;
                plugin.getPunishmentManager().unfreezePlayer(
                        target.getUniqueId(), target.getName(),
                        staff.getUniqueId(), staff.getName());
            } else {
                plugin.getPunishmentManager().unfreezePlayer(
                        target.getUniqueId(), target.getName(),
                        null, "Console");
            }
            
            sender.sendMessage(plugin.getMessagesConfig().getMessage("freeze.unfrozen-staff")
                    .replace("{player}", target.getName()));
        } else {
            // Freeze
            if (sender instanceof Player) {
                Player staff = (Player) sender;
                plugin.getPunishmentManager().freezePlayer(
                        target.getUniqueId(), target.getName(),
                        staff.getUniqueId(), staff.getName());
            } else {
                plugin.getPunishmentManager().freezePlayer(
                        target.getUniqueId(), target.getName(),
                        null, "Console");
            }
            
            sender.sendMessage(plugin.getMessagesConfig().getMessage("freeze.frozen-staff")
                    .replace("{player}", target.getName()));
        }
        
        return true;
    }
}
