package com.alexgodly.smooch.neoforge;

import com.alexgodly.smooch.ConfigScreens;
import com.alexgodly.smooch.KissPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

final class SmoochNeoForgeClient {
    static final KeyMapping KISS_KEY = new KeyMapping("key.smooch.kiss", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.smooch");

    private SmoochNeoForgeClient() {}

    static void init(IEventBus modBus, ModContainer container) {
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> event.register(KISS_KEY));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            Minecraft mc = Minecraft.getInstance();
            while (KISS_KEY.consumeClick()) {
                if (mc.player != null && mc.getConnection() != null && mc.getConnection().hasChannel(KissPayload.TYPE)) {
                    PacketDistributor.sendToServer(KissPayload.INSTANCE);
                }
            }
        });
        if (ModList.get().isLoaded("cloth_config")) {
            container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> ConfigScreens.create(parent));
        }
    }
}
