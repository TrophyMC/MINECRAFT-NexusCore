package de.mecrytv.nexusCore.models.punish;

import com.google.gson.JsonObject;
import de.mecrytv.model.ICacheModel;

public class PunishmentHistoryModel implements ICacheModel {

    private String punishmentId;
    private int count;

    public PunishmentHistoryModel(){}

    public PunishmentHistoryModel(String playerUUID, String reasonKey, int count){
        this.punishmentId = playerUUID + ":" + reasonKey;
        this.count = count;
    }

    @Override
    public String getIdentifier() {
        return punishmentId;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("id", punishmentId);
        json.addProperty("count", count);
        return json;
    }

    @Override
    public void deserialize(JsonObject data) {
        this.punishmentId = data.get("punishmentId").getAsString();
        this.count = data.get("count").getAsInt();
    }

    public int getCount() { return count; }
    public void increment() { this.count++; }
}
