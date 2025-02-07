package com.example.wan_try.dglab;

import com.example.wan_try.dglab.api.IDgLabContextFactory;
import org.java_websocket.WebSocket;

public class MinecraftDgLabContextFactory implements IDgLabContextFactory<MinecraftDgLabContext> {
    @Override
    public MinecraftDgLabContext createDgLabContext(WebSocket conn, String targetId, String clientId) {
        return new MinecraftDgLabContext(conn,targetId,clientId);
    }
}
