package com.example.wan_try.network;

import com.example.wan_try.dglab.sync.Reactive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DgLabDataUpdatePacket {
    private Integer strengthA;
    private Integer strengthB;
    private Integer strengthALimit;
    private Integer strengthBLimit;

    public static void encode(DgLabDataUpdatePacket msg, FriendlyByteBuf buf) {

        buf.writeInt(msg.strengthA);
        buf.writeInt(msg.strengthB);
        buf.writeInt(msg.strengthALimit);
        buf.writeInt(msg.strengthBLimit);
    }

    public static DgLabDataUpdatePacket decode(FriendlyByteBuf buf) {
        var context = new DgLabDataUpdatePacket();
        context.strengthA = buf.readInt();
        context.strengthB = buf.readInt();
        context.strengthALimit = buf.readInt();
        context.strengthBLimit = buf.readInt();
        return context;
    }

    public static void handle(DgLabDataUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> NetworkHandler.handleDgLabDataUpdate(msg));
        ctx.get().setPacketHandled(true);
    }
}
