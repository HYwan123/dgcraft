package com.example.wan_try.dglab;


import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Arrays;


public class DgLabPack {

    private DgLabPackType type;

    public void setType(DgLabPackType type) {
        this.type = type;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String clientId;
    private String targetId; 
    private String message;

    private DgLabPack(DgLabPackType type, String clientId, String targetId, String message) {
        if (message != null && message.length() > 1950) {
            throw new IllegalArgumentException("Message length cannot exceed 1950 characters");
        }
        
        if (type != DgLabPackType.Bind && (clientId == null || targetId == null || message == null)) {
            throw new IllegalArgumentException("clientId, targetId and message cannot be null except for bind type");
        }

        this.type = type;
        this.clientId = clientId;
        this.targetId = targetId;
        this.message = message;
    }

    public static DgLabPack createBindPack(String clientId, String targetId, String message) {
        return new DgLabPack(DgLabPackType.Bind, clientId,targetId, message);
    }

    public static DgLabPack createMessagePack(String clientId, String targetId, String message) {
        return new DgLabPack(DgLabPackType.Msg, clientId, targetId, message);
    }

    public static DgLabPack createBreakPack(String clientId, String targetId, String command) {
        return new DgLabPack(DgLabPackType.Break, clientId, targetId, command);
    }

    public static DgLabPack createHeartBeatPack(String clientId, String targetId, String response) {
        return new DgLabPack(DgLabPackType.Heartbeat, clientId, targetId, response);
    }

    public DgLabPackType getType() {
        return type;
    }

    public String getClientId() {
        return clientId;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getMessage() {
        return message;
    }

    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type.getValue());
        json.addProperty("clientId", clientId);
        json.addProperty("targetId", targetId);
        json.addProperty("message", message);
        return json.toString();
    }

    public static DgLabPack fromJson(String jsonStr) {
        JsonObject json = new Gson().fromJson(jsonStr, JsonObject.class);
        DgLabPackType type = Arrays.stream(DgLabPackType.values())
                .filter(x -> x.getValue().equals(json.get("type").getAsString()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid type value"));
        String clientId = json.get("clientId").getAsString();
        String targetId = json.get("targetId").getAsString();
        String message = json.get("message").getAsString();
        return new DgLabPack(type, clientId, targetId, message);
    }
}
