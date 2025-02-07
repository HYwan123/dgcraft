package com.example.wan_try.network;

import com.example.wan_try.CommonConfigHandler;
import com.example.wan_try.Main;
import com.example.wan_try.QrCodeHandler;
import com.example.wan_try.dglab.DGLabClient;
import com.example.wan_try.dglab.MinecraftDgLabContext;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;

public class NetworkHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("wan_dglab_test", "main"),
        () -> PROTOCOL_VERSION,
        NetworkHandler::judgeVersion,
            (x)->true

    );

    public static boolean judgeVersion(String x){
        return CommonConfigHandler.client.get() || PROTOCOL_VERSION.equals(x);
    }




    public static void init() {
        int id = 0;
        INSTANCE.registerMessage(
            id++,
            QRCodeRequestPacket.class,
            QRCodeRequestPacket::encode,
            QRCodeRequestPacket::decode,
            QRCodeRequestPacket::handle
        );
        INSTANCE.registerMessage(
            id++,
            QRCodeResponsePacket.class,
            QRCodeResponsePacket::encode,
            QRCodeResponsePacket::decode,
            QRCodeResponsePacket::handle
        );
        INSTANCE.registerMessage(
                id++,
                DgLabDataUpdatePacket.class,
                DgLabDataUpdatePacket::encode,
                DgLabDataUpdatePacket::decode,
                DgLabDataUpdatePacket::handle
        );
        INSTANCE.registerMessage(
                id++,
                ContextRequestPacket.class,
                ContextRequestPacket::encode,
                ContextRequestPacket::decode,
                ContextRequestPacket::handle
        );


    }

    public static void handleQRCodeRequest(String playerUUID, ServerPlayer sender) {
        try {
            BitMatrix qrCode = Main.getInstance().getClient().genQrCode(playerUUID);
            INSTANCE.sendTo(
                new QRCodeResponsePacket(qrCode),
                sender.connection.getConnection(),
                NetworkDirection.PLAY_TO_CLIENT
            );
        } catch (WriterException e) {
            LOGGER.error("Failed to generate QR code for player {}", playerUUID, e);
        }
    }

    public static void handleQRCodeResponse(BitMatrix qrCode) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ()->QrCodeHandler.handleQRCodeResponse(qrCode));
    }

    public static void handleDgLabDataUpdate(DgLabDataUpdatePacket msg) {
        var ctx = new MinecraftDgLabContext(null,null,null);

        QrCodeHandler.getQrCodeScreen().setContext(Arrays.stream(new MinecraftDgLabContext[] {ctx}).toList());
    }
}