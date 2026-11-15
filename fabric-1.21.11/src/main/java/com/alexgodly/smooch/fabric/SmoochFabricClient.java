package com.alexgodly.smooch.fabric;

import com.alexgodly.smooch.KissPayload;
import com.alexgodly.smooch.Smooch;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class SmoochFabricClient implements ClientModInitializer {
    public static KeyMapping kissKey;

    @Override
    public void onInitializeClient() {
        kissKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.smooch.kiss", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, KeyMapping.Category.register(Smooch.id("main"))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (kissKey.consumeClick()) {
                if (client.player != null && ClientPlayNetworking.canSend(KissPayload.TYPE)) {
                    ClientPlayNetworking.send(KissPayload.INSTANCE);
                }
            }
        });
    }
}
