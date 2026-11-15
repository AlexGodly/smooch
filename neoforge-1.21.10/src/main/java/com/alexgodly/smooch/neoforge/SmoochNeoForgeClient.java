package com.alexgodly.smooch.neoforge;

import com.alexgodly.smooch.ConfigScreens;
import com.alexgodly.smooch.KissPayload;
import com.alexgodly.smooch.Smooch;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Smooch.MOD_ID, dist = Dist.CLIENT)
public class SmoochNeoForgeClient {
    static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Smooch.id("main"));
    static final KeyMapping KISS_KEY = new KeyMapping("key.smooch.kiss", InputConstants.Type.KEYSYM, 75 /* K */, CATEGORY);

    public SmoochNeoForgeClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> {
            event.registerCategory(CATEGORY);
            event.register(KISS_KEY);
        });
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            Minecraft mc = Minecraft.getInstance();
            while (KISS_KEY.consumeClick()) {
                if (mc.player != null && mc.getConnection() != null && mc.getConnection().hasChannel(KissPayload.TYPE)) {
                    ClientPacketDistributor.sendToServer(KissPayload.INSTANCE);
                }
            }
        });
        if (ModList.get().isLoaded("cloth_config")) {
            container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> ConfigScreens.create(parent));
        }
    }
}
