package com.example.wan_try.gui;

import com.example.wan_try.Main;
import com.example.wan_try.dglab.DGLabClient;
import com.example.wan_try.dglab.IDGLabClient;
import com.example.wan_try.dglab.MinecraftDgLabContext;
import com.google.zxing.common.BitMatrix;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class QRCodeScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger(QRCodeScreen.class);
    private int guiLeft;
    private int guiTop;
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private boolean showQRCode = true;

    public void setQrCode(BitMatrix qrCode) {
        this.qrCode = qrCode;
    }

    public void setContext(List<MinecraftDgLabContext> context) {
        this.context = context;
    }

    private  BitMatrix qrCode = null;
    private List<MinecraftDgLabContext> context = null;

    public QRCodeScreen(Component title, BitMatrix qrCode,List<MinecraftDgLabContext> context) {
        super(title);
        this.qrCode = qrCode;
        this.context = context;

        // 在构造函数中检查设备连接状态
//        IDGLabClient client = Main.getInstance().getClient();
//        if (client != null && Minecraft.getInstance().player != null) {
//            String playerUUID = Minecraft.getInstance().player.getStringUUID();
//            List<DGLabClient.DGLabContext> contexts = client.getContext(playerUUID);
//            // 如果已经有设备连接，直接隐藏二维码
//            if (!contexts.isEmpty()) {
//                this.showQRCode = false;
//                this.wasConnected = true;
//                LOGGER.info("Device already connected, hiding QR code");
//            }
//        }
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - TEXTURE_WIDTH) / 2;
        this.guiTop = (this.height - TEXTURE_HEIGHT) / 2;

        // 添加关闭按钮
        Button closeButton = new Button(
            this.width / 2 - BUTTON_WIDTH / 2,
            this.guiTop + TEXTURE_HEIGHT + 10,
            BUTTON_WIDTH,
            BUTTON_HEIGHT,
            new TranslatableComponent("gui.done"),
            button -> this.onClose()
        );
        closeButton.active = true;
        this.addRenderableWidget(closeButton);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (showQRCode) {
            // 只渲染半透明背景
            this.renderBackground(poseStack);

            // 渲染二维码
            int qrSize = Math.min(qrCode.getWidth(), qrCode.getHeight());
            int scale = 3;
            int qrLeft = (this.width - qrSize * scale) / 2;  // 居中显示
            int qrTop = (this.height - qrSize * scale) / 2;

            // 渲染二维码背景（白色）
            fill(poseStack, qrLeft - 5, qrTop - 5,
                 qrLeft + qrSize * scale + 5, qrTop + qrSize * scale + 5,
                 0xFFFFFFFF);

            // 渲染二维码（黑色）
            for (int y = 0; y < qrSize; y++) {
                for (int x = 0; x < qrSize; x++) {
                    if (qrCode.get(x, y)) {
                        fill(poseStack,
                             qrLeft + x * scale,
                             qrTop + y * scale,
                             qrLeft + x * scale + scale,
                             qrTop + y * scale + scale,
                             0xFF000000);
                    }
                }
            }

            // 渲染标题
            drawCenteredString(poseStack, this.font, this.title,
                this.width / 2, qrTop - 20, 0xFFFFFF);
        } else {
            // 当二维码隐藏时，渲染设备信息
            renderDeviceInfo(poseStack);
        }

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private void renderDeviceInfo(PoseStack poseStack) {
        if (showQRCode) return;  // 扫描二维码时不显示设备信息

//        IDGLabClient client = Main.getInstance().getClient();
//        String playerUUID = Minecraft.getInstance().player.getStringUUID();
//        List<DGLabClient.DGLabContext> contexts = client.getContext(playerUUID);

        // 居中显示信息面板
        int infoX = this.width / 2 - 60;
        int infoY = this.height / 2 - 75;
        int lineHeight = 15;

        // 渲染信息面板背景
        fill(poseStack, infoX - 5, infoY - 5,
             infoX + 120, infoY + 150,
             0x88000000);

        // 显示连接状态
        String connectionStatus = context.isEmpty() ?
            "§c● 未连接设备" :
            "§a● 已连接 " + context.size() + " 个设备";
        drawString(poseStack, this.font, connectionStatus, infoX, infoY, 0xFFFFFF);

        // 如果有连接的设备，显示通道信息
        if (context != null && !context.isEmpty()) {
            for (int i = 0; i < context.size(); i++) {
                DGLabClient.DGLabContext context = this.context.get(i);
                infoY += lineHeight * 2;

                // 设备标识
                drawString(poseStack, this.font,
                    "§l⚡ 设备 " + (i + 1),
                    infoX, infoY, 0xFFFFFF);

                // A通道信息
                infoY += lineHeight;
                String channelAInfo = String.format("§7▸ 通道A: §f%d§7/§f%d",
                    context.getStrengthA(),
                    context.getStrengthALimit());
                drawString(poseStack, this.font, channelAInfo, infoX, infoY, 0xFFFFFF);

                // B通道信息
                infoY += lineHeight;
                String channelBInfo = String.format("§7▸ 通道B: §f%d§7/§f%d",
                    context.getStrengthB(),
                    context.getStrengthBLimit());
                drawString(poseStack, this.font, channelBInfo, infoX, infoY, 0xFFFFFF);

                // 添加分隔线
                infoY += lineHeight;
                fill(poseStack, infoX, infoY, infoX + 110, infoY + 1, 0x44FFFFFF);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        // 检查设备连接状态
        if ( Minecraft.getInstance().player != null && context != null) {
            List<MinecraftDgLabContext> contexts = context;

            boolean isConnected = !contexts.isEmpty();

            // 如果刚刚连接上（状态从未连接变为已连接）
            if (isConnected) {
                showQRCode = false;
            }else {
                showQRCode = true;
            }
        }

        // 刷新界面
        if (this.minecraft != null) {
            this.minecraft.setScreen(this);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}