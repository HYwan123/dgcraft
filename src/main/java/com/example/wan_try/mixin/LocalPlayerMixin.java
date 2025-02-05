package com.example.wan_try.mixin;

import com.example.wan_try.ClientConfigHandler;
import com.example.wan_try.FeedbackGenerator;
import com.example.wan_try.Main;
import com.example.wan_try.QrCodeHandler;
import com.example.wan_try.dglab.MinecraftDgLabContext;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.lwjgl.system.CallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {
    @Unique
    FeedbackGenerator forge_1_18_2_40_2_21_mdk$generator = new FeedbackGenerator();

    public LocalPlayerMixin(Level pLevel, BlockPos pPos, float pYRot, GameProfile pGameProfile) {
        super(pLevel, pPos, pYRot, pGameProfile);
    }

    @Shadow
    private boolean flashOnSetHealth;


    @Inject(method = "hurtTo", at = @At("HEAD"))
    private void onHurt(float pHealth, CallbackInfo ci) {
        boolean a = Main.getInstance().getServer() == null;
        boolean b = FMLEnvironment.dist.isClient();
        boolean c = ClientConfigHandler.client.get();
        QrCodeHandler.getQrCodeScreen().setContext(Main.getInstance().getClient().getContext(getStringUUID()));
        if (a && b && c) {


            if (this.flashOnSetHealth) {
                if (pHealth <= 0.0f) {
                    forge_1_18_2_40_2_21_mdk$onDeath();
                    return;
                }

                // 获取当前实例的血量
                float currentHealth = getHealth();
                forge_1_18_2_40_2_21_mdk$onHurt(Math.max(getHealth() - pHealth,0));
                //QrCodeHandler.getQrCodeScreen ().setContext(Main.getInstance().getClient().getContext(getStringUUID()));
                //System.out.println("🔥 进入 hurtTo()，目标血量：" + pHealth);
                //System.out.println("💖 当前血量：" + currentHealth);
            }
        }
    }

    @Unique
    private void forge_1_18_2_40_2_21_mdk$onHurt(float damage) {
        if(damage <= 0.0f) return;
        if (ClientConfigHandler.pasento.get()) {
            this.forge_1_18_2_40_2_21_mdk$generator.sendHurtFeedback(this, Main.getInstance().getClient(), damage);
        } else {
            this.forge_1_18_2_40_2_21_mdk$generator.sendHurtFeedbackWan(this, Main.getInstance().getClient(), damage);
        }

    }


    @Unique
    private void forge_1_18_2_40_2_21_mdk$onDeath() {
        if (ClientConfigHandler.pasento.get()) {
            this.forge_1_18_2_40_2_21_mdk$generator.sendDeathFeedback(this, Main.getInstance().getClient());
        } else {
            this.forge_1_18_2_40_2_21_mdk$generator.sendDeathFeedbackWan(this, Main.getInstance().getClient());
        }


    }
}
