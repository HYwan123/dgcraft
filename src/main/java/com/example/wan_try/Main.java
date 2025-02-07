package com.example.wan_try;

import com.example.wan_try.dglab.*;
import com.example.wan_try.dglab.api.IDGLabClient;
import com.google.zxing.common.BitMatrix;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.example.wan_try.network.NetworkHandler;
import com.example.wan_try.network.QRCodeRequestPacket;

import java.net.InetSocketAddress;

@Mod("me_hywan_dgcraft")
@Mod.EventBusSubscriber
public class Main {
    // 常量定义
    private static final Logger LOGGER = LogManager.getLogger("WanDGLabTest");
    private static final int MIN_FREQUENCY = 20;
    private static final int MAX_FREQUENCY = 240;
    private static final int MAX_STRENGTH = 100;
    private static final float DAMAGE_REDUCTION = 0.5f;

    // 实例变量
    private static Main instance;
    private DGLabClient<MinecraftDgLabContext> client;
    private final FeedbackGenerator feedbackGenerator;
    private MinecraftServer server;

    private final MinecraftDgLabContext LocalContext = null;



    public Main() {
        instance = this;
        feedbackGenerator = new FeedbackGenerator();
        initializeMod();
    }

    // 初始化方法
    private void initializeMod() {
        LOGGER.info("Initializing WanDGLabTest mod");
        registerConfigs();
        registerEventListeners();
        NetworkHandler.init(); // 初始化网络处理
    }

    private void registerConfigs() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CommonConfigHandler.CLIENT_CONFIG);
        LOGGER.debug("Registered client config");
    }

    private void registerEventListeners() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
//        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> true));
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        // 客户端版本字符串 - 由于这是客户端mod，实际上不会被发送
                        () -> "anything. i don't care",

                        // 远程版本验证逻辑
                        // remoteversionstring: 服务器版本字符串
                        // networkbool: 网络协议兼容标志
                        (remoteversionstring, networkbool) -> networkbool // 接受任何服务器版本
                )
        );
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.debug("Registered event listeners");
    }

    // 事件处理方法
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {

        Player player = event.getPlayer();
        Level level = player.level;
        LOGGER.info("Player {} logging in on side {}", 
            player.getDisplayName().getString(), 
            level.isClientSide() ? "CLIENT" : "SERVER"
        );
        handleQRCodeGeneration(player, level);
        sendWelcomeMessage(player, level);
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {

            LOGGER.info("Player {} died, sending death feedback", player.getDisplayName().getString());
            if(CommonConfigHandler.pasento.get()){
                feedbackGenerator.sendDeathFeedback(player, client);
            }
            else{
                feedbackGenerator.sendDeathFeedbackWan(player, client);
            }

        }
    }



    @SubscribeEvent
    public void onPlayerExit(PlayerEvent.PlayerLoggedOutEvent event){
        if (event.getEntity() instanceof Player player) {
            if(player.isLocalPlayer()){
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ()-> QrCodeHandler::closeScreen);
            }
        }
//                if(event.getEntity() instanceof Player player) {
//
//            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
//                qrCodeScreen.setContext(null);
//                qrCodeScreen.setQrCode(null);
//            });
//            LOGGER.info(FMLEnvironment.dist.isClient());
//            LOGGER.info(Minecraft.getInstance().level.isClientSide());
//            client.getContext(player.getStringUUID()).forEach(DGLabClient.DGLabContext::disconnect);
//        }
    }

    @SubscribeEvent
    public void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            float originalDamage = event.getAmount();
            float reducedDamage = originalDamage * DAMAGE_REDUCTION;
            
            LOGGER.info("Player {} took {} damage (reduced from {})", 
                player.getDisplayName().getString(),
                reducedDamage,
                originalDamage
            );
            if(CommonConfigHandler.pasento.get()){
                feedbackGenerator.sendHurtFeedback(player, client, originalDamage);
            }
            else{
                feedbackGenerator.sendHurtFeedbackWan(player, client, originalDamage);
            }

            event.setAmount(reducedDamage);
            player.displayClientMessage(new TextComponent("你受到了 " + reducedDamage + " 点伤害！"), true);
        }
    }

    // 设置方法
    private void setup(final FMLCommonSetupEvent event) {



    }


    private void doClientStuff(final FMLClientSetupEvent event) {
        LOGGER.debug("Registering keybindings");
        ClientRegistry.registerKeyBinding(KeybindHandler.OPEN_QR_KEY);
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        this.server = event.getServer();
        initializeDGLabClient();
    }


    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) throws InterruptedException {
        stopDGLabClient();
        this.server = null;
    }


    // 辅助方法
    public void initializeDGLabClient() {
        LOGGER.info("Setting up DGLab client connection to {}:{}", 
            CommonConfigHandler.SERVER_IP.get(),
            CommonConfigHandler.SERVER_PORT.get()
        );
        
        try {
            client = new DGLabClient<MinecraftDgLabContext>(
                new InetSocketAddress(CommonConfigHandler.SERVER_IP.get(), CommonConfigHandler.SERVER_PORT.get()),
                new InetSocketAddress(CommonConfigHandler.SERVER_Reflect_IP.get(), CommonConfigHandler.SERVER_Reflect_PORT.get()),
                    new MinecraftDgLabContextFactory()
            );
            client.start();
            LOGGER.info("Successfully started DGLab client");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize DGLab client", e);
        }
    }

    private void stopDGLabClient() throws InterruptedException {
        client.stop();
    }

    private void handleQRCodeGeneration(Player player, Level level) {
        try {
            if (level.isClientSide()) {
                if (Minecraft.getInstance().hasSingleplayerServer() || 
                    player.hasPermissions(2)) { // 检查是否是房主
                    // 本地生成二维码
                    BitMatrix qrcode = client.genQrCode(player.getStringUUID());
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

    private void showQRCode(BitMatrix qrcode) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () ->()-> QrCodeHandler.showQrCode(qrcode));
    }

    private void sendWelcomeMessage(Player player, Level level) {
        player.sendMessage(
            new TextComponent("你好喵~" + player.getDisplayName().getString() +
                "来自" + (level.isClientSide() ? "客户端" : "服务端")),
            Util.NIL_UUID
        );
    }

    // Getter方法
    public static Main getInstance() {
        return instance;
    }

    public IDGLabClient<MinecraftDgLabContext> getClient() {
        return client;
    }

    public MinecraftServer getServer() {
        return server;
    }
}
