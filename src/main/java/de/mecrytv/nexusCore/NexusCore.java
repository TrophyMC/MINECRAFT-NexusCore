package de.mecrytv.nexusCore;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.languageapi.LanguageAPI;
import de.mecrytv.nexusCore.commands.ReportCommand;
import de.mecrytv.nexusCore.commands.ReportsCommand;
import de.mecrytv.nexusCore.listeners.ChatLogListener;
import de.mecrytv.nexusCore.listeners.ReportTeleportListener;
import de.mecrytv.nexusCore.listeners.VanishListener;
import de.mecrytv.nexusCore.manager.*;
import de.mecrytv.nexusCore.models.*;
import de.mecrytv.nexusCore.models.punish.BanModel;
import de.mecrytv.nexusCore.models.punish.MuteModel;
import de.mecrytv.nexusCore.models.punish.PunishmentHistoryModel;
import de.mecrytv.nexusCore.models.punish.WarnModel;
import de.mecrytv.utils.DatabaseConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;

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

        RegisteredServiceProvider<LanguageAPI> rsp = Bukkit.getServicesManager().getRegistration(LanguageAPI.class);
        if (rsp != null) {
            this.languageAPI = rsp.getProvider();
            getLogger().info("✅ Globaly Language-Cache Successfully Synced!");
        } else {
            getLogger().severe("❌ Failed to sync Globaly Language-Cache! Disabling plugin.");
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
        databaseAPI.registerModel("reportteleport", TeleportModel::new);
        databaseAPI.registerModel("message_logs", ChatMSGModel::new);
        databaseAPI.registerModel("proofs", ProofModel::new);
        databaseAPI.registerModel("punishments", PunishmentHistoryModel::new);
        databaseAPI.registerModel("ban", BanModel::new);
        databaseAPI.registerModel("mute", MuteModel::new);
        databaseAPI.registerModel("warn", WarnModel::new);

        if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
            getLogger().info("✅ HeadDatabase-API erfolgreich gefunden!");
        } else {
            getLogger().warning("❌ HeadDatabase wurde nicht gefunden! GUI-Flaggen funktionieren nicht.");
        }

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

    public Component getPrefix() {
        String prefixRaw = this.config.getString("prefix");
        if (prefixRaw.isEmpty() || prefixRaw == null) {
            prefixRaw = "<darK_grey>[<gold>Moderation<dark_grey>] ";
        }
        return MiniMessage.miniMessage().deserialize(prefixRaw);
    }
    public static NexusCore getInstance() {
        return instance;
    }
    public LanguageAPI getLanguageAPI() {
        return languageAPI;
    }
    public ConfigManager getConfiguration() {
        return config;
    }
    public DatabaseAPI getDatabaseAPI() {
        return databaseAPI;
    }
    public SkinCacheManager getSkinCacheManager() {
        return skinCacheManager;
    }
    public VanishManager getVanishManager() {
        return vanishManager;
    }
    public MessageLogManager getMessageLogManager() {
        return messageLogManager;
    }
    public PunishManager getPunishManager() {
        return punishManager;
    }
}
