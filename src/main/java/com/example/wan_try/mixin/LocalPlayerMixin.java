package com.example.wan_try.mixin;

import com.example.wan_try.ClientConfigHandler;
import com.example.wan_try.FeedbackGenerator;
import com.example.wan_try.Main;
import com.example.wan_try.QrCodeHandler;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLEnvironment;
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

    @Shadow public abstract void tick();

    @Unique
    private int forge_1_18_2_40_2_21_mdk$tickCount =0;

    @Inject(method = "hurtTo", at = @At("HEAD"))

    private void onHurt(float pHealth, CallbackInfo ci) {
        if(Main.getInstance().getClient() == null) return;
        boolean a = Main.getInstance().getServer() == null;
        boolean b = FMLEnvironment.dist.isClient();
        boolean c = ClientConfigHandler.client.get();
        QrCodeHandler.getQrCodeScreen().setContext(Main.getInstance().getClient().getContext(getStringUUID()));
        if (a && b && c) {


            if (this.flashOnSetHealth) {
                if (pHealth <= 0.0f) {
                    forge_1_18_2_40_2_21_mdk$onDeath();
                }
                //QrCodeHandler.getQrCodeScreen ().setContext(Main.getInstance().getClient().getContext(getStringUUID()));
                //System.out.println("🔥 进入 hurtTo()，目标血量：" + pHealth);
                //System.out.println("💖 当前血量：" + currentHealth);
            }
        }
    }


    @Unique private float forge_1_18_2_40_2_21_mdk$lastHealth = getHealth();
    @Inject(method = "tick",at=@At("HEAD"))
    private void onTick(CallbackInfo ci){
        forge_1_18_2_40_2_21_mdk$tickCount++;
        boolean a = Main.getInstance().getServer() == null;
        boolean b = FMLEnvironment.dist.isClient();
        boolean c = ClientConfigHandler.client.get();
        if(!(a && b && c)) return;

        if(getHealth() < forge_1_18_2_40_2_21_mdk$lastHealth&& forge_1_18_2_40_2_21_mdk$tickCount >20){

            forge_1_18_2_40_2_21_mdk$tickCount =0;
            System.out.println("添加强度"+(forge_1_18_2_40_2_21_mdk$lastHealth-getHealth()));
            forge_1_18_2_40_2_21_mdk$onHurt(Math.max(forge_1_18_2_40_2_21_mdk$lastHealth-getHealth() ,0));

        }
        forge_1_18_2_40_2_21_mdk$lastHealth=getHealth();
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
