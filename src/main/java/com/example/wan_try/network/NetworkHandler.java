package com.example.wan_try.network;

import com.example.wan_try.Main;
import com.example.wan_try.dglab.MinecraftDgLabContext;
import com.example.wan_try.gui.QRCodeScreen;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("wan_dglab_test", "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

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
                MinecraftDgLabContext.class,
                MinecraftDgLabContext::encode,
                MinecraftDgLabContext::decode,
                MinecraftDgLabContext::handle
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
        QRCodeScreen screen = Main.getInstance().getQrCodeScreen();

        screen.setQrCode(qrCode);
        Minecraft.getInstance().setScreen(screen);
    }
} 