package de.ecpunishment.configs;

import de.ecpunishment.EcPunishment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class GuiConfig {
    
    private final EcPunishment plugin;
    private File guiFile;
    private FileConfiguration gui;
    
    public GuiConfig(EcPunishment plugin) {
        this.plugin = plugin;
        this.guiFile = new File(plugin.getDataFolder(), "gui.yml");
    }
    
    public void loadConfig() {
        if (!guiFile.exists()) {
            createDefaultGui();
        }
        
        gui = YamlConfiguration.loadConfiguration(guiFile);
    }
    
    private void createDefaultGui() {
        plugin.getDataFolder().mkdirs();
        
        try {
            guiFile.createNewFile();
            FileConfiguration defaultGui = YamlConfiguration.loadConfiguration(guiFile);
            
            // Main punishment GUI
            defaultGui.set("punishment-gui.title", "§4Bestrafung: §c{player}");
            defaultGui.set("punishment-gui.size", 54);
            
            // Button positions and materials
            defaultGui.set("punishment-gui.buttons.warn.slot", 10);
            defaultGui.set("punishment-gui.buttons.warn.material", "YELLOW_WOOL");
            defaultGui.set("punishment-gui.buttons.warn.name", "§6Warnung");
            defaultGui.set("punishment-gui.buttons.warn.lore", new String[]{
                "§7Klicke um den Spieler zu verwarnen"
            });
            
            defaultGui.set("punishment-gui.buttons.mute.slot", 11);
            defaultGui.set("punishment-gui.buttons.mute.material", "ORANGE_WOOL");
            defaultGui.set("punishment-gui.buttons.mute.name", "§6Mute");
            defaultGui.set("punishment-gui.buttons.mute.lore", new String[]{
                "§7Klicke um den Spieler zu muten"
            });
            
            defaultGui.set("punishment-gui.buttons.ban.slot", 12);
            defaultGui.set("punishment-gui.buttons.ban.material", "RED_WOOL");
            defaultGui.set("punishment-gui.buttons.ban.name", "§4Ban");
            defaultGui.set("punishment-gui.buttons.ban.lore", new String[]{
                "§7Klicke um den Spieler zu bannen"
            });
            
            defaultGui.set("punishment-gui.buttons.kick.slot", 13);
            defaultGui.set("punishment-gui.buttons.kick.material", "IRON_DOOR");
            defaultGui.set("punishment-gui.buttons.kick.name", "§eKick");
            defaultGui.set("punishment-gui.buttons.kick.lore", new String[]{
                "§7Klicke um den Spieler zu kicken"
            });
            
            defaultGui.set("punishment-gui.buttons.jail.slot", 14);
            defaultGui.set("punishment-gui.buttons.jail.material", "IRON_BARS");
            defaultGui.set("punishment-gui.buttons.jail.name", "§8Jail");
            defaultGui.set("punishment-gui.buttons.jail.lore", new String[]{
                "§7Klicke um den Spieler einzusperren"
            });
            
            defaultGui.set("punishment-gui.buttons.freeze.slot", 15);
            defaultGui.set("punishment-gui.buttons.freeze.material", "ICE");
            defaultGui.set("punishment-gui.buttons.freeze.name", "§bFreeze");
            defaultGui.set("punishment-gui.buttons.freeze.lore", new String[]{
                "§7Klicke um den Spieler einzufrieren"
            });
            
            defaultGui.set("punishment-gui.buttons.softban.slot", 16);
            defaultGui.set("punishment-gui.buttons.softban.material", "BARRIER");
            defaultGui.set("punishment-gui.buttons.softban.name", "§cSoftban");
            defaultGui.set("punishment-gui.buttons.softban.lore", new String[]{
                "§7Klicke um den Spieler zu softbannen"
            });
            
            defaultGui.set("punishment-gui.buttons.history.slot", 31);
            defaultGui.set("punishment-gui.buttons.history.material", "BOOK");
            defaultGui.set("punishment-gui.buttons.history.name", "§9Strafhistorie");
            defaultGui.set("punishment-gui.buttons.history.lore", new String[]{
                "§7Klicke um die Historie anzuzeigen"
            });
            
            // Duration selection GUI
            defaultGui.set("duration-gui.title", "§6Dauer wählen: §e{punishment}");
            defaultGui.set("duration-gui.size", 27);
            
            // History GUI
            defaultGui.set("history-gui.title", "§9Historie: §b{player}");
            defaultGui.set("history-gui.size", 54);
            
            defaultGui.save(guiFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create default GUI config: " + e.getMessage());
        }
    }
    
    public String getGuiTitle(String guiType) {
        return gui.getString(guiType + ".title", "§cGUI");
    }
    
    public int getGuiSize(String guiType) {
        return gui.getInt(guiType + ".size", 27);
    }
    
    public String getButtonName(String guiType, String button) {
        return gui.getString(guiType + ".buttons." + button + ".name", "§cButton");
    }
    
    public String[] getButtonLore(String guiType, String button) {
        return gui.getStringList(guiType + ".buttons." + button + ".lore").toArray(new String[0]);
    }
    
    public String getButtonMaterial(String guiType, String button) {
        return gui.getString(guiType + ".buttons." + button + ".material", "STONE");
    }
    
    public int getButtonSlot(String guiType, String button) {
        return gui.getInt(guiType + ".buttons." + button + ".slot", 0);
    }
}
