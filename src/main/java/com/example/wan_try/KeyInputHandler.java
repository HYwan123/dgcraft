package com.example.wan_try;

import com.example.wan_try.dglab.DGLabClient;
import com.example.wan_try.gui.QRCodeScreen;
import com.example.wan_try.network.NetworkHandler;
import com.example.wan_try.network.QRCodeRequestPacket;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class KeyInputHandler {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Logger LOGGER = LogManager.getLogger("WanDGLabTest-keyinputhandler");
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (KeybindHandler.OPEN_QR_KEY.isDown()) {
                if (mc.screen == null && mc.player != null) {
                    handleQRCodeGeneration(mc.player,mc.level);

//                    try {
//                        DGLabClient client = Main.getInstance().getClient();
//                        if (client != null) {
//                            mc.setScreen(new QRCodeScreen(
//                                new TranslatableComponent("gui.wan_dglab_test.qr_title"),
//                                client.genQrCode(mc.player.getStringUUID())
//                            ));
//                        }
//                    } catch (WriterException e) {
//                        mc.player.displayClientMessage(
//                            new TranslatableComponent("message.wan_dglab_test.qr_error"),
//                            true
//                        );
//                        e.printStackTrace();
//                    }
                }
            }
        }
    }
    private static void handleQRCodeGeneration(Player player, Level level) {
        try {
            if (level.isClientSide()) {
                if (ClientConfigHandler.client.get()) { // 检查是否是房主
                    // 本地生成二维码
                    if(Main.getInstance().getClient() == null) Main.getInstance().initializeDGLabClient();
                    BitMatrix qrcode = Main.getInstance().getClient().genQrCode(player.getStringUUID());
                    showQRCode(qrcode);
                } else {
                    // 向服务器请求二维码
                    NetworkHandler.INSTANCE.sendToServer(
                            new QRCodeRequestPacket(player.getStringUUID())
                    );
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to handle QR code generation for player {}",
                    player.getDisplayName().getString(), e);
        }
    }

    private static void showQRCode(BitMatrix qrcode) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->QrCodeHandler.showQrCodeScreen(qrcode));
        LOGGER.info("Opened QR code screen");
    }


}
