package com.example.wan_try.gui;

import com.example.wan_try.Main;
import com.example.wan_try.dglab.DGLabClient;
import com.example.wan_try.dglab.api.IDGLabClient;
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
    private float animationProgress = 0f;
    private long lastAnimationTime = 0;
    
    // Theme colors
    private static final int COLOR_BACKGROUND = 0xDD212121;
    private static final int COLOR_PANEL = 0xEE2D2D30;
    private static final int COLOR_ACCENT_BLUE = 0xFF0088FF;
    private static final int COLOR_ACCENT_PURPLE = 0xFFCC44FF;
    private static final int COLOR_TEXT = 0xFFEEEEEE;
    private static final int COLOR_TEXT_SECONDARY = 0xFFAAAAAA;
    private static final int COLOR_SUCCESS = 0xFF44CC44;
    private static final int COLOR_ERROR = 0xFFFF3333;
    private static final int COLOR_WARNING = 0xFFFFAA00;

    public void setQrCode(BitMatrix qrCode) {
        this.qrCode = qrCode;
    }

    public void setContext(List<MinecraftDgLabContext> context) {
        this.context = context;
    }

    private BitMatrix qrCode = null;
    private List<MinecraftDgLabContext> context = null;

    public QRCodeScreen(Component title, BitMatrix qrCode, List<MinecraftDgLabContext> context) {
        super(title);
        this.qrCode = qrCode;
        this.context = context;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - TEXTURE_WIDTH) / 2;
        this.guiTop = (this.height - TEXTURE_HEIGHT) / 2;

        // 添加现代风格关闭按钮
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
        // 渲染半透明深色背景
        fillGradient(poseStack, 0, 0, this.width, this.height, 0xCC101010, 0xCC151515);
        
        if (context == null || context.isEmpty()) {
            // 渲染二维码
            renderQRCode(poseStack);
        } else {
            // 当二维码隐藏时，渲染设备信息
            renderDeviceInfo(poseStack);
        }

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private void renderQRCode(PoseStack poseStack) {
        int qrSize = Math.min(qrCode.getWidth(), qrCode.getHeight());
        int scale = 3;
        int qrLeft = (this.width - qrSize * scale) / 2;
        int qrTop = (this.height - qrSize * scale) / 2;

        // 渲染卡片背景 (圆角矩形效果)
        fillGradient(poseStack, 
            qrLeft - 20, qrTop - 50, 
            qrLeft + qrSize * scale + 20, qrTop + qrSize * scale + 20,
            COLOR_PANEL, 0xEE1A1A1C);
            
        // 渲染边框发光效果
        renderBorder(poseStack, 
            qrLeft - 20, qrTop - 50, 
            qrLeft + qrSize * scale + 20, qrTop + qrSize * scale + 20, 
            COLOR_ACCENT_BLUE, 2);

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
        int titleY = qrTop - 35;
        drawCenteredString(poseStack, this.font, this.title,
                this.width / 2, titleY, COLOR_TEXT);
        
        // 渲染分隔线
        fill(poseStack, this.width/2 - 50, titleY + 15, this.width/2 + 50, titleY + 16, COLOR_ACCENT_BLUE);

        if (Main.getInstance().getClient() != null) {
            DGLabClient client = (DGLabClient) Main.getInstance().getClient();
            // 渲染错误信息
            if (client.getLastException() != null) {
                int errorY = qrTop - 60;
                // 使用圆角矩形显示错误消息
                fillGradient(poseStack,
                        this.width / 2 - 150, errorY,
                        this.width / 2 + 150, errorY + 20,
                        COLOR_ERROR & 0xDDFFFFFF, COLOR_ERROR & 0xAAFFFFFF);
                
                // 使用红色文字显示错误信息
                drawCenteredString(poseStack, this.font,
                        "⚠ " + client.getLastException().getMessage(),
                        this.width / 2, errorY + 6, 0xFFFFFFFF);
            }
        }
    }

    private void renderDeviceInfo(PoseStack poseStack) {
        if (context == null || context.isEmpty()) return;

        // 更新动画进度
        updateAnimation();

        // 居中显示信息面板
        int panelWidth = 240;
        int panelHeight = context.isEmpty() ? 80 : 100 + context.size() * 110;
        int infoX = (this.width - panelWidth) / 2;
        int infoY = (this.height - panelHeight) / 2;

        // 渲染主面板背景 - 使用高级渐变色
        fillGradient(poseStack,
                infoX, infoY,
                infoX + panelWidth, infoY + panelHeight,
                COLOR_PANEL, 0xEE1D1D1D);
                
        // 渲染面板边框发光效果
        renderBorder(poseStack, infoX, infoY, infoX + panelWidth, infoY + panelHeight, COLOR_ACCENT_BLUE, 2);

        // 标题带有图标
        String title = "✧ 设备状态监控 ✧";
        drawCenteredString(poseStack, this.font, title, this.width / 2, infoY + 15, COLOR_ACCENT_BLUE);
        
        // 渲染标题下方的装饰线
        int lineWidth = Math.min(180, (int)(180 * animationProgress));
        fill(poseStack, this.width/2 - lineWidth/2, infoY + 30, this.width/2 + lineWidth/2, infoY + 31, COLOR_ACCENT_PURPLE);

        // 连接状态指示器
        String connectionStatus = context.isEmpty() ?
                "§c◉ 设备未连接" :
                "§a◉ 已连接 " + context.size() + " 台设备";
        drawCenteredString(poseStack, this.font, connectionStatus,
                this.width / 2,
                infoY + 40, COLOR_TEXT);

        if (!context.isEmpty()) {
            int deviceY = infoY + 60;

            for (int i = 0; i < context.size(); i++) {
                DGLabClient.DGLabContext device = this.context.get(i);

                // 设备卡片背景 - 带有动画的平滑出现效果
                int alpha = Math.min(255, (int)(255 * animationProgress));
                int cardColor = (COLOR_PANEL & 0x00FFFFFF) | (alpha << 24);
                int cardColor2 = (0xFF1A1A20 & 0x00FFFFFF) | (alpha << 24);
                
                fillGradient(poseStack,
                        infoX + 10, deviceY,
                        infoX + panelWidth - 10, deviceY + 100,
                        cardColor, cardColor2);
                
                // 设备卡片边框
                renderBorder(poseStack, 
                    infoX + 10, deviceY, 
                    infoX + panelWidth - 10, deviceY + 100, 
                    COLOR_ACCENT_PURPLE & 0x88FFFFFF, 1);

                // 设备标题带有动画的图标
                String deviceIcon = getAnimatedIcon();
                String deviceTitle = "§b" + deviceIcon + " 设备 " + (i + 1);
                drawString(poseStack, this.font, deviceTitle,
                        infoX + 20, deviceY + 12, COLOR_TEXT);

                // 通道标签和数值
                String channelALabel = String.format("§7通道 A:  §f%d§7/§f%d",
                        device.getStrengthA().get(), device.getStrengthALimit().get());
                drawString(poseStack, this.font, channelALabel,
                        infoX + 20, deviceY + 32, COLOR_TEXT);

                String channelBLabel = String.format("§7通道 B:  §f%d§7/§f%d",
                        device.getStrengthB().get(), device.getStrengthBLimit().get());
                drawString(poseStack, this.font, channelBLabel,
                        infoX + 20, deviceY + 62, COLOR_TEXT);

                // 通道进度条背景 - 圆角效果
                renderRoundedRect(poseStack, infoX + 20, deviceY + 42, infoX + panelWidth - 30, deviceY + 52, 0x88000000);
                renderRoundedRect(poseStack, infoX + 20, deviceY + 72, infoX + panelWidth - 30, deviceY + 82, 0x88000000);

                // 通道A进度条 - 带有动画效果
                int strengthALimit = device.getStrengthALimit().get();
                float progressA = strengthALimit > 0 ? 
                    (float) device.getStrengthA().get() / strengthALimit : 0f;
                int progressWidth = (int)((panelWidth - 50) * progressA * animationProgress);
                renderProgressBar(poseStack,
                        infoX + 20, deviceY + 42,
                        infoX + 20 + progressWidth, deviceY + 52,
                        COLOR_ACCENT_BLUE);

                // 通道B进度条 - 带有动画效果
                int strengthBLimit = device.getStrengthBLimit().get();
                float progressB = strengthBLimit > 0 ? 
                    (float) device.getStrengthB().get() / strengthBLimit : 0f;
                progressWidth = (int)((panelWidth - 50) * progressB * animationProgress);
                renderProgressBar(poseStack,
                        infoX + 20, deviceY + 72,
                        infoX + 20 + progressWidth, deviceY + 82,
                        COLOR_ACCENT_PURPLE);

                deviceY += 110;
            }
        }
    }
    
    private void renderBorder(PoseStack poseStack, int x1, int y1, int x2, int y2, int color, int thickness) {
        // Top border
        fill(poseStack, x1, y1, x2, y1 + thickness, color);
        // Bottom border
        fill(poseStack, x1, y2 - thickness, x2, y2, color);
        // Left border
        fill(poseStack, x1, y1 + thickness, x1 + thickness, y2 - thickness, color);
        // Right border
        fill(poseStack, x2 - thickness, y1 + thickness, x2, y2 - thickness, color);
    }
    
    private void renderRoundedRect(PoseStack poseStack, int x1, int y1, int x2, int y2, int color) {
        // Main rectangle
        fill(poseStack, x1 + 2, y1, x2 - 2, y2, color);
        // Top and bottom rectangles
        fill(poseStack, x1 + 1, y1 + 1, x2 - 1, y2 - 1, color);
        // Left and right rectangles
        fill(poseStack, x1, y1 + 2, x2, y2 - 2, color);
    }
    
    private void renderProgressBar(PoseStack poseStack, int x1, int y1, int x2, int y2, int color) {
        // 保证至少有1个像素宽度的进度条
        int barWidth = Math.max(x2 - x1, 1);
        
        // Main rectangle with glow effect
        fillGradient(poseStack, x1, y1, x1 + barWidth, y2, color, color & 0xDDFFFFFF);
        
        // Top highlight for 3D effect
        fill(poseStack, x1, y1, x1 + barWidth, y1 + 1, 0x55FFFFFF);
        
        // Bottom shadow for 3D effect
        fill(poseStack, x1, y2 - 1, x1 + barWidth, y2, color & 0xAA000000);
        
        // 只在进度条有一定宽度时添加动画点
        if (barWidth > 5) {
            // Glowing dots for animation effect
            for (int i = 0; i < 3; i++) {
                int dotPos = x1 + ((barWidth * i / 2) + (int)(barWidth * animationProgress) % barWidth);
                if (dotPos < x2) {
                    fill(poseStack, dotPos, y1 + 2, dotPos + 2, y2 - 2, 0x99FFFFFF);
                }
            }
        }
    }
    
    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAnimationTime > 16) { // ~60 FPS
            animationProgress = Math.min(1.0f, animationProgress + 0.05f);
            lastAnimationTime = currentTime;
        }
    }
    
    private String getAnimatedIcon() {
        long time = System.currentTimeMillis() % 1500;
        if (time < 500) return "⚡";
        else if (time < 1000) return "✦";
        else return "⚙";
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
                    // 重置动画进度用于平滑过渡
                    animationProgress = 0f;
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