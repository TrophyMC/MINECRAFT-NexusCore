package de.mecrytv.nexusCore.models.punish;

import com.google.gson.JsonObject;
import de.mecrytv.model.ICacheModel;

public class MuteModel implements ICacheModel {

    private String id; // targetUUID:timestamp
    private String reportID;
    private String targetUUID;
    private String reason;
    private String staffUUID;
    private String staffName;
    private long muteTimestamp;
    private long muteExpires;

    public MuteModel() {
    }

    public MuteModel(String reportID, String targetUUID, String reason, String staffUUID, String staffName, long timestamp, long expiry) {
        this.id = targetUUID + ":" + timestamp;
        this.reportID = reportID;
        this.targetUUID = targetUUID;
        this.reason = reason;
        this.staffUUID = staffUUID;
        this.staffName = staffName;
        this.muteTimestamp = timestamp;
        this.muteExpires = expiry;
    }

    public MuteModel(String reportID, String targetUUID, String reason, String staffUUID, String staffName, long timestamp) {
        this(reportID, targetUUID, reason, staffUUID, staffName, timestamp, -1L);
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("reportID", reportID);
        json.addProperty("targetUUID", targetUUID);
        json.addProperty("reason", reason);
        json.addProperty("staffUUID", staffUUID);
        json.addProperty("staffName", staffName);
        json.addProperty("muteTimestamp", muteTimestamp);
        json.addProperty("muteExpires", muteExpires);
        return json;
    }

    @Override
    public void deserialize(JsonObject data) {
        this.id = data.has("id") ? data.get("id").getAsString() : "";
        this.reportID = data.has("reportID") ? data.get("reportID").getAsString() : "none";
        this.targetUUID = data.has("targetUUID") ? data.get("targetUUID").getAsString() : "";
        this.reason = data.get("reason").getAsString();
        this.staffUUID = data.get("staffUUID").getAsString();
        this.staffName = data.get("staffName").getAsString();
        this.muteTimestamp = data.get("muteTimestamp").getAsLong();
        this.muteExpires = data.get("muteExpires").getAsLong();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReportID() {
        return reportID;
    }

    public void setReportID(String reportID) {
        this.reportID = reportID;
    }

    public String getTargetUUID() {
        return targetUUID;
    }

    public void setTargetUUID(String targetUUID) {
        this.targetUUID = targetUUID;
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

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
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