package de.mecrytv.nexusCore;

import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.databaseapi.utils.DatabaseConfig;
import de.mecrytv.languageapi.LanguageAPI;
import de.mecrytv.nexusCore.commands.ReportCommand;
import de.mecrytv.nexusCore.commands.ReportsCommand;
import de.mecrytv.nexusCore.listeners.ChatLogListener;
import de.mecrytv.nexusCore.listeners.ReportTeleportListener;
import de.mecrytv.nexusCore.listeners.VanishListener;
import de.mecrytv.nexusCore.manager.*;
import de.mecrytv.nexusCore.models.*;
import de.mecrytv.nexusapi.NexusAPI;
import de.mecrytv.nexusapi.utils.TaskBatcher;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class NexusCore extends JavaPlugin {

    private static NexusCore instance;
    private LanguageAPI languageAPI;
    private ConfigManager config;
    private DatabaseAPI databaseAPI;
    private SkinCacheManager skinCacheManager;
    private VanishManager vanishManager;
    private MessageLogManager messageLogManager;
    private PunishManager punishManager;

    @Override
    public void onEnable() {
        instance = this;

        NexusAPI.getInstance();

        TaskBatcher.setScheduler(runnable -> Bukkit.getScheduler().runTask(this, runnable));

        RegisteredServiceProvider<LanguageAPI> rsp = Bukkit.getServicesManager().getRegistration(LanguageAPI.class);
        if (rsp != null) {
            this.languageAPI = rsp.getProvider();
        } else {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.config = new ConfigManager(getDataFolder().toPath(), "config.json");

        DatabaseConfig dbConfig = new DatabaseConfig(
                config.getString("mariadb.host"),
                config.getInt("mariadb.port"),
                config.getString("mariadb.database"),
                config.getString("mariadb.username"),
                config.getString("mariadb.password"),
                config.getString("redis.host"),
                config.getInt("redis.port"),
                config.getString("redis.password")
        );

        this.databaseAPI = new DatabaseAPI(dbConfig);

        databaseAPI.registerModel("reports", ReportModel::new);
        databaseAPI.registerModel("skins", SkinCacheModel::new);
        databaseAPI.registerModel("message_logs", ChatMSGModel::new);
        databaseAPI.registerModel("proofs", ProofModel::new);

        this.skinCacheManager = new SkinCacheManager();
        this.vanishManager = new VanishManager(this);
        this.messageLogManager = new MessageLogManager(this);
        this.punishManager = new PunishManager(this);

        getCommand("report").setExecutor(new ReportCommand());
        getCommand("reports").setExecutor(new ReportsCommand());

        getServer().getPluginManager().registerEvents(new ReportTeleportListener(), this);
        getServer().getPluginManager().registerEvents(new VanishListener(), this);
        getServer().getPluginManager().registerEvents(new ChatLogListener(), this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "nexus:bridge");
    }

    @Override
    public void onDisable() {
        if (this.messageLogManager != null) this.messageLogManager.flushAll();
        if (this.databaseAPI != null) this.databaseAPI.shutdown();
    }

    public static NexusCore getInstance() { return instance; }
    public LanguageAPI getLanguageAPI() { return languageAPI; }
    public DatabaseAPI getDatabaseAPI() { return databaseAPI; }
    public PunishManager getPunishManager() { return punishManager; }
    public VanishManager getVanishManager() { return vanishManager; }
    public SkinCacheManager getSkinCacheManager() { return skinCacheManager; }
    public MessageLogManager getMessageLogManager() { return messageLogManager; }
    public Component getPrefix() {
        return MiniMessage.miniMessage().deserialize(this.config.getString("prefix"));
    }
}