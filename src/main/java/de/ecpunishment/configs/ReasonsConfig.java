package de.ecpunishment.configs;

import de.ecpunishment.EcPunishment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ReasonsConfig {
    
    private final EcPunishment plugin;
    private File reasonsFile;
    private FileConfiguration reasons;
    
    public ReasonsConfig(EcPunishment plugin) {
        this.plugin = plugin;
        this.reasonsFile = new File(plugin.getDataFolder(), "reasons.yml");
    }
    
    public void loadConfig() {
        if (!reasonsFile.exists()) {
            createDefaultReasons();
        }
        
        reasons = YamlConfiguration.loadConfiguration(reasonsFile);
    }
    
    private void createDefaultReasons() {
        plugin.getDataFolder().mkdirs();
        
        try {
            reasonsFile.createNewFile();
            FileConfiguration defaultReasons = YamlConfiguration.loadConfiguration(reasonsFile);
            
            // Default reasons for different punishment types
            defaultReasons.set("defaults.warn", "Regelverstoß");
            defaultReasons.set("defaults.mute", "Unangemessenes Verhalten im Chat");
            defaultReasons.set("defaults.ban", "Schwerwiegender Regelverstoß");
            defaultReasons.set("defaults.kick", "Störung des Spielbetriebs");
            defaultReasons.set("defaults.jail", "Zeitweise Auszeit");
            defaultReasons.set("defaults.freeze", "Verdacht auf Cheating");
            defaultReasons.set("defaults.softban", "Beschränkung der Spielaktivitäten");
            
            // Predefined reasons for GUI
            defaultReasons.set("predefined.warn", new String[]{
                "Spam",
                "Beleidigung",
                "Werbung",
                "Trolling",
                "Unangemessene Sprache"
            });
            
            defaultReasons.set("predefined.mute", new String[]{
                "Spam im Chat",
                "Beleidigung anderer Spieler",
                "Werbung für andere Server",
                "Rassistische Äußerungen",
                "Ständiges Fluchen"
            });
            
            defaultReasons.set("predefined.ban", new String[]{
                "Hacking/Cheating",
                "Griefing",
                "Wiederholte Regelverstöße",
                "Schwere Beleidigung",
                "DDoS-Drohungen"
            });
            
            defaultReasons.set("predefined.kick", new String[]{
                "AFK in wichtigen Bereichen",
                "Störung von Events",
                "Nicht befolgen von Anweisungen",
                "Technische Probleme"
            });
            
            defaultReasons.save(reasonsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create default reasons: " + e.getMessage());
        }
    }
    
    public String getDefaultReason(String punishmentType) {
        return reasons.getString("defaults." + punishmentType, "Kein Grund angegeben");
    }
    
    public String[] getPredefinedReasons(String punishmentType) {
        return reasons.getStringList("predefined." + punishmentType).toArray(new String[0]);
    }
}
