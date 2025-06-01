package de.ecpunishment.commands;

import de.ecpunishment.EcPunishment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetjailCommand implements CommandExecutor {
    
    private final EcPunishment plugin;
    
    public SetjailCommand(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern verwendet werden!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ecpunishment.setjail")) {
            player.sendMessage("§cDu hast keine Berechtigung für diesen Befehl!");
            return true;
        }
        
        plugin.getJailManager().setJailLocation(player.getLocation());
        player.sendMessage("§aGefängnis-Standort wurde erfolgreich gesetzt!");
        player.sendMessage("§7Position: §f" + 
            String.format("%.2f, %.2f, %.2f", 
                player.getLocation().getX(), 
                player.getLocation().getY(), 
                player.getLocation().getZ()) +
            " §7in Welt §f" + player.getWorld().getName());
        
        return true;
    }
}
