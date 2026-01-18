package de.mecrytv.nexusCore;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.languageapi.LanguageAPI;
import de.mecrytv.nexusCore.commands.ReportCommand;
import de.mecrytv.nexusCore.commands.ReportsCommand;
import de.mecrytv.nexusCore.listeners.ReportTeleportListener;
import de.mecrytv.nexusCore.manager.ConfigManager;
import de.mecrytv.nexusCore.manager.SkinCacheManager;
import de.mecrytv.nexusCore.models.ReportModel;
import de.mecrytv.nexusCore.models.SkinCacheModel;
import de.mecrytv.nexusCore.models.TeleportModel;
import de.mecrytv.utils.DatabaseConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;

public final class NexusCore extends JavaPlugin {

    private static NexusCore instance;
    private LanguageAPI languageAPI;
    private ConfigManager config;
    private DatabaseAPI databaseAPI;
    private SkinCacheManager skinCacheManager;

    @Override
    public void onEnable() {
        instance = this;
        this.languageAPI = new LanguageAPI(Paths.get("/home/minecraft/languages/"));
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

        if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
            getLogger().info("✅ HeadDatabase-API erfolgreich gefunden!");
        } else {
            getLogger().warning("❌ HeadDatabase wurde nicht gefunden! GUI-Flaggen funktionieren nicht.");
        }

        this.skinCacheManager = new SkinCacheManager();

        getCommand("report").setExecutor(new ReportCommand());
        getCommand("reports").setExecutor(new ReportsCommand());

        getServer().getPluginManager().registerEvents(new ReportTeleportListener(), this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "nexus:bridge");
    }

    @Override
    public void onDisable() {
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
}
