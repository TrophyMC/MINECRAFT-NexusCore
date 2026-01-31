package de.mecrytv.nexusCore.models.punish;

import com.google.gson.JsonObject;
import de.mecrytv.model.ICacheModel;
import de.mecrytv.nexusCore.enums.PunishTypes;

public class BanModel implements ICacheModel {

    private String banID;
    private String playerUUID;
    private String reason;
    private String staffUUID;
    private String staffName;
    private PunishTypes punishTypes;
    private long banTimestamp;
    private long banExpires;
    private String ipAddress;

    public BanModel() { }

    public BanModel(String banID, String playerUUID, String reason, String staffUUID, String staffName, PunishTypes punishTypes, long banTimestamp, long banExpires) {
        this.banID = banID;
        this.playerUUID = playerUUID;
        this.reason = reason;
        this.staffUUID = staffUUID;
        this.staffName = staffName;
        this.punishTypes = punishTypes;
        this.banTimestamp = banTimestamp;
        this.banExpires = banExpires;
        this.ipAddress = null;
    }

    public BanModel(String banID, String playerUUID, String reason, String staffUUID, String staffName, PunishTypes punishTypes, long banTimestamp, String ipAddress) {
        this.banID = banID;
        this.playerUUID = playerUUID;
        this.reason = reason;
        this.staffUUID = staffUUID;
        this.staffName = staffName;
        this.punishTypes = punishTypes;
        this.banTimestamp = banTimestamp;
        this.ipAddress = ipAddress;
        this.banExpires = -1;
    }

    @Override
    public String getIdentifier() {
        return banID;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("banID", banID);
        json.addProperty("playerUUID", playerUUID);
        json.addProperty("reason", reason);
        json.addProperty("staffUUID", staffUUID);
        json.addProperty("staffName", staffName);
        json.addProperty("banType", punishTypes.name());
        json.addProperty("banTimestamp", banTimestamp);
        json.addProperty("banExpires", banExpires);
        json.addProperty("ipAddress", ipAddress);
        return json;
    }

    @Override
    public void deserialize(JsonObject data) {
        this.banID = data.get("banID").getAsString();
        this.playerUUID = data.get("playerUUID").getAsString();
        this.reason = data.get("reason").getAsString();
        this.staffUUID = data.get("staffUUID").getAsString();
        this.staffName = data.get("staffName").getAsString();
        this.punishTypes = PunishTypes.valueOf(data.get("punishTypes").getAsString());
        this.banTimestamp = data.get("banTimestamp").getAsLong();
        this.banExpires = data.get("banExpires").getAsLong();
        if(data.has("ipAddress") && !data.get("ipAddress").isJsonNull()) {
            this.ipAddress = data.get("ipAddress").getAsString();
        } else {
            this.ipAddress = null;
        }
    }

    public String getStaffName() {
        return staffName;
    }
    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }
    public String getBanID() {
        return banID;
    }
    public void setBanID(String banID) {
        this.banID = banID;
    }
    public PunishTypes getPunishTypes() {
        return punishTypes;
    }
    public void setPunishTypes(PunishTypes punishTypes) {
        this.punishTypes = punishTypes;
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
