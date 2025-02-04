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
    private long lastUpdate = 0;
    private static final long UPDATE_INTERVAL = 500; // 每500ms更新一次
    private boolean wasConnected = false;

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
        if (context !=null && context.isEmpty()) {
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
        if (context != null && context.isEmpty()) return;

        // 居中显示信息面板
        int panelWidth = 200;
        int panelHeight = context.isEmpty() ? 60 : 80 + context.size() * 100;
        int infoX = (this.width - panelWidth) / 2;
        int infoY = (this.height - panelHeight) / 2;
        int lineHeight = 20;

        // 渲染主面板背景 - 使用渐变色
        fillGradient(poseStack, 
            infoX, infoY,
            infoX + panelWidth, infoY + panelHeight,
            0xCC2C2F33, 0xCC1F1F23);

        // 渲染边框
        fill(poseStack, infoX, infoY, infoX + panelWidth, infoY + 2, 0xFF00FFFF); // 上边框
        fill(poseStack, infoX, infoY + panelHeight - 2, infoX + panelWidth, infoY + panelHeight, 0xFF00FFFF); // 下边框
        fill(poseStack, infoX, infoY, infoX + 2, infoY + panelHeight, 0xFF00FFFF); // 左边框
        fill(poseStack, infoX + panelWidth - 2, infoY, infoX + panelWidth, infoY + panelHeight, 0xFF00FFFF); // 右边框

        // 标题
        String title = "设备状态监控";
        int titleWidth = this.font.width(title);
        drawString(poseStack, this.font, title, infoX + (panelWidth - titleWidth) / 2, infoY + 10, 0xFF00FFFF);
        
        // 连接状态指示器
        String connectionStatus = context.isEmpty() ?
            "§c⬤ 设备未连接" :
            "§a⬤ 已连接 " + context.size() + " 台设备";
        drawString(poseStack, this.font, connectionStatus, 
            infoX + (panelWidth - this.font.width(connectionStatus)) / 2, 
            infoY + 30, 0xFFFFFF);

        if (!context.isEmpty()) {
            int deviceY = infoY + 60;
            
            for (int i = 0; i < context.size(); i++) {
                DGLabClient.DGLabContext device = this.context.get(i);
                
                // 设备卡片背景
                fillGradient(poseStack,
                    infoX + 10, deviceY,
                    infoX + panelWidth - 10, deviceY + 80,
                    0x88000000, 0x88202020);
                
                // 设备标题
                String deviceTitle = "§b⚡ 设备 " + (i + 1);
                drawString(poseStack, this.font, deviceTitle, 
                    infoX + 20, deviceY + 10, 0xFFFFFF);

                // 通道进度条背景
                fill(poseStack, infoX + 20, deviceY + 35, infoX + panelWidth - 20, deviceY + 45, 0x88000000);
                fill(poseStack, infoX + 20, deviceY + 60, infoX + panelWidth - 20, deviceY + 70, 0x88000000);

                // 通道A进度条
                float progressA = (float)device.getStrengthA() / device.getStrengthALimit();
                fill(poseStack, 
                    infoX + 20, deviceY + 35,
                    infoX + 20 + (int)((panelWidth - 40) * progressA), deviceY + 45,
                    0xFF00FFFF);

                // 通道B进度条
                float progressB = (float)device.getStrengthB() / device.getStrengthBLimit();
                fill(poseStack,
                    infoX + 20, deviceY + 60,
                    infoX + 20 + (int)((panelWidth - 40) * progressB), deviceY + 70,
                    0xFFFF00FF);

                // 通道标签和数值
                String channelALabel = String.format("§7通道 A: §f%d§7/§f%d", 
                    device.getStrengthA(), device.getStrengthALimit());
                drawString(poseStack, this.font, channelALabel, 
                    infoX + 20, deviceY + 25, 0xFFFFFF);

                String channelBLabel = String.format("§7通道 B: §f%d§7/§f%d",
                    device.getStrengthB(), device.getStrengthBLimit());
                drawString(poseStack, this.font, channelBLabel,
                    infoX + 20, deviceY + 50, 0xFFFFFF);

                deviceY += 90;
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        
        long currentTime = System.currentTimeMillis();
        // 只在需要时更新界面
        if (currentTime - lastUpdate > UPDATE_INTERVAL) {
            // 检查设备连接状态
            IDGLabClient<MinecraftDgLabContext> client = Main.getInstance().getClient();
            if (client != null && Minecraft.getInstance().player != null) {
                String playerUUID = Minecraft.getInstance().player.getStringUUID();
                List<MinecraftDgLabContext> contexts = client.getContext(playerUUID);
                
                boolean isConnected = !contexts.isEmpty();
                
                // 如果刚刚连接上（状态从未连接变为已连接）
                if (isConnected && !wasConnected) {
                    showQRCode = false;
                }
                
                // 更新连接状态
                wasConnected = isConnected;
            }
            lastUpdate = currentTime;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false; // 不暂停游戏
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true; // 允许使用ESC关闭界面
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        // 允许使用按键绑定
        return false;
    }
}