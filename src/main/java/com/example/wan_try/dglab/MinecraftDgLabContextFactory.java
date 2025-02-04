package com.example.wan_try.dglab;

import org.java_websocket.WebSocket;

public class MinecraftDgLabContextFactory implements IDgLabContextFactory<MinecraftDgLabContext>{
    @Override
    public MinecraftDgLabContext createDgLabContext(WebSocket conn, String targetId, String clientId) {
        return new MinecraftDgLabContext(conn,targetId,clientId);
    }
}
