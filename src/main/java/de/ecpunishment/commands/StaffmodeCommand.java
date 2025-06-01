package de.ecpunishment.commands;

import de.ecpunishment.EcPunishment;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class StaffmodeCommand implements CommandExecutor {
    
    private final EcPunishment plugin;
    private final Map<UUID, ItemStack[]> savedInventories;
    private final Map<UUID, GameMode> savedGameModes;
    private final Map<UUID, Player> watchedPlayers; // Staff -> Target
    
    public StaffmodeCommand(EcPunishment plugin) {
        this.plugin = plugin;
        this.savedInventories = new HashMap<>();
        this.savedGameModes = new HashMap<>();
        this.watchedPlayers = new HashMap<>();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern verwendet werden!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ecpunishment.staffmode")) {
            player.sendMessage("§cDu hast keine Berechtigung für diesen Befehl!");
            return true;
        }
        
        UUID playerUuid = player.getUniqueId();
        
        if (savedInventories.containsKey(playerUuid)) {
            // Deactivate staff mode
            player.getInventory().clear();
            player.getInventory().setContents(savedInventories.get(playerUuid));
            player.setGameMode(savedGameModes.get(playerUuid));
            
            savedInventories.remove(playerUuid);
            savedGameModes.remove(playerUuid);
            watchedPlayers.remove(playerUuid);
            
            player.sendMessage("§cStaff-Modus deaktiviert!");
        } else {
            // Activate staff mode
            savedInventories.put(playerUuid, player.getInventory().getContents().clone());
            savedGameModes.put(playerUuid, player.getGameMode());
            
            player.getInventory().clear();
            player.setGameMode(GameMode.CREATIVE);
            player.setAllowFlight(true);
            player.setFlying(true);
            
            // Give staff items
            giveStaffItems(player);
            
            player.sendMessage("§aStaff-Modus aktiviert!");
            player.sendMessage("§7Verwende die Items in deinem Inventar für Staff-Aktionen.");
        }
        
        return true;
    }
    
    private void giveStaffItems(Player player) {
        // Slot 0: Compass for random teleport
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        compassMeta.setDisplayName("§bZufälliger Teleport");
        compassMeta.setLore(Arrays.asList(
            "§7Rechtsklick: Teleportiere zu einem",
            "§7zufälligen Online-Spieler"
        ));
        compass.setItemMeta(compassMeta);
        player.getInventory().setItem(0, compass);
        
        // Slot 1: Stick for freezing players
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta stickMeta = stick.getItemMeta();
        stickMeta.setDisplayName("§cFreeze-Tool");
        stickMeta.setLore(Arrays.asList(
            "§7Rechtsklick auf Spieler:",
            "§7Friere den Spieler ein/auf"
        ));
        stick.setItemMeta(stickMeta);
        player.getInventory().setItem(1, stick);
        
        // Slot 2: Book for player info
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta bookMeta = book.getItemMeta();
        bookMeta.setDisplayName("§eSpieler-Info");
        bookMeta.setLore(Arrays.asList(
            "§7Rechtsklick auf Spieler:",
            "§7Zeige Spieler-Informationen"
        ));
        book.setItemMeta(bookMeta);
        player.getInventory().setItem(2, book);
        
        // Slot 3: Paper for punishment GUI
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta paperMeta = paper.getItemMeta();
        paperMeta.setDisplayName("§6Bestrafungs-GUI");
        paperMeta.setLore(Arrays.asList(
            "§7Rechtsklick auf Spieler:",
            "§7Öffne Bestrafungs-Menü"
        ));
        paper.setItemMeta(paperMeta);
        player.getInventory().setItem(3, paper);
        
        // Slot 4: Ender Eye for vanish
        ItemStack enderEye = new ItemStack(Material.ENDER_EYE);
        ItemMeta eyeMeta = enderEye.getItemMeta();
        eyeMeta.setDisplayName("§5Vanish");
        eyeMeta.setLore(Arrays.asList(
            "§7Rechtsklick: Werde unsichtbar",
            "§7für normale Spieler"
        ));
        enderEye.setItemMeta(eyeMeta);
        player.getInventory().setItem(4, enderEye);
        
        // Slot 7: Clock for watching players
        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta clockMeta = clock.getItemMeta();
        clockMeta.setDisplayName("§dSpieler beobachten");
        clockMeta.setLore(Arrays.asList(
            "§7Rechtsklick auf Spieler:",
            "§7Folge dem Spieler unsichtbar"
        ));
        clock.setItemMeta(clockMeta);
        player.getInventory().setItem(7, clock);
        
        // Slot 8: Barrier for exit staff mode
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        barrierMeta.setDisplayName("§cStaff-Modus verlassen");
        barrierMeta.setLore(Arrays.asList(
            "§7Rechtsklick: Verlasse den",
            "§7Staff-Modus"
        ));
        barrier.setItemMeta(barrierMeta);
        player.getInventory().setItem(8, barrier);
    }
    
    public boolean isInStaffMode(UUID playerUuid) {
        return savedInventories.containsKey(playerUuid);
    }
    
    public void setWatchedPlayer(UUID staffUuid, Player target) {
        watchedPlayers.put(staffUuid, target);
    }
    
    public Player getWatchedPlayer(UUID staffUuid) {
        return watchedPlayers.get(staffUuid);
    }
    
    public void removeWatchedPlayer(UUID staffUuid) {
        watchedPlayers.remove(staffUuid);
    }
    
    public Map<UUID, ItemStack[]> getSavedInventories() {
        return savedInventories;
    }
    
    public Map<UUID, GameMode> getSavedGameModes() {
        return savedGameModes;
    }
}
