package com.example.wan_try;

import com.example.wan_try.gui.QRCodeScreen;
import com.google.zxing.common.BitMatrix;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;


public class QrCodeHandler {
    public static QRCodeScreen getQrCodeScreen() {
        return qrCodeScreen;
    }

    private static final QRCodeScreen qrCodeScreen = new QRCodeScreen(new TextComponent("扫描二维码连接设备"),null,null);

    public static void handleQRCodeResponse(BitMatrix qrCode) {
        QRCodeScreen screen = QrCodeHandler.getQrCodeScreen();
        screen.setQrCode(qrCode);
        Minecraft.getInstance().setScreen(screen);
    }

    public static void closeScreen(){
        qrCodeScreen.setContext(null);
        qrCodeScreen.setQrCode(null);
    };

    public static void showQrCode(BitMatrix qrCode) {
        qrCodeScreen.setQrCode(qrCode);
        Minecraft.getInstance().setScreen(qrCodeScreen);
    }



    public static void showQrCodeScreen(BitMatrix qrcode) {
        var mc = Minecraft.getInstance();
        QRCodeScreen screen = QrCodeHandler.getQrCodeScreen();
        screen.setQrCode(qrcode);
        screen.setContext(Main.getInstance().getClient().getContext(mc.player.getStringUUID()));
        Minecraft.getInstance().setScreen(screen);
    }

}
