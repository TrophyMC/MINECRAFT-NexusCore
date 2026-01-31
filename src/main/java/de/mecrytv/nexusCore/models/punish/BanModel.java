package de.mecrytv.nexusCore.models.punish;

import com.google.gson.JsonObject;
import de.mecrytv.model.ICacheModel;
import de.mecrytv.nexusCore.enums.PunishTypes;

public class BanModel implements ICacheModel {

    private String id;
    private String reportID;
    private String targetUUID;
    private String reason;
    private String staffUUID;
    private String staffName;
    private PunishTypes punishTypes;
    private long banTimestamp;
    private long banExpires;
    private String ipAddress;

    public BanModel() { }

    public BanModel(String reportID, String targetUUID, String reason, String staffUUID, String staffName, PunishTypes punishTypes, long banTimestamp, long banExpires) {
        this.id = targetUUID + ":" + banTimestamp;
        this.reportID = reportID;
        this.targetUUID = targetUUID;
        this.reason = reason;
        this.staffUUID = staffUUID;
        this.staffName = staffName;
        this.punishTypes = punishTypes;
        this.banTimestamp = banTimestamp;
        this.banExpires = banExpires;
    }

    public BanModel(String reportID, String targetUUID, String reason, String staffUUID, String staffName, PunishTypes punishTypes, long banTimestamp, String ipAddress) {
        this.id = targetUUID + ":" + banTimestamp;
        this.reportID = reportID;
        this.targetUUID = targetUUID;
        this.reason = reason;
        this.staffUUID = staffUUID;
        this.staffName = staffName;
        this.punishTypes = punishTypes;
        this.banTimestamp = banTimestamp;
        this.ipAddress = ipAddress;
        this.banExpires = -1;
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
        json.addProperty("punishTypes", punishTypes.name());
        json.addProperty("banTimestamp", banTimestamp);
        json.addProperty("banExpires", banExpires);
        json.addProperty("ipAddress", ipAddress);
        return json;
    }

    @Override
    public void deserialize(JsonObject data) {
        this.id = data.has("id") ? data.get("id").getAsString() : (data.has("banID") ? data.get("banID").getAsString() : "");
        this.reportID = data.has("reportID") ? data.get("reportID").getAsString() : "none";
        this.targetUUID = data.has("targetUUID") ? data.get("targetUUID").getAsString() : (data.has("playerUUID") ? data.get("playerUUID").getAsString() : "");
        this.reason = data.get("reason").getAsString();
        this.staffUUID = data.get("staffUUID").getAsString();
        this.staffName = data.get("staffName").getAsString();
        this.punishTypes = PunishTypes.valueOf(data.get("punishTypes").getAsString());
        this.banTimestamp = data.get("banTimestamp").getAsLong();
        this.banExpires = data.get("banExpires").getAsLong();
        this.ipAddress = (data.has("ipAddress") && !data.get("ipAddress").isJsonNull()) ? data.get("ipAddress").getAsString() : null;
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

    public PunishTypes getPunishTypes() {
        return punishTypes;
    }

    public void setPunishTypes(PunishTypes punishTypes) {
        this.punishTypes = punishTypes;
    }

    public long getBanTimestamp() {
        return banTimestamp;
    }

    public void setBanTimestamp(long banTimestamp) {
        this.banTimestamp = banTimestamp;
    }

    public long getBanExpires() {
        return banExpires;
    }

    public void setBanExpires(long banExpires) {
        this.banExpires = banExpires;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
