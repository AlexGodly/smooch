package com.alexgodly.smooch.fabric;

import com.alexgodly.smooch.Smooch;
import com.mojang.blaze3d.platform.InputConstants;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.network.FriendlyByteBuf;
import org.lwjgl.glfw.GLFW;

public class SmoochFabricClient implements ClientModInitializer {
    private static FabricKeyBinding kissKey;

    @Override
    public void onInitializeClient() {
        kissKey = FabricKeyBinding.Builder.create(Smooch.id("kiss"), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "smooch").build();
        KeyBindingRegistry.INSTANCE.addCategory("smooch");
        KeyBindingRegistry.INSTANCE.register(kissKey);

        ClientTickCallback.EVENT.register(client -> {
            while (kissKey.consumeClick()) {
                if (client.player != null && ClientSidePacketRegistry.INSTANCE.canServerReceive(Smooch.KISS_PACKET)) {
                    ClientSidePacketRegistry.INSTANCE.sendToServer(Smooch.KISS_PACKET, new FriendlyByteBuf(Unpooled.buffer()));
                }
            }
        });
    }
}
