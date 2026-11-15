package com.alexgodly.smooch;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Smooch implements ModInitializer {
    public static final String MOD_ID = "smooch";
    public static final Logger LOGGER = LoggerFactory.getLogger("Smooch");

    public static final SoundEvent KISS = sound("kiss");
    public static final SoundEvent SPARKLE = sound("sparkle");
    public static final SoundEvent EFFECTS = sound("effects");

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static SoundEvent sound(String name) {
        ResourceLocation id = id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    @Override
    public void onInitialize() {
        SmoochConfig.load();

        PayloadTypeRegistry.playC2S().register(KissPayload.TYPE, KissPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(KissPayload.TYPE, (payload, context) ->
                context.server().execute(() -> KissHandler.onKeybind(context.player())));

        ServerTickEvents.END_SERVER_TICK.register(KissHandler::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> KissHandler.forget(handler.getPlayer()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> KissHandler.clear());
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> SmoochConfig.load());
    }
}
