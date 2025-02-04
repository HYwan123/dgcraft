package com.example.wan_try.dglab;

import com.example.wan_try.Main;
import com.example.wan_try.network.NetworkHandler;
import com.example.wan_try.network.QRCodeRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.java_websocket.WebSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class MinecraftDgLabContext extends DGLabClient.DGLabContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftDgLabContext.class);
    
    // 存储每个通道当前活跃的波形
    private final Map<String, List<ActiveWaveform>> activeWaveforms = new ConcurrentHashMap<>();
    // 存储每个通道的基础强度
    private final Map<String, Integer> baseStrengths = new ConcurrentHashMap<>();

    public MinecraftDgLabContext(WebSocket conn,String targetId,String clientId){
        super(conn,targetId,clientId);
    }



    @Override
    protected void setStrengthAWithOutNotify(int strengthA) {
        super.setStrengthAWithOutNotify(strengthA);

        notifyPlayer();
    }

    @Override
    protected void setStrengthBWithOutNotify(int strengthB) {
        super.setStrengthBWithOutNotify(strengthB);
        notifyPlayer();
    }



    public void notifyPlayer(){
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if(server != null){
            UUID playerId = UUID.fromString(this.getClientId());
            if(playerId.equals(Minecraft.getInstance().player.getUUID())) return;
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            NetworkHandler.INSTANCE.sendTo(this,player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        }

    }

    @Override
    public void addStrengthChange(int addValue) {
        super.addStrengthChange(addValue);
        notifyPlayer();
    }

    @Override
    public void minusStrengthChange(int minusValue) {
        super.minusStrengthChange(minusValue);
        notifyPlayer();
    }
    //TODO: why decode is called twice
    public static void encode(MinecraftDgLabContext msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getStrengthA());
        buf.writeInt(msg.getStrengthB());
        buf.writeInt(msg.getStrengthALimit());
        buf.writeInt(msg.getStrengthBLimit());

    }

    public static MinecraftDgLabContext decode(FriendlyByteBuf buf) {
        MinecraftDgLabContext context = new MinecraftDgLabContext(null,null,null);
        context.setStrengthAWithOutNotify(buf.readInt());
        context.setStrengthBWithOutNotify(buf.readInt());
        context.setStrengthALimit(buf.readInt());
        context.setStrengthBLimit(buf.readInt());
        return context;
    }

    public static void handle(MinecraftDgLabContext msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 处理逻辑在NetworkHandler中实现
            Main.getInstance().getQrCodeScreen().setContext(Arrays.stream(new MinecraftDgLabContext[] {msg}).toList());
        });
        ctx.get().setPacketHandled(true);
    }

    // 表示一个正在进行的波形
    private static class ActiveWaveform {
        final WaveSequence sequence;
        final long startTime;
        final long duration;
        final int priority;

        ActiveWaveform(WaveSequence sequence, long duration, int priority) {
            this.sequence = sequence;
            this.startTime = System.currentTimeMillis();
            this.duration = duration;
            this.priority = priority;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - startTime > duration;
        }
    }


    /**
     * 添加新的波形序列
     */
    public void addWaveform(String channel, WaveSequence sequence, long durationMs, int priority) {
        activeWaveforms.computeIfAbsent(channel, k -> new ArrayList<>())
            .add(new ActiveWaveform(sequence, durationMs, priority));
        LOGGER.debug("Added waveform to channel {} with duration {}ms and priority {}", 
            channel, durationMs, priority);
    }

    /**
     * 获取当前应该发送的波形
     */
    public WaveSequence getCurrentWaveform(String channel) {
        List<ActiveWaveform> waveforms = activeWaveforms.get(channel);
        if (waveforms == null || waveforms.isEmpty()) {
            return null;
        }

        // 清理过期的波形
        waveforms.removeIf(w -> {
            boolean expired = w.isExpired();
            if (expired) {
                LOGGER.debug("Removed expired waveform from channel {}", channel);
            }
            return expired;
        });

        if (waveforms.isEmpty()) {
            return null;
        }

        // 如果只有一个波形，直接返回
        if (waveforms.size() == 1) {
            return waveforms.get(0).sequence;
        }

        // 按优先级排序
        waveforms.sort((a, b) -> Integer.compare(b.priority, a.priority));

        // 合并波形
        return mergeWaveforms(waveforms);
    }

    /**
     * 合并多个波形序列
     */
    private WaveSequence mergeWaveforms(List<ActiveWaveform> waveforms) {
        WaveSequence merged = new WaveSequence();
        
        // 获取所有序列中最长的波形长度
        int maxLength = waveforms.stream()
            .mapToInt(w -> w.sequence.size())
            .max()
            .orElse(0);

        for (int i = 0; i < maxLength; i++) {
            Wave mergedWave = null;
            
            for (ActiveWaveform activeWave : waveforms) {
                List<Wave> waves = activeWave.sequence;
                if (i >= waves.size()) continue;
                
                Wave currentWave = waves.get(i);
                if (mergedWave == null) {
                    mergedWave = currentWave;
                } else {
                    mergedWave = mergeWaves(mergedWave, currentWave, activeWave.priority);
                }
            }
            
            if (mergedWave != null) {
                merged.add(mergedWave);
            }
        }

        return merged;
    }

    /**
     * 合并两个波形，考虑优先级
     */
    private Wave mergeWaves(Wave wave1, Wave wave2, int priority2) {
        int[] mergedFreqs = new int[wave1.getFrequency().length];
        int[] mergedStrengths = new int[wave1.getStrength().length];
        
        for (int i = 0; i < mergedFreqs.length; i++) {
            // 频率使用优先级高的波形的值
            mergedFreqs[i] = wave2.getFrequency()[i];
            
            // 强度值叠加，但要考虑上限
            int strength = wave1.getStrength()[i] + wave2.getStrength()[i];
            mergedStrengths[i] = Math.min(strength, getStrengthLimit()); 
        }
        
        return new Wave(mergedFreqs, mergedStrengths);
    }

    /**
     * 获取强度上限
     */
    private int getStrengthLimit() {
        return Math.max(getStrengthALimit(), getStrengthBLimit());
    }

    /**
     * 设置通道的基础强度
     */
    public void setBaseStrength(String channel, int strength) {
        baseStrengths.put(channel, strength);
    }

    /**
     * 清除通道的所有波形
     */
    @Override
    public void clearWaveForm(String channel) {
        super.clearWaveForm(channel);
        activeWaveforms.remove(channel);
        baseStrengths.remove(channel);
        LOGGER.debug("Cleared all waveforms from channel {}", channel);
    }

    /**
     * 发送波形前进行合并处理
     */
    @Override
    public void sendWaveForm(String channel, WaveSequence sequence) {
        // 添加到活跃波形列表
        addWaveform(channel, sequence, 1000, 1); // 默认1秒持续时间，优先级1
        
        // 获取合并后的波形
        WaveSequence mergedSequence = getCurrentWaveform(channel);
        if (mergedSequence != null) {
            super.sendWaveForm(channel, mergedSequence);
            LOGGER.debug("Sent merged waveform to channel {}", channel);
        }
    }
} 