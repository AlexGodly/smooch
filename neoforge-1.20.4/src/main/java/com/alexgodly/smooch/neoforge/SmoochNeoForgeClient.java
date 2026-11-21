package com.alexgodly.smooch.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

final class SmoochNeoForgeClient {
    static final KeyMapping KISS_KEY = new KeyMapping("key.smooch.kiss", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.smooch");

    private SmoochNeoForgeClient() {}

    static void init(IEventBus modBus) {
        modBus.addListener((RegisterKeyMappingsEvent event) -> event.register(KISS_KEY));
        NeoForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            while (KISS_KEY.consumeClick()) {
                if (mc.player != null && mc.getConnection() != null && Network.serverHasMod()) Network.sendKiss();
            }
        });
        if (ModList.get().isLoaded("cloth_config")) {
            try {
                Class.forName("com.alexgodly.smooch.neoforge.ClothScreen").getMethod("register").invoke(null);
            } catch (ReflectiveOperationException ignored) {
                // built without Cloth Config support
            }
        }
    }
}
