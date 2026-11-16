package com.alexgodly.smooch.forge;

import com.alexgodly.smooch.Smooch;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

final class SmoochForgeClient {
    static final KeyMapping KISS_KEY = new KeyMapping("key.smooch.kiss", InputConstants.Type.KEYBOARD, InputConstants.KEY_K, KeyMapping.Category.register(Smooch.id("main")));

    private SmoochForgeClient() {}

    static void init(FMLJavaModLoadingContext context) {
        RegisterKeyMappingsEvent.BUS.addListener(event -> event.register(KISS_KEY));
        TickEvent.ClientTickEvent.Post.BUS.addListener(event -> onTick());
        ConfigHook.register();
    }

    static void onTick() {
        Minecraft mc = Minecraft.getInstance();
        while (KISS_KEY.consumeClick()) {
            if (mc.player != null && mc.getConnection() != null && Network.serverHasMod(mc.getConnection().getConnection())) {
                Network.sendKiss();
            }
        }
    }
}
