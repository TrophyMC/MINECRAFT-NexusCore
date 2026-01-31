package de.mecrytv.nexusCore.models.punish;

import com.google.gson.JsonObject;
import de.mecrytv.model.ICacheModel;

public class WarnModel implements ICacheModel {

    private String warnId;
    private String playerUUID;
    private String reason;
    private String staffUUID;
    private String staffName;
    private long warnTimestamp;

    public WarnModel() { }

    public WarnModel(String warnId, String playerUUID, String reason, String staffUUID, String staffName, long warnTimestamp) {
        this.warnId = warnId;
        this.playerUUID = playerUUID;
        this.reason = reason;
        this.staffUUID = staffUUID;
        this.staffName = staffName;
        this.warnTimestamp = warnTimestamp;
    }

    @Override
    public String getIdentifier() {
        return warnId;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("warnId", warnId);
        json.addProperty("playerUUID", playerUUID);
        json.addProperty("reason", reason);
        json.addProperty("staffUUID", staffUUID);
        json.addProperty("staffName", staffName);
        json.addProperty("warnTimestamp", warnTimestamp);
        return json;
    }

    @Override
    public void deserialize(JsonObject data) {
        this.warnId = data.get("warnId").getAsString();
        this.playerUUID = data.get("playerUUID").getAsString();
        this.reason = data.get("reason").getAsString();
        this.staffUUID = data.get("staffUUID").getAsString();
        this.staffName = data.get("staffName").getAsString();
        this.warnTimestamp = data.get("warnTimestamp").getAsLong();
    }

    public String getWarnId() {
        return warnId;
    }
    public void setWarnId(String warnId) {
        this.warnId = warnId;
    }
    public String getPlayerUUID() {
        return playerUUID;
    }
    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public String getStaffUUID() {
        return staffUUID;
    }
    public void setStaffUUID(String staffUUID) {
        this.staffUUID = staffUUID;
    }
    public long getWarnTimestamp() {
        return warnTimestamp;
    }
    public void setWarnTimestamp(long warnTimestamp) {
        this.warnTimestamp = warnTimestamp;
    }
    public String getStaffName() {
        return staffName;
    }
    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }
}
