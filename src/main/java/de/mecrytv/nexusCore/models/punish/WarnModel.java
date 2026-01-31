package de.mecrytv.nexusCore.models.punish;

import com.google.gson.JsonObject;
import de.mecrytv.model.ICacheModel;

public class WarnModel implements ICacheModel {

    private String id; // targetUUID:timestamp
    private String reportID;
    private String targetUUID;
    private String reason;
    private String staffUUID;
    private String staffName;
    private long warnTimestamp;

    public WarnModel() { }

    public WarnModel(String reportID, String targetUUID, String reason, String staffUUID, String staffName, long timestamp) {
        this.id = targetUUID + ":" + timestamp;
        this.reportID = reportID;
        this.targetUUID = targetUUID;
        this.reason = reason;
        this.staffUUID = staffUUID;
        this.staffName = staffName;
        this.warnTimestamp = timestamp;
    }

    @Override
    public String getIdentifier() { return id; }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("reportID", reportID);
        json.addProperty("targetUUID", targetUUID);
        json.addProperty("reason", reason);
        json.addProperty("staffUUID", staffUUID);
        json.addProperty("staffName", staffName);
        json.addProperty("warnTimestamp", warnTimestamp);
        return json;
    }

    @Override
    public void deserialize(JsonObject data) {
        this.id = data.has("id") ? data.get("id").getAsString() : (data.has("warnId") ? data.get("warnId").getAsString() : "");
        this.reportID = data.has("reportID") ? data.get("reportID").getAsString() : "none";
        this.targetUUID = data.has("targetUUID") ? data.get("targetUUID").getAsString() : (data.has("playerUUID") ? data.get("playerUUID").getAsString() : "");
        this.reason = data.get("reason").getAsString();
        this.staffUUID = data.get("staffUUID").getAsString();
        this.staffName = data.get("staffName").getAsString();
        this.warnTimestamp = data.get("warnTimestamp").getAsLong();
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

    public long getWarnTimestamp() {
        return warnTimestamp;
    }

    public void setWarnTimestamp(long warnTimestamp) {
        this.warnTimestamp = warnTimestamp;
    }
}
