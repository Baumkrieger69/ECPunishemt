package de.ecpunishment;

import de.ecpunishment.commands.*;
import de.ecpunishment.configs.*;
import de.ecpunishment.data.DatabaseManager;
import de.ecpunishment.data.PunishmentManager;
import de.ecpunishment.listeners.*;
import de.ecpunishment.utils.EscalationManager;
import de.ecpunishment.utils.JailManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EcPunishment extends JavaPlugin {
    
    private static EcPunishment instance;
    private DatabaseManager databaseManager;
    private PunishmentManager punishmentManager;
    private EscalationManager escalationManager;
    private JailManager jailManager;
    
    // Commands
    private StaffmodeCommand staffmodeCommand;
    
    // Listeners
    private PlayerInteractListener playerInteractListener;
    
    // Config managers
    private ConfigManager configManager;
    private MessagesConfig messagesConfig;
    private GuiConfig guiConfig;
    private ReasonsConfig reasonsConfig;
    private EscalationConfig escalationConfig;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize configs
        initializeConfigs();
        
        // Initialize managers
        this.databaseManager = new DatabaseManager(this);
        this.punishmentManager = new PunishmentManager(this);
        this.escalationManager = new EscalationManager(this);
        this.jailManager = new JailManager(this);
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        getLogger().info("EcPunishment has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (jailManager != null) {
            jailManager.saveJailConfig();
        }
        getLogger().info("EcPunishment has been disabled!");
    }
    
    private void initializeConfigs() {
        this.configManager = new ConfigManager(this);
        this.messagesConfig = new MessagesConfig(this);
        this.guiConfig = new GuiConfig(this);
        this.reasonsConfig = new ReasonsConfig(this);
        this.escalationConfig = new EscalationConfig(this);
        
        configManager.loadConfig();
        messagesConfig.loadConfig();
        guiConfig.loadConfig();
        reasonsConfig.loadConfig();
        escalationConfig.loadConfig();
    }
    
    private void registerCommands() {
        getCommand("punish").setExecutor(new PunishCommand(this));
        getCommand("warn").setExecutor(new WarnCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("kick").setExecutor(new KickCommand(this));
        getCommand("jail").setExecutor(new JailCommand(this));
        getCommand("freeze").setExecutor(new FreezeCommand(this));
        getCommand("softban").setExecutor(new SoftbanCommand(this));
        getCommand("history").setExecutor(new HistoryCommand(this));
        getCommand("modlog").setExecutor(new ModlogCommand(this));
        this.staffmodeCommand = new StaffmodeCommand(this);
        getCommand("staffmode").setExecutor(staffmodeCommand);
        getCommand("modstats").setExecutor(new ModstatsCommand(this));
        getCommand("ecpunishment").setExecutor(new MainCommand(this));
        
        // Additional commands
        getCommand("setjail").setExecutor(new SetjailCommand(this));
        getCommand("unjail").setExecutor(new UnjailCommand(this));
        getCommand("unfreeze").setExecutor(new UnfreezeCommand(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        this.playerInteractListener = new PlayerInteractListener(this);
        getServer().getPluginManager().registerEvents(playerInteractListener, this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
    }
    
    public void reload() {
        configManager.loadConfig();
        messagesConfig.loadConfig();
        guiConfig.loadConfig();
        reasonsConfig.loadConfig();
        escalationConfig.loadConfig();
        jailManager.saveJailConfig();
    }
    
    // Getters
    public static EcPunishment getInstance() {
        return instance;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }
    
    public EscalationManager getEscalationManager() {
        return escalationManager;
    }
    
    public JailManager getJailManager() {
        return jailManager;
    }
    
    public StaffmodeCommand getStaffmodeCommand() {
        return staffmodeCommand;
    }
    
    public PlayerInteractListener getPlayerInteractListener() {
        return playerInteractListener;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }
    
    public GuiConfig getGuiConfig() {
        return guiConfig;
    }
    
    public ReasonsConfig getReasonsConfig() {
        return reasonsConfig;
    }
    
    public EscalationConfig getEscalationConfig() {
        return escalationConfig;
    }
}
