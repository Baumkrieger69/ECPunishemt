package de.ecpunishment.gui;

import de.ecpunishment.EcPunishment;
import de.ecpunishment.data.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PunishmentGUI {
    
    private final EcPunishment plugin;
    
    public PunishmentGUI(EcPunishment plugin) {
        this.plugin = plugin;
    }
    
    public void openPunishmentGUI(Player staff, Player target) {
        Inventory gui = Bukkit.createInventory(null, 54, "§4Bestrafung: §c" + target.getName());
        
        // Warn button
        ItemStack warnItem = createGuiItem(Material.YELLOW_WOOL, "§6Warnung", 
                "§7Klicke um den Spieler zu verwarnen");
        gui.setItem(10, warnItem);
        
        // Mute button
        ItemStack muteItem = createGuiItem(Material.ORANGE_WOOL, "§6Mute", 
                "§7Klicke um den Spieler zu muten");
        gui.setItem(11, muteItem);
        
        // Ban button
        ItemStack banItem = createGuiItem(Material.RED_WOOL, "§4Ban", 
                "§7Klicke um den Spieler zu bannen");
        gui.setItem(12, banItem);
        
        // Kick button
        ItemStack kickItem = createGuiItem(Material.IRON_DOOR, "§eKick", 
                "§7Klicke um den Spieler zu kicken");
        gui.setItem(13, kickItem);
        
        // Jail button
        ItemStack jailItem = createGuiItem(Material.IRON_BARS, "§8Jail", 
                "§7Klicke um den Spieler einzusperren");
        gui.setItem(14, jailItem);
        
        // Freeze button
        ItemStack freezeItem = createGuiItem(Material.ICE, "§bFreeze", 
                "§7Klicke um den Spieler einzufrieren");
        gui.setItem(15, freezeItem);
        
        // Softban button
        ItemStack softbanItem = createGuiItem(Material.BARRIER, "§cSoftban", 
                "§7Klicke um den Spieler zu softbannen");
        gui.setItem(16, softbanItem);
        
        // History button
        ItemStack historyItem = createGuiItem(Material.BOOK, "§9Strafhistorie", 
                "§7Klicke um die Historie anzuzeigen");
        gui.setItem(31, historyItem);
        
        // Player head
        ItemStack playerHead = createGuiItem(Material.PLAYER_HEAD, "§a" + target.getName(), 
                "§7Zielspieler");
        gui.setItem(4, playerHead);
        
        // Fill empty slots with glass
        ItemStack glass = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", "");
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glass);
            }
        }
        
        staff.openInventory(gui);
    }
    
    public void openHistoryGUI(Player staff, UUID targetUuid, String targetName) {
        List<Punishment> history = plugin.getPunishmentManager().getPlayerHistory(targetUuid);
        
        int size = Math.min(54, ((history.size() / 9) + 1) * 9);
        if (size < 18) size = 18;
        
        Inventory gui = Bukkit.createInventory(null, size, "§9Historie: §b" + targetName);
        
        for (int i = 0; i < Math.min(history.size(), size - 9); i++) {
            Punishment punishment = history.get(i);
            Material material = getMaterialForPunishment(punishment.getType());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Typ: §e" + punishment.getType().name());
            lore.add("§7Grund: §e" + punishment.getReason());
            lore.add("§7Von: §e" + punishment.getPunisherName());
            lore.add("§7Datum: §e" + formatTimestamp(punishment.getTimestamp()));
            
            if (punishment.getDuration() != -1) {
                lore.add("§7Dauer: §e" + formatDuration(punishment.getDuration()));
            } else {
                lore.add("§7Dauer: §cPermanent");
            }
            
            lore.add("§7Status: " + (punishment.isActive() ? "§aAktiv" : "§cInaktiv"));
            
            ItemStack item = createGuiItem(material, "§6" + punishment.getType().name(), lore);
            gui.setItem(i, item);
        }
        
        // Back button
        ItemStack backItem = createGuiItem(Material.ARROW, "§cZurück", "§7Zurück zum Hauptmenü");
        gui.setItem(size - 5, backItem);
        
        staff.openInventory(gui);
    }
    
    public void openDurationGUI(Player staff, String punishmentType, Player target) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6Dauer wählen: §e" + punishmentType);
        
        // Duration options
        gui.setItem(10, createDurationItem("5 Minuten", 5 * 60 * 1000));
        gui.setItem(11, createDurationItem("30 Minuten", 30 * 60 * 1000));
        gui.setItem(12, createDurationItem("1 Stunde", 60 * 60 * 1000));
        gui.setItem(13, createDurationItem("6 Stunden", 6 * 60 * 60 * 1000));
        gui.setItem(14, createDurationItem("1 Tag", 24 * 60 * 60 * 1000));
        gui.setItem(15, createDurationItem("7 Tage", 7 * 24 * 60 * 60 * 1000));
        gui.setItem(16, createDurationItem("Permanent", -1));
        
        // Back button
        ItemStack backItem = createGuiItem(Material.ARROW, "§cZurück", "§7Zurück zum Hauptmenü");
        gui.setItem(22, backItem);
        
        staff.openInventory(gui);
    }
    
    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(line);
        }
        meta.setLore(loreList);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createDurationItem(String duration, long milliseconds) {
        return createGuiItem(Material.CLOCK, "§e" + duration, "§7Klicke um diese Dauer zu wählen");
    }
    
    private Material getMaterialForPunishment(Punishment.PunishmentType type) {
        switch (type) {
            case WARN: return Material.YELLOW_WOOL;
            case MUTE: return Material.ORANGE_WOOL;
            case BAN: return Material.RED_WOOL;
            case KICK: return Material.IRON_DOOR;
            case JAIL: return Material.IRON_BARS;
            case FREEZE: return Material.ICE;
            case SOFTBAN: return Material.BARRIER;
            case SHADOWMUTE: return Material.GRAY_WOOL;
            default: return Material.PAPER;
        }
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
    
    public void openPunishmentMenu(Player staff, Player target) {
        openPunishmentGUI(staff, target);
    }
}
