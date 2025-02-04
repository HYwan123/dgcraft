package com.example.wan_try;

import net.minecraftforge.client.ClientRegistry;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraftforge.client.settings.KeyConflictContext;

public class KeybindHandler {
    public static final String CATEGORY = "key.categories.mymod"; // 你的按键分类
    public static final KeyMapping OPEN_GUI_KEY = new KeyMapping(
            "key.mymod.opengui", // 按键名称
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O, // 默认绑定 "G" 键
            CATEGORY // 分类
    );

    public static final KeyMapping OPEN_QR_KEY = new KeyMapping(
        "key.wan_dglab_test.open_qr",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "key.categories.wan_dglab_test"
    );

    public static void register() {
        ClientRegistry.registerKeyBinding(OPEN_GUI_KEY);
    }
}
