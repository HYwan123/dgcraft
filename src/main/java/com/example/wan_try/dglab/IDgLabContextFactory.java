package com.example.wan_try.dglab;
import org.java_websocket.WebSocket;


public interface IDgLabContextFactory<T extends DGLabClient.DGLabContext> {
    T createDgLabContext(WebSocket conn,String targetId,String clientId);
}
