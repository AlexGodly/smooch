package com.alexgodly.smooch.forge;

import net.minecraft.client.util.InputMappings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.lwjgl.glfw.GLFW;

final class SmoochForgeClient {
    static final KeyBinding KISS_KEY = new KeyBinding("key.smooch.kiss", InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.smooch");

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
