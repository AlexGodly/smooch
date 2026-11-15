package com.alexgodly.smooch.forge;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.lwjgl.glfw.GLFW;

final class SmoochForgeClient {
    static final KeyMapping KISS_KEY = new KeyMapping("key.smooch.kiss", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.smooch");

    private SmoochForgeClient() {}

    static void init(IEventBus modBus) {
        modBus.addListener((FMLClientSetupEvent event) -> ClientRegistry.registerKeyBinding(KISS_KEY));
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            while (KISS_KEY.consumeClick()) {
                if (mc.player != null && mc.getConnection() != null && Network.serverHasMod(mc.getConnection().getConnection())) {
                    Network.CHANNEL.sendToServer(new Network.Kiss());
                }
            }
        });
    }
}
