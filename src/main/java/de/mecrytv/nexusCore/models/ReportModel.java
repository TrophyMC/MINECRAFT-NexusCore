package de.mecrytv.nexusCore.models;

import com.google.gson.JsonObject;
import de.mecrytv.model.ICacheModel;

public class ReportModel implements ICacheModel {

    private String reportID;
    private String targetUUID;
    private String targetName;
    private String reporterUUID;
    private String reporterName;
    private long reportTime;
    private String reason;
    private String state;
    private String staffUUID;
    private String staffName;

    public ReportModel() {}

    public ReportModel(String targetUUID, String targetName, String reporterUUID, String reporterName, String reason) {
        this.reportID = java.util.UUID.randomUUID().toString();
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.reporterUUID = reporterUUID;
        this.reporterName = reporterName;
        this.reportTime = System.currentTimeMillis();
        this.reason = reason;
        this.state = "OPEN";
        this.staffUUID = "none";
        this.staffName = "none";
    }

    @Override
    public String getIdentifier() {
        return reportID;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("id", reportID);
        json.addProperty("targetUUID", targetUUID);
        json.addProperty("targetName", targetName);
        json.addProperty("reporterUUID", reporterUUID);
        json.addProperty("reporterName", reporterName);
        json.addProperty("reportTime", reportTime);
        json.addProperty("reason", reason);
        json.addProperty("state", state);
        json.addProperty("staffUUID", staffUUID);
        json.addProperty("staffName", staffName);
        return json;
    }

    @Override
    public void deserialize(JsonObject data) {
        this.reportID = data.has("id") ? data.get("id").getAsString() : data.get("reportID").getAsString();
        this.targetUUID = data.get("targetUUID").getAsString();
        this.targetName = data.get("targetName").getAsString();
        this.reporterUUID = data.get("reporterUUID").getAsString();
        this.reporterName = data.get("reporterName").getAsString();
        this.reportTime = data.get("reportTime").getAsLong();
        this.reason = data.get("reason").getAsString();
        this.state = data.get("state").getAsString();
        this.staffUUID = data.has("staffUUID") ? data.get("staffUUID").getAsString() : "none";
        this.staffName = data.has("staffName") ? data.get("staffName").getAsString() : "none";
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
    public String getReporterUUID() {
        return reporterUUID;
    }
    public void setReporterUUID(String reporterUUID) {
        this.reporterUUID = reporterUUID;
    }
    public long getReportTime() {
        return reportTime;
    }
    public void setReportTime(long reportTime) {
        this.reportTime = reportTime;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getStaffUUID() {
        return staffUUID;
    }
    public void setStaffUUID(String staffUUID) {
        this.staffUUID = staffUUID;
    }
    public String getTargetName() {
        return targetName;
    }
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
    public String getReporterName() {
        return reporterName;
    }
    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }
    public String getStaffName() {
        return staffName;
    }
    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }
}
