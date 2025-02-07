package com.example.wan_try;

import com.example.wan_try.dglab.*;

import com.example.wan_try.dglab.api.IDGLabClient;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;

public class FeedbackGenerator {
    private static final Logger LOGGER = LogManager.getLogger("WanDGLabTest");
    private static final int MIN_FREQUENCY = 20;
    private static final int MAX_FREQUENCY = 240;
    private static final int MAX_STRENGTH = 100;


    public void sendDeathFeedback(Player player, IDGLabClient<MinecraftDgLabContext> client) {
        List<MinecraftDgLabContext> contexts = client.getContext(player.getStringUUID());
        LOGGER.debug("Found {} DGLab contexts for player {}", 
            contexts.size(),
            player.getDisplayName().getString()
        );

        for (MinecraftDgLabContext context : contexts) {
            try {
                sendDeathWaveSequence(context);
                LOGGER.debug("Sent death feedback wave sequence for context {}", context);
            } catch (Exception e) {
                LOGGER.error("Failed to send death feedback for player {}",
                    player.getDisplayName().getString(), e);
            }
        }
        player.displayClientMessage(new TextComponent("你死了,感受痛苦吧!"), true);
    }


    public void sendHurtFeedback(Player player, IDGLabClient client, float originalDamage) {
        float maxHealth = player.getMaxHealth();
        int baseStrength = calculateBaseStrength(originalDamage, maxHealth);
        
        LOGGER.debug("Calculated base strength: {} (from damage/maxHealth: {}/{})", 
            baseStrength, originalDamage, maxHealth);
        
        List<DGLabClient.DGLabContext> contexts = client.getContext(player.getStringUUID());
        for (DGLabClient.DGLabContext context : contexts) {
            try {
                sendHurtWaveSequence((MinecraftDgLabContext) context, baseStrength);
                LOGGER.debug("Sent hurt feedback wave sequence (strength: {})", baseStrength);
            } catch (Exception e) {
                LOGGER.error("Failed to send hurt feedback for player {}", 
                    player.getDisplayName().getString(), e);
            }
        }
    }

    private void sendDeathWaveSequence(MinecraftDgLabContext context) {
        setMaxStrength(context);
        WaveSequence sequence = createDeathWaveSequence();

        // 死亡波形优先级更高
        context.addWaveform("A", sequence, 3000, 2); // 3秒持续时间，优先级2
        
        WaveSequence currentWaveform = context.getCurrentWaveform("A");
        if (currentWaveform != null) {
            context.clearWaveForm("A");
            context.sendWaveForm("A", currentWaveform);
        }
    }

    private void sendHurtWaveSequence(MinecraftDgLabContext context, int baseStrength) {
        setMaxStrength(context);
        WaveSequence sequence = createHurtWaveSequence(baseStrength);
        
        // 使用波形管理器添加波形
        context.addWaveform("A", sequence, 1000, 1); // 1秒持续时间，优先级1
        context.addWaveform("B", sequence, 1000, 1);
        
        // 获取当前应该发送的波形（包含可能的叠加效果）
        WaveSequence currentWaveformA = context.getCurrentWaveform("A");
        WaveSequence currentWaveformB = context.getCurrentWaveform("B");
        
        if (currentWaveformA != null) {
            context.sendWaveForm("A", currentWaveformA);
        }
        if (currentWaveformB != null) {
            context.sendWaveForm("B", currentWaveformB);
        }
    }

    private void setMaxStrength(DGLabClient.DGLabContext context) {
        context.getStrengthA().getSideB().update(context.getStrengthALimit().get());
        context.getStrengthB().getSideB().update(context.getStrengthBLimit().get());
    }


    //wantest
    public void addStrength(DGLabClient.DGLabContext context ,int addValue) {
        context.addStrengthChange(addValue);
    }
    public void minusStrength(DGLabClient.DGLabContext context , int minusValue) {
        context.minusStrengthChange(minusValue);
    }

