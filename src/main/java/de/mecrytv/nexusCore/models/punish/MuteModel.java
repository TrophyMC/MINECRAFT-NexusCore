package de.mecrytv.nexusCore.models.punish;

import com.google.gson.JsonObject;
import de.mecrytv.model.ICacheModel;

public class MuteModel implements ICacheModel {

    private String playerUUID;
    private String reason;
    private String staffUUID;
    private long muteTimestamp;
    private long muteExpires;

    public MuteModel() { }

    public MuteModel(String playerUUID, String reason, String staffUUID, long muteTimestamp, long muteExpires) {
        this.playerUUID = playerUUID;
        this.reason = reason;
        this.staffUUID = staffUUID;
        this.muteTimestamp = muteTimestamp;
        this.muteExpires = muteExpires;
    }

    @Override
    public String getIdentifier() {
        return playerUUID;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("playerUUID", playerUUID);
        json.addProperty("reason", reason);
        json.addProperty("staffUUID", staffUUID);
        json.addProperty("muteTimestamp", muteTimestamp);
        json.addProperty("muteExpires", muteExpires);
        return json;
    }

    @Override
    public void deserialize(JsonObject data) {
        this.playerUUID = data.get("playerUUID").getAsString();
        this.reason = data.get("reason").getAsString();
        this.staffUUID = data.get("staffUUID").getAsString();
        this.muteTimestamp = data.get("muteTimestamp").getAsLong();
        this.muteExpires = data.get("muteExpires").getAsLong();
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
    public long getMuteTimestamp() {
        return muteTimestamp;
    }
    public void setMuteTimestamp(long muteTimestamp) {
        this.muteTimestamp = muteTimestamp;
    }
    public long getMuteExpires() {
        return muteExpires;
    }
    public void setMuteExpires(long muteExpires) {
        this.muteExpires = muteExpires;
    }
}
