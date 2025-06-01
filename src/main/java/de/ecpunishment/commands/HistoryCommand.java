package de.ecpunishment.commands;

import de.ecpunishment.EcPunishment;
import de.ecpunishment.data.Punishment;
import de.ecpunishment.gui.PunishmentGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HistoryCommand implements CommandExecutor {
    
    private final EcPunishment plugin;
    private final PunishmentGUI gui;
    
    public HistoryCommand(EcPunishment plugin) {
        this.plugin = plugin;
        this.gui = new PunishmentGUI(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ecpunishment.history")) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("general.no-permission"));
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("history.usage"));
            return true;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || !target.hasPlayedBefore()) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("general.player-not-found"));
            return true;
        }
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            gui.openHistoryGUI(player, target.getUniqueId(), target.getName());
        } else {
            // Console output
            List<Punishment> history = plugin.getPunishmentManager().getPlayerHistory(target.getUniqueId());
            sender.sendMessage("§9=== Strafhistorie für " + target.getName() + " ===");
            
            if (history.isEmpty()) {
                sender.sendMessage("§7Keine Strafen gefunden.");
                return true;
            }
            
            for (Punishment punishment : history) {
                String status = punishment.isActive() ? "§aAktiv" : "§cInaktiv";
                String duration = punishment.getDuration() == -1 ? "Permanent" : formatDuration(punishment.getDuration());
                
                sender.sendMessage("§6" + punishment.getType().name() + " §7- " + status);
                sender.sendMessage("  §7Grund: §e" + punishment.getReason());
                sender.sendMessage("  §7Von: §e" + punishment.getPunisherName());
                sender.sendMessage("  §7Dauer: §e" + duration);
                sender.sendMessage("  §7Datum: §e" + formatTimestamp(punishment.getTimestamp()));
                sender.sendMessage("");
            }
        }
        
        return true;
    }
    
    private String formatTimestamp(long timestamp) {
        return new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date(timestamp));
    }
    
    private String formatDuration(long duration) {
        if (duration == -1) return "Permanent";
        
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " Tag(e)";
        } else if (hours > 0) {
            return hours + " Stunde(n)";
        } else if (minutes > 0) {
            return minutes + " Minute(n)";
        } else {
            return seconds + " Sekunde(n)";
        }
    }
}
