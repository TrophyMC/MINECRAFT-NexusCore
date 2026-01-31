package de.mecrytv.nexusCore.models.punish;

import com.google.gson.JsonObject;
import de.mecrytv.model.ICacheModel;

public class PunishmentHistoryModel implements ICacheModel {

    private String id; // Format: targetUUID:reasonKey
    private String targetUUID;
    private int count;

    public PunishmentHistoryModel() {}

    public PunishmentHistoryModel(String targetUUID, String reasonKey, int count) {
        this.id = targetUUID + ":" + reasonKey;
        this.targetUUID = targetUUID;
        this.count = count;
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("targetUUID", targetUUID);
        json.addProperty("count", count);
        return json;
    }

    @Override
    public void deserialize(JsonObject data) {
        this.id = data.has("id") ? data.get("id").getAsString() : "";
        this.targetUUID = data.has("targetUUID") ? data.get("targetUUID").getAsString() : "";
        this.count = data.has("count") ? data.get("count").getAsInt() : 0;
    }

    public int getCount() { return count; }
    public void increment() { this.count++; }
    public String getTargetUUID() { return targetUUID; }
}