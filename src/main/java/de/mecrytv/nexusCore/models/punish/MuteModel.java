package de.mecrytv.nexusCore.models.punish;

import com.google.gson.JsonObject;
import de.mecrytv.model.ICacheModel;

public class MuteModel implements ICacheModel {

    private String muteID;
    private String playerUUID;
    private String reason;
    private String staffUUID;
    private String staffName;
    private long muteTimestamp;
    private long muteExpires;

    public MuteModel() { }

    public MuteModel(String muteID, String playerUUID, String reason, String staffUUID, String staffName, long muteTimestamp, long muteExpires) {
        this.muteID = muteID;
        this.playerUUID = playerUUID;
        this.reason = reason;
        this.staffUUID = staffUUID;
        this.staffName = staffName;
        this.muteTimestamp = muteTimestamp;
        this.muteExpires = muteExpires;
    }

    public MuteModel(String muteID, String playerUUID, String reason, String staffUUID, String staffName, long muteTimestamp) {
        this.muteID = muteID;
        this.playerUUID = playerUUID;
        this.reason = reason;
        this.staffUUID = staffUUID;
        this.staffName = staffName;
        this.muteTimestamp = muteTimestamp;
        this.muteExpires = -1;
    }

    @Override
    public String getIdentifier() {
        return muteID;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("muteID", muteID);
        json.addProperty("playerUUID", playerUUID);
        json.addProperty("reason", reason);
        json.addProperty("staffUUID", staffUUID);
        json.addProperty("staffName", staffName);
        json.addProperty("muteTimestamp", muteTimestamp);
        json.addProperty("muteExpires", muteExpires);
        return json;
    }

    @Override
    public void deserialize(JsonObject data) {
        this.muteID = data.get("muteID").getAsString();
        this.playerUUID = data.get("playerUUID").getAsString();
        this.reason = data.get("reason").getAsString();
        this.staffUUID = data.get("staffUUID").getAsString();
        this.staffName = data.get("staffName").getAsString();
        this.muteTimestamp = data.get("muteTimestamp").getAsLong();
        this.muteExpires = data.get("muteExpires").getAsLong();
    }

    public String getMuteID() {
        return muteID;
    }
    public void setMuteID(String muteID) {
        this.muteID = muteID;
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
    public String getStaffName() {
        return staffName;
    }
    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }
}
