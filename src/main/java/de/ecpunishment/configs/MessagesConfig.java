package de.ecpunishment.configs;

import de.ecpunishment.EcPunishment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class MessagesConfig {
    
    private final EcPunishment plugin;
    private File messagesFile;
    private FileConfiguration messages;
    
    public MessagesConfig(EcPunishment plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
    }
    
    public void loadConfig() {
        if (!messagesFile.exists()) {
            createDefaultMessages();
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    private void createDefaultMessages() {
        plugin.getDataFolder().mkdirs();
        
        try {
            messagesFile.createNewFile();
            FileConfiguration defaultMessages = YamlConfiguration.loadConfiguration(messagesFile);
            
            // General messages
            defaultMessages.set("general.no-permission", "§cDu hast keine Berechtigung für diesen Befehl!");
            defaultMessages.set("general.player-only", "§cDieser Befehl kann nur von Spielern ausgeführt werden!");
            defaultMessages.set("general.player-not-found", "§cSpieler nicht gefunden!");
            defaultMessages.set("general.cannot-punish", "§cDu kannst diesen Spieler nicht bestrafen!");
            defaultMessages.set("general.invalid-duration", "§cUngültige Zeitangabe!");
            defaultMessages.set("general.reload-success", "§aKonfiguration erfolgreich neu geladen!");
            defaultMessages.set("general.reload-error", "§cFehler beim Neuladen der Konfiguration!");
            
            // Warn messages
            defaultMessages.set("warn.usage", "§cVerwendung: /warn <Spieler> [Grund]");
            defaultMessages.set("warn.success", "§a{player} wurde verwarnt. Grund: {reason}");
            defaultMessages.set("warn.received", "§6Du wurdest von {punisher} verwarnt!\n§7Grund: §e{reason}");
            
            // Mute messages
            defaultMessages.set("mute.usage", "§cVerwendung: /mute <Spieler> [Zeit] [Grund]");
            defaultMessages.set("mute.success", "§a{player} wurde für {duration} gemutet. Grund: {reason}");
            defaultMessages.set("mute.temporary", "§cDu wurdest von {punisher} für {duration} gemutet!\n§7Grund: §e{reason}");
            defaultMessages.set("mute.permanent", "§cDu wurdest von {punisher} permanent gemutet!\n§7Grund: §e{reason}");
            defaultMessages.set("mute.chat-blocked", "§cDu bist gemutet und kannst nicht schreiben!");
            
            // Ban messages
            defaultMessages.set("ban.usage", "§cVerwendung: /ban <Spieler> [Zeit] [Grund]");
            defaultMessages.set("ban.success", "§a{player} wurde für {duration} gebannt. Grund: {reason}");
            defaultMessages.set("ban.temporary", "§cDu wurdest von {punisher} für {duration} gebannt!\n\n§7Grund: §e{reason}\n\n§7Du kannst dich nach Ablauf der Zeit wieder einloggen.");
            defaultMessages.set("ban.permanent", "§cDu wurdest von {punisher} permanent gebannt!\n\n§7Grund: §e{reason}\n\n§7Wende dich an die Administration.");
            
            // Kick messages
            defaultMessages.set("kick.usage", "§cVerwendung: /kick <Spieler> [Grund]");
            defaultMessages.set("kick.success", "§a{player} wurde gekickt. Grund: {reason}");
            defaultMessages.set("kick.message", "§cDu wurdest von {punisher} gekickt!\n\n§7Grund: §e{reason}");
            
            // Jail messages
            defaultMessages.set("jail.usage", "§cVerwendung: /jail <Spieler> [Zeit]");
            defaultMessages.set("jail.success", "§a{player} wurde für {duration} ins Gefängnis gesperrt.");
            defaultMessages.set("jail.jailed", "§cDu wurdest von {punisher} für {duration} ins Gefängnis gesperrt!");
            defaultMessages.set("jail.released", "§aDu wurdest aus dem Gefängnis entlassen!");
            
            // Freeze messages
            defaultMessages.set("freeze.usage", "§cVerwendung: /freeze <Spieler>");
            defaultMessages.set("freeze.frozen", "§cDu wurdest von {punisher} eingefroren! Bewege dich nicht!");
            defaultMessages.set("freeze.unfrozen", "§aDu wurdest von {punisher} wieder aufgetaut!");
            defaultMessages.set("freeze.frozen-staff", "§a{player} wurde eingefroren.");
            defaultMessages.set("freeze.unfrozen-staff", "§a{player} wurde aufgetaut.");
            defaultMessages.set("freeze.move-blocked", "§cDu bist eingefroren und kannst dich nicht bewegen!");
            
            // Softban messages
            defaultMessages.set("softban.usage", "§cVerwendung: /softban <Spieler>");
            defaultMessages.set("softban.success", "§a{player} wurde softgebannt.");
            defaultMessages.set("softban.applied", "§cDu wurdest von {punisher} softgebannt! Du kannst dem Server beitreten, aber keine Aktionen ausführen.");
            defaultMessages.set("softban.action-blocked", "§cDu bist softgebannt und kannst diese Aktion nicht ausführen!");
            
            // History messages
            defaultMessages.set("history.usage", "§cVerwendung: /history <Spieler>");
            
            // Modlog messages
            defaultMessages.set("modlog.usage", "§cVerwendung: /modlog <Spieler>");
            
            // Staff mode messages
            defaultMessages.set("staffmode.enabled", "§aStaff-Modus aktiviert!");
            defaultMessages.set("staffmode.disabled", "§cStaff-Modus deaktiviert!");
            
            // Punish GUI messages
            defaultMessages.set("punish.usage", "§cVerwendung: /punish <Spieler>");
            
            defaultMessages.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create default messages: " + e.getMessage());
        }
    }
    
    public String getMessage(String path) {
        String message = messages.getString(path);
        if (message == null) {
            return "§cMessage not found: " + path;
        }
        return message.replace("&", "§");
    }
    
    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return message;
    }
}
