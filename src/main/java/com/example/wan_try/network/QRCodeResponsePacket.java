package com.example.wan_try.network;

import com.google.zxing.common.BitMatrix;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class QRCodeResponsePacket {
    private final int width;
    private final int height;
    private final boolean[] data;

    public QRCodeResponsePacket(BitMatrix qrCode) {
        this.width = qrCode.getWidth();
        this.height = qrCode.getHeight();
        this.data = new boolean[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                data[y * width + x] = qrCode.get(x, y);
            }
        }
    }

    private QRCodeResponsePacket(int width, int height, boolean[] data) {
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public static void encode(QRCodeResponsePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.width);
        buf.writeInt(msg.height);
        for (boolean b : msg.data) {
            buf.writeBoolean(b);
        }
    }

    public static QRCodeResponsePacket decode(FriendlyByteBuf buf) {
        int width = buf.readInt();
        int height = buf.readInt();
        boolean[] data = new boolean[width * height];
        for (int i = 0; i < data.length; i++) {
            data[i] = buf.readBoolean();
        }
        return new QRCodeResponsePacket(width, height, data);
    }

    public BitMatrix toBitMatrix() {
        BitMatrix matrix = new BitMatrix(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (data[y * width + x]) {
                    matrix.set(x, y);
                }
            }
        }
        return matrix;
    }

    public static void handle(QRCodeResponsePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            NetworkHandler.handleQRCodeResponse(msg.toBitMatrix());
        });
        ctx.get().setPacketHandled(true);
    }
} 