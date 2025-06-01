package de.ecpunishment.commands;

import de.ecpunishment.EcPunishment;
import de.ecpunishment.gui.PunishmentGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PunishCommand implements CommandExecutor {
    
    private final EcPunishment plugin;
    private final PunishmentGUI gui;
    
    public PunishCommand(EcPunishment plugin) {
        this.plugin = plugin;
        this.gui = new PunishmentGUI(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("general.player-only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ecpunishment.punish")) {
            player.sendMessage(plugin.getMessagesConfig().getMessage("general.no-permission"));
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage(plugin.getMessagesConfig().getMessage("punish.usage"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.getMessagesConfig().getMessage("general.player-not-found"));
            return true;
        }
        
        if (target.hasPermission("ecpunishment.bypass") && !player.hasPermission("ecpunishment.*")) {
            player.sendMessage(plugin.getMessagesConfig().getMessage("general.cannot-punish"));
            return true;
        }
        
        gui.openPunishmentGUI(player, target);
        return true;
    }
}
