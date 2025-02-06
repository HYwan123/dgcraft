package com.example.wan_try.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin implements ClientGamePacketListener {
    @Shadow @Final private Minecraft minecraft;

    @Shadow private ClientLevel level;

    @Inject(method = "handleSetEntityData",at=@At("HEAD"))
    private void handleSetEntityData(ClientboundSetEntityDataPacket pPacket, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getId());
        if(entity instanceof LocalPlayer) {

            var health = pPacket.getUnpackedData().stream().filter(x->x.getAccessor().equals(LivingEntityAccessor.getDataHealthId())).findFirst();
            if(!health.isEmpty()) {

            }
        }
        if (entity != null && pPacket.getUnpackedData() != null) {
            entity.getEntityData().assignValues(pPacket.getUnpackedData());
        }
    }
}
