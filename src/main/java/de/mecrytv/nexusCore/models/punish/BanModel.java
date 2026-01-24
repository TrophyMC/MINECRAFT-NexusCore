package de.mecrytv.nexusCore.models.punish;

import com.google.gson.JsonObject;
import de.mecrytv.model.ICacheModel;
import de.mecrytv.nexusCore.enums.BanType;

public class BanModel implements ICacheModel {

    private String playerUUID;
    private String reason;
    private String staffUUID;
    private BanType banType;
    private long banTimestamp;
    private long banExpires;
    private String ipAddress;

    public BanModel() { }

    public BanModel(String playerUUID, String reason, String staffUUID, BanType banType, long banTimestamp, long banExpires) {
        this.playerUUID = playerUUID;
        this.reason = reason;
        this.staffUUID = staffUUID;
        this.banType = banType;
        this.banTimestamp = banTimestamp;
        this.banExpires = banExpires;
        this.ipAddress = null;
    }

    public BanModel(String playerUUID, String reason, String staffUUID, BanType banType, long banTimestamp, String ipAddress) {
        this.playerUUID = playerUUID;
        this.reason = reason;
        this.staffUUID = staffUUID;
        this.banType = banType;
        this.banTimestamp = banTimestamp;
        this.ipAddress = ipAddress;
        this.banExpires = -1;
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
        json.addProperty("banType", banType.name());
        json.addProperty("banTimestamp", banTimestamp);
        json.addProperty("banExpires", banExpires);
        json.addProperty("ipAddress", ipAddress);
        return json;
    }

    @Override
    public void deserialize(JsonObject data) {
        this.playerUUID = data.get("playerUUID").getAsString();
        this.reason = data.get("reason").getAsString();
        this.staffUUID = data.get("staffUUID").getAsString();
        this.banType = BanType.valueOf(data.get("banType").getAsString());
        this.banTimestamp = data.get("banTimestamp").getAsLong();
        this.banExpires = data.get("banExpires").getAsLong();
        if(data.has("ipAddress") && !data.get("ipAddress").isJsonNull()) {
            this.ipAddress = data.get("ipAddress").getAsString();
        } else {
            this.ipAddress = null;
        }
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
    public BanType getBanType() {
        return banType;
    }
    public void setBanType(BanType banType) {
        this.banType = banType;
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