    public void sendDeathFeedbackWan(Player player, IDGLabClient<MinecraftDgLabContext> client) {

        List<MinecraftDgLabContext> contexts = client.getContext(player.getStringUUID());
        for (MinecraftDgLabContext context : contexts) {
            try {
                CompletableFuture<Object> future = CompletableFuture.supplyAsync(()->{

                    setMaxStrength(context);
                    player.displayClientMessage(new TextComponent("你死了"), true);
                    sendDefinedWaveForm(context);
                    context.getStrengthA().getSideB().update(0);
                    context.getStrengthB().getSideB().update(0);
                    return null;
                });

            } catch (Exception e) {
                LOGGER.error("Failed to send hurt feedback for player {}",
                        player.getDisplayName().getString(), e);
            }
        }


    }
    public void sendHurtFeedbackWan(Player player, IDGLabClient<MinecraftDgLabContext> client, float originalDamage) {
        if(client == null) return;
        double addValue = CommonConfigHandler.DAMAGE_INTENSITY_INCREMENT.get()*originalDamage;
        System.out.println("\naddValue:"+addValue);
        System.out.println("\nClientConfigHandler.DAMAGE_INTENSITY_INCREMENT.get():"+ CommonConfigHandler.DAMAGE_INTENSITY_INCREMENT.get());
        System.out.println("\noriginalDamage:"+originalDamage);
        List<MinecraftDgLabContext> contexts = client.getContext(player.getStringUUID());
        addValue = Math.min(addValue, 200);
        for (MinecraftDgLabContext context : contexts) {
            try {
                double finalAddValue = addValue;
                CompletableFuture<Object> future = CompletableFuture.supplyAsync(()->{

                    addStrength(context,(int) finalAddValue);
                    player.displayClientMessage(new TextComponent("强度增加: " + (int) finalAddValue), true);
                    sendDefinedWaveForm(context);
                    minusStrength(context, (int) finalAddValue);
                    return null;
                });

            } catch (Exception e) {
                LOGGER.error("Failed to send hurt feedback for player {}",
                        player.getDisplayName().getString(), e);
            }
        }


    }

    private void sendDefinedWaveForm(MinecraftDgLabContext context) {
        WaveSequence sequence = creatHuntSquareWave();
        context.clearWaveForm("A");
        context.sendWaveForm("A", sequence);
        context.clearWaveForm("B");
        context.sendWaveForm("B", sequence);
        try {
            sleep(CommonConfigHandler.DAMAGE_WAVEFORM_DURATION.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public WaveSequence creatHuntSquareWave(){
        WaveSequence sequence = new WaveSequence();
        Wave wave = new Wave(
                new int[]{20, 20, 20, 20},
                new int[]{100, 100, 100, 100}
        );
        int count= CommonConfigHandler.DAMAGE_WAVEFORM_DURATION.get()/25;
        for(int i = 0; i < count; i++) {
            sequence.add(wave);
        }
        return sequence;
    }

    //wantest end
    private WaveSequence createDeathWaveSequence() {
        WaveSequence sequence = new WaveSequence();
        Wave wave = new Wave(
            new int[]{20, 20, 20, 20},
            new int[]{85, 90, 95, 100}
        );
        for(int i = 0; i < 12; i++) {
            sequence.add(wave);
        }
        return sequence;
    }

    private WaveSequence createHurtWaveSequence(int baseStrength) {
        int baseFreq = calculateBaseFrequency(baseStrength);
        Wave wave = createHurtWave(baseFreq, baseStrength);
        WaveSequence sequence = new WaveSequence();
        sequence.add(wave);
        sequence.add(wave);
        return sequence;
    }

    private Wave createHurtWave(int baseFreq, int baseStrength) {
        return new Wave(
            new int[]{
                baseFreq,
                Math.max(MIN_FREQUENCY, baseFreq - 10),
                Math.max(MIN_FREQUENCY, baseFreq - 20),
                Math.max(MIN_FREQUENCY, baseFreq - 30)
            },
            new int[]{
                Math.min(MAX_STRENGTH, baseStrength),
                Math.min(MAX_STRENGTH, baseStrength),
                Math.min(MAX_STRENGTH, (int)(baseStrength * 0.9)),
                Math.min(MAX_STRENGTH, (int)(baseStrength * 0.8))
            }
        );
    }

    private int calculateBaseFrequency(int baseStrength) {
        int baseFreq = MAX_FREQUENCY - (baseStrength * 2);
        return Math.max(MIN_FREQUENCY, baseFreq);
    }

    private int calculateBaseStrength(float damage, float maxHealth) {
        return (int) Math.min(MAX_STRENGTH, (damage / maxHealth) * 100);
    }
} 