package com.example.wan_try;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfigHandler {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CLIENT_CONFIG;

    // IP地址配置
    public static final ForgeConfigSpec.ConfigValue<String> SERVER_IP = BUILDER
            .comment("服务器IP地址")
            .define("ip", "192.168.2.106");

    // 端口配置
    public static final ForgeConfigSpec.IntValue SERVER_PORT = BUILDER
            .comment("服务器端口")
            .defineInRange("port", 9999, 1, 65535);
    public static final ForgeConfigSpec.ConfigValue<String> SERVER_Reflect_IP = BUILDER
            .comment("映射服务器IP地址")
            .define("reflect_ip", "hywan.tpddns.cn");

    // 端口配置
    public static final ForgeConfigSpec.IntValue SERVER_Reflect_PORT = BUILDER
            .comment("映射服务器端口")
            .defineInRange("reflect_port", 9999, 1, 65535);


    // 强度配置
    public static final ForgeConfigSpec.DoubleValue INTENSITY = BUILDER
            .comment("强度设置 (0.0-1.0)")
            .defineInRange("intensity", 0.5, 0.0, 1.0);

    // 受伤相关配置
    public static final ForgeConfigSpec.DoubleValue DAMAGE_INTENSITY_INCREMENT = BUILDER
            .comment("每次受伤增加的强度 (0.0-200.0)")
            .defineInRange("damage_intensity_increment", 5, 0.0, 200.0);

    public static final ForgeConfigSpec.BooleanValue ENABLE_DAMAGE_WAVEFORM = BUILDER
            .comment("是否启用受伤默认波形")
            .define("enable_damage_waveform", true);

    public static final ForgeConfigSpec.IntValue DAMAGE_WAVEFORM_DURATION = BUILDER
            .comment("受伤后波形持续时间（以毫秒为单位）")
            .defineInRange("damage_waveform_duration", 1000, 0, 10000);

    
    static {
        BUILDER.push("Client Settings");
        // 配置已在上面定义，这里不需要重复定义
        BUILDER.pop();
        CLIENT_CONFIG = BUILDER.build();
    }
}
