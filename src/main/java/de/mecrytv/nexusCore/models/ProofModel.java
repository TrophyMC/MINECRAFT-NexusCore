package de.mecrytv.nexusCore.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.mecrytv.model.ICacheModel;
import de.mecrytv.nexusCore.enums.ProofType;

import java.util.ArrayList;
import java.util.List;

public class ProofModel implements ICacheModel {

    private String reportID;
    private Enum<ProofType> type;
    private List<String> data = new ArrayList<>();

    public ProofModel(){}

    public ProofModel(String reportID, Enum<ProofType> type, List<String> data){
        this.reportID = reportID;
        this.type = type;
        this.data = data;
    }

    @Override
    public String getIdentifier() {
        return reportID;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("reportID", reportID);
        json.addProperty("type", type.name());

        JsonArray array = new JsonArray();
        data.forEach(array::add);
        json.add("data", array);

        return json;
    }

    @Override
    public void deserialize(JsonObject dataJson) {
        this.reportID = dataJson.get("reportID").getAsString();
        this.type = ProofType.valueOf(dataJson.get("type").getAsString());
        this.data = new ArrayList<>();
        dataJson.getAsJsonArray("data").forEach(el -> this.data.add(el.getAsString()));
    }

    public Enum<ProofType> getType() { return type; }
    public List<String> getData() { return data; }
}
