package de.mecrytv.nexusCore.manager;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.nexusCore.NexusCore;
import de.mecrytv.nexusCore.models.ChatMSGModel;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MessageLogManager {

    private final NexusCore plugin;
    private final ConcurrentHashMap<UUID, ChatMSGModel> logCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, LinkedList<String>> snapshotCache = new ConcurrentHashMap<>();

    private final int MESSAGE_THRESHOLD = 100;
    private final long AUTO_SAVE_INTERVAL = 30;

    public MessageLogManager(NexusCore plugin) {
        this.plugin = plugin;
        startAutoSaveTask();
    }


    public void log(@NotNull UUID uuid, @NotNull String message) {
        ChatMSGModel model = logCache.computeIfAbsent(uuid, k -> new ChatMSGModel(uuid.toString()));

        synchronized (model) {
            model.addMessage(message);
            if (model.getMessages().size() >= MESSAGE_THRESHOLD) {
                saveAndClearAsync(uuid);
            }
        }
    }
    public CompletableFuture<Void> saveAndClearAsync(UUID uuid) {
        ChatMSGModel cachedModel = logCache.get(uuid);
        if (cachedModel == null) return CompletableFuture.completedFuture(null);

        ChatMSGModel dataToSave;
        synchronized (cachedModel) {
            if (cachedModel.getMessages().isEmpty()) return CompletableFuture.completedFuture(null);
            dataToSave = new ChatMSGModel(uuid.toString());
            dataToSave.getMessages().addAll(cachedModel.getMessages());
            cachedModel.getMessages().clear();
        }

        return DatabaseAPI.<ChatMSGModel>get("message_logs", uuid.toString()).thenAcceptAsync(existing -> {
            if (existing != null) {
                existing.getMessages().addAll(dataToSave.getMessages());
                DatabaseAPI.set("message_logs", existing);
            } else {
                DatabaseAPI.set("message_logs", dataToSave);
            }
        }).exceptionally(ex -> {
            plugin.getLogger().severe("Fehler beim Speichern der Chat-Logs für " + uuid + ": " + ex.getMessage());
            return null;
        });
    }
    private void startAutoSaveTask() {
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
            flushAll();
        }, 1, AUTO_SAVE_INTERVAL, TimeUnit.MINUTES);
    }
    public void flushAll() {
        logCache.keySet().forEach(this::saveAndClearAsync);
    }
    public void remove(UUID uuid) {
        saveAndClearAsync(uuid).thenRun(() -> logCache.remove(uuid));
    }
    public List<String> getSnapshot(UUID uuid) {
        return new ArrayList<>(snapshotCache.getOrDefault(uuid, new LinkedList<>()));
    }
    public void logToSnapshot(UUID uuid, String message) {
        snapshotCache.compute(uuid, (key, list) -> {
            if (list == null) list = new LinkedList<>();
            list.addFirst("[" + System.currentTimeMillis() + "] " + message);
            if (list.size() > 50) list.removeLast();
            return list;
        });
    }
}