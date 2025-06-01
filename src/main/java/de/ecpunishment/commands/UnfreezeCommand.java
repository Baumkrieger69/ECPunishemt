package de.ecpunishment.commands;

import de.ecpunishment.EcPunishment;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnfreezeCommand implements CommandExecutor {
    
    private final EcPunishment plugin;
    
    public UnfreezeCommand(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ecpunishment.unfreeze")) {
            sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl!");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage("§cVerwendung: /unfreeze <Spieler>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cSpieler nicht gefunden!");
            return true;
        }
        
        if (!plugin.getPunishmentManager().isFrozen(target.getUniqueId())) {
            sender.sendMessage("§c" + target.getName() + " ist nicht eingefroren!");
            return true;
        }
        
        // Unfreeze the player
        plugin.getPunishmentManager().unfreezePlayer(target.getUniqueId());
        
        sender.sendMessage("§a" + target.getName() + " wurde aufgetaut!");
        target.sendMessage("§aDu wurdest aufgetaut! Du kannst dich wieder bewegen.");
        
        // Log the action
        if (sender instanceof Player) {
            Player staff = (Player) sender;
            plugin.getLogger().info(staff.getName() + " has unfrozen " + target.getName());
        } else {
            plugin.getLogger().info("Console has unfrozen " + target.getName());
        }
        
        return true;
    }
}
