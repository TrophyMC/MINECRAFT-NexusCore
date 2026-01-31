package de.mecrytv.nexusCore.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.mecrytv.databaseapi.model.ICacheModel;

import java.util.ArrayList;
import java.util.List;

public class ChatMSGModel implements ICacheModel {

    private String playerUUID;
    private final List<MessageEntry> messages = new ArrayList<>();

    public ChatMSGModel(){}

    public ChatMSGModel(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public record MessageEntry(String message, long timestamp) {}

    @Override
    public String getIdentifier() {
        return playerUUID;
    }

    public void addMessage(String content) {
        messages.add(new MessageEntry(content, System.currentTimeMillis()));
    }

    public List<MessageEntry> getMessages() { return messages; }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("playerUUID", playerUUID);

        JsonArray array = new JsonArray();
        for (MessageEntry entry : messages) {
            JsonObject msgJson = new JsonObject();
            msgJson.addProperty("m", entry.message());
            msgJson.addProperty("t", entry.timestamp());
            array.add(msgJson);
        }
        json.add("messages", array);
        return json;
    }

    @Override
    public void deserialize(JsonObject data) {
        this.playerUUID = data.get("playerUUID").getAsString();
        if (data.has("messages")) {
            data.getAsJsonArray("messages").forEach(el -> {
                JsonObject obj = el.getAsJsonObject();
                messages.add(new MessageEntry(obj.get("m").getAsString(), obj.get("t").getAsLong()));
            });
        }
    }
}
