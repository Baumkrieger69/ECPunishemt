package de.ecpunishment.commands;

import de.ecpunishment.EcPunishment;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnjailCommand implements CommandExecutor {
    
    private final EcPunishment plugin;
    
    public UnjailCommand(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ecpunishment.unjail")) {
            sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl!");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage("§cVerwendung: /unjail <Spieler>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cSpieler nicht gefunden!");
            return true;
        }
        
        if (!plugin.getJailManager().isJailed(target.getUniqueId())) {
            sender.sendMessage("§c" + target.getName() + " ist nicht im Gefängnis!");
            return true;
        }
        
        // Remove jail punishment from database
        plugin.getPunishmentManager().unjailPlayer(target.getUniqueId());
        
        // Remove from jail manager
        plugin.getJailManager().unjailPlayer(target);
        
        sender.sendMessage("§a" + target.getName() + " wurde aus dem Gefängnis entlassen!");
        target.sendMessage("§aDu wurdest aus dem Gefängnis entlassen!");
        
        // Log the action
        if (sender instanceof Player) {
            Player staff = (Player) sender;
            plugin.getLogger().info(staff.getName() + " has unjailed " + target.getName());
        } else {
            plugin.getLogger().info("Console has unjailed " + target.getName());
        }
        
        return true;
    }
}
