package de.mecrytv.nexusCore.models;

import com.google.gson.JsonObject;
import de.mecrytv.model.ICacheModel;

public class SkinCacheModel implements ICacheModel {

    private String uuid;
    private String textureValue;
    private long lastUpdated;

    public SkinCacheModel() {}

    public SkinCacheModel(String uuid, String textureValue) {
        this.uuid = uuid;
        this.textureValue = textureValue;
        this.lastUpdated = System.currentTimeMillis();
    }

    @Override
    public String getIdentifier() {
        return uuid;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("uuid", uuid);
        json.addProperty("textureValue", textureValue);
        json.addProperty("lastUpdated", lastUpdated);
        return json;
    }

    @Override
    public void deserialize(JsonObject data) {
        this.uuid = data.get("uuid").getAsString();
        this.textureValue = data.get("textureValue").getAsString();
        this.lastUpdated = data.get("lastUpdated").getAsLong();
    }

    public String getTextureValue() { return textureValue; }
    public long getLastUpdated() {
        return lastUpdated;
    }
}
