package de.ecpunishment.commands;

import de.ecpunishment.EcPunishment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MainCommand implements CommandExecutor {
    
    private final EcPunishment plugin;
    
    public MainCommand(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Show plugin info
            sender.sendMessage("§9=== EcPunishment v1.0.0 ===");
            sender.sendMessage("§7Ein erweiterbares Bestrafungssystem für Minecraft-Server");
            sender.sendMessage("§7Verwende §e/ecpunishment reload §7um die Konfiguration neu zu laden");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("ecpunishment.reload")) {
                sender.sendMessage(plugin.getMessagesConfig().getMessage("general.no-permission"));
                return true;
            }
            
            try {
                plugin.reload();
                sender.sendMessage(plugin.getMessagesConfig().getMessage("general.reload-success"));
            } catch (Exception e) {
                sender.sendMessage(plugin.getMessagesConfig().getMessage("general.reload-error"));
                plugin.getLogger().severe("Error reloading plugin: " + e.getMessage());
            }
            return true;
        }
        
        sender.sendMessage("§cUnbekannter Befehl. Verwende §e/ecpunishment reload");
        return true;
    }
}
