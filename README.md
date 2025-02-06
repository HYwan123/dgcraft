# WAN DGLab Test Mod

这是一个用于 Minecraft 1.18.2 的 DGLab 设备交互 Mod。

## 功能特性

- 支持 DGLab 设备连接和控制
- 提供二维码扫描连接界面
- 受伤反馈系统
- 可配置的强度和波形设置

## 配置说明

配置文件位置：`.minecraft/config/wan_dglab_test-client.toml`

### 服务器设置
- `ip` - 服务器 IP 地址
- `port` - 服务器端口 (1-65535)
- `reflect_ip` - 映射服务器 IP 地址
- `reflect_port` - 映射服务器端口 (1-65535)

### 强度设置
- `intensity` - 基础强度设置 (0.0-1.0)
- `damage_intensity_increment` - 每点伤害增加的强度值 (0.0-200.0)
  - 例如：设置为 3.0 时，受到 1 点伤害会增加 3 点强度
- `damage_waveform_duration` - 受伤后波形持续时间（毫秒）(0-10000)

### 功能开关
- `enable_damage_waveform` - 是否启用受伤默认波形
- `%hunt_damage` - 是否启用百分比计算
- `isClientOnly` - 是否启用纯客户端模式

## 快捷键

- `R` - 打开二维码连接界面（可在游戏内设置中修改）

## 使用方法

1. 安装 Mod 后启动游戏
2. 按下 `R` 键打开二维码界面
3. 使用 DGLab App 扫描二维码进行连接
4. 连接成功后，游戏内受伤会触发设备反馈

## 依赖要求

- Minecraft 1.18.2
- Forge 1.18.2-40.2.0 或更高版本
- Java 17 或更高版本

## 第三方库

- Java-WebSocket 1.6.0
- ZXing Core 3.5.1
- ZXing JavaSE 3.5.1

## 许可证

[添加许可证信息]
