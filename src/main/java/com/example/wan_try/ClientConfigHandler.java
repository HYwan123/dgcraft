package com.example.wan_try;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfigHandler {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CLIENT_CONFIG;

    // IP地址配置
    public static final ForgeConfigSpec.ConfigValue<String> SERVER_IP = BUILDER
            .comment("服务器IP地址")
            .define("ip", getLocalIpAddress());

    private static String getLocalIpAddress() {
        try {
            // Get all network interfaces
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();
                String interfaceName = iface.getDisplayName().toLowerCase();
                
                // Check if interface is Ethernet or WLAN adapter and has default gateway
                if ((interfaceName.contains("ethernet") || interfaceName.contains("wireless") || 
                    interfaceName.contains("wi-fi") || interfaceName.contains("wlan")) && !iface.isLoopback() && iface.isUp()) {
                    
                    // Get all IP addresses for this interface
                    java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        java.net.InetAddress addr = addresses.nextElement();
                        String ipAddress = addr.getHostAddress();
                        
                        // Check if interface has a default gateway by trying to reach a public DNS
                        try {
                            java.net.Socket socket = new java.net.Socket();
                            socket.bind(new java.net.InetSocketAddress(addr, 0));
                            socket.connect(new java.net.InetSocketAddress("8.8.8.8", 53), 1000);
                            socket.close();
                            
                            if (!addr.isLoopbackAddress() && addr instanceof java.net.Inet4Address && 
                                ipAddress.startsWith("192.")) {
                                return ipAddress;
                            }
                        } catch (Exception e) {
                            continue; // Skip interfaces without default gateway
                        }
                    }
                }
            }
            
            // If no 192.* address found on preferred interfaces, try any interface
            interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();
                java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress addr = addresses.nextElement();
                    String ipAddress = addr.getHostAddress();
                    if (!addr.isLoopbackAddress() && addr instanceof java.net.Inet4Address && 
                        ipAddress.startsWith("192.")) {
                        return ipAddress;
                    }
                }
            }
            
            // If still no address found, return localhost
            return "127.0.0.1";
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    // 端口配置
    public static final ForgeConfigSpec.IntValue SERVER_PORT = BUILDER
            .comment("服务器端口")
            .defineInRange("port", 9999, 1, 65535);
    public static final ForgeConfigSpec.ConfigValue<String> SERVER_Reflect_IP = BUILDER
            .comment("映射服务器IP地址")
            .define("reflect_ip", getLocalIpAddress());

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

    public static final ForgeConfigSpec.BooleanValue pasento = BUILDER
            .comment("是否启用百分比计算")
            .define("%hunt_damage", false);

    public static final ForgeConfigSpec.BooleanValue client = BUILDER
            .comment("是否启用纯客户端")
            .define("isClientOnly", true);

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
