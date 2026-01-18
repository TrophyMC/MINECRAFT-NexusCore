package de.mecrytv.nexusCore.models;

import com.google.gson.JsonObject;
import de.mecrytv.model.ICacheModel;

public class TeleportModel implements ICacheModel {

    private String staffUUID;
    private String targetUUID;
    private String targetName;

    public TeleportModel() {}

    public TeleportModel(String staffUUID, String targetUUID, String targetName) {
        this.staffUUID = staffUUID;
        this.targetUUID = targetUUID;
        this.targetName = targetName;
    }

    @Override
    public String getIdentifier() {
        return staffUUID;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("staffUUID", staffUUID);
        json.addProperty("targetUUID", targetUUID);
        json.addProperty("targetName", targetName);
        return json;
    }

    @Override
    public void deserialize(JsonObject data) {
        this.staffUUID = data.get("staffUUID").getAsString();
        this.targetUUID = data.get("targetUUID").getAsString();
        this.targetName = data.get("targetName").getAsString();
    }

    public String getTargetUUID() { return targetUUID; }
    public String getTargetName() {
        return targetName;
    }
}
