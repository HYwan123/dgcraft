package com.example.wan_try.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class QRCodeRequestPacket {
    private final String playerUUID;

    public QRCodeRequestPacket(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public static void encode(QRCodeRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.playerUUID);
    }

    public static QRCodeRequestPacket decode(FriendlyByteBuf buf) {
        return new QRCodeRequestPacket(buf.readUtf());
    }

    public static void handle(QRCodeRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 处理逻辑在NetworkHandler中实现
            NetworkHandler.handleQRCodeRequest(msg.playerUUID, ctx.get().getSender());
        });
        ctx.get().setPacketHandled(true);
    }
} 