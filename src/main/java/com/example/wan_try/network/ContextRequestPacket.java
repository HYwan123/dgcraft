package com.example.wan_try.network;

import com.example.wan_try.Main;
import com.example.wan_try.dglab.DGLabClient;
import com.example.wan_try.dglab.MinecraftDgLabContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ContextRequestPacket {

    public static void encode(ContextRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(1);
    }

    public static ContextRequestPacket decode(FriendlyByteBuf buf){
        buf.readInt();
        return new ContextRequestPacket();
    }

    public static void handle(ContextRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 处理逻辑在NetworkHandler中实现
            MinecraftDgLabContext dgctx = Main.getInstance().getClient().getContext(ctx.get().getSender().getStringUUID()).get(0);
            dgctx.notifyPlayer();
        });
        ctx.get().setPacketHandled(true);
    }
}
