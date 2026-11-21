package com.alexgodly.smooch.client;

import com.alexgodly.smooch.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public final class SmoochClient {
    private static final KeyBinding KISS_KEY = new KeyBinding("key.smooch.kiss", Keyboard.KEY_K, "key.categories.smooch");

    private SmoochClient() {}

    public static void init() {
        ClientRegistry.registerKeyBinding(KISS_KEY);
        MinecraftForge.EVENT_BUS.register(new SmoochClient());
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        while (KISS_KEY.isPressed()) {
            if (mc.player != null && mc.getConnection() != null) Network.CHANNEL.sendToServer(new Network.Kiss());
        }
    }
}
