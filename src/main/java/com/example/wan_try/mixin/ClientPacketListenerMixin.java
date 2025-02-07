package com.example.wan_try.mixin;

import com.example.wan_try.CommonConfigHandler;
import com.example.wan_try.FeedbackGenerator;
import com.example.wan_try.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin implements ClientGamePacketListener {
    @Shadow @Final private Minecraft minecraft;
    @Unique
    FeedbackGenerator forge_1_18_2_40_2_21_mdk$generator = new FeedbackGenerator();

    @Shadow private ClientLevel level;

    @Inject(method = "handleSetEntityData",at=@At("HEAD"))
    private void handleSetEntityData(ClientboundSetEntityDataPacket pPacket, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getId());
        if(entity instanceof LocalPlayer player) {

            var health = pPacket.getUnpackedData().stream().filter(x->x.getAccessor().equals(LivingEntityAccessor.getDataHealthId())).findFirst();
            if(!health.isEmpty()) {
                float value = (float)health.get().getValue();
                forge_1_18_2_40_2_21_mdk$onHurt(player,player.getHealth()-value);
            }
        }
        if (entity != null && pPacket.getUnpackedData() != null) {
            entity.getEntityData().assignValues(pPacket.getUnpackedData());
        }
    }

    @Unique
    private void forge_1_18_2_40_2_21_mdk$onHurt(LocalPlayer player,float damage) {
        if(damage <= 0.0f) return;
        if (CommonConfigHandler.pasento.get()) {
            this.forge_1_18_2_40_2_21_mdk$generator.sendHurtFeedback(player, Main.getInstance().getClient(), damage);
        } else {
            this.forge_1_18_2_40_2_21_mdk$generator.sendHurtFeedbackWan(player, Main.getInstance().getClient(), damage);
        }

    }
}
