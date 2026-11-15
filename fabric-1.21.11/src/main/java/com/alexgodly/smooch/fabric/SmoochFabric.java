package com.alexgodly.smooch.fabric;

import com.alexgodly.smooch.KissHandler;
import com.alexgodly.smooch.KissPayload;
import com.alexgodly.smooch.Smooch;
import com.alexgodly.smooch.SmoochConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class SmoochFabric implements ModInitializer {
    private static SoundEvent sound(String name) {
        Identifier id = Smooch.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    @Override
    public void onInitialize() {
        SoundEvent kiss = sound("kiss"), sparkle = sound("sparkle"), effects = sound("effects");
        Smooch.KISS = () -> kiss;
        Smooch.SPARKLE = () -> sparkle;
        Smooch.EFFECTS = () -> effects;

        SmoochConfig.init(FabricLoader.getInstance().getConfigDir());

        PayloadTypeRegistry.playC2S().register(KissPayload.TYPE, KissPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(KissPayload.TYPE, (payload, context) ->
                context.server().execute(() -> KissHandler.onKeybind(context.player())));

        ServerTickEvents.END_SERVER_TICK.register(KissHandler::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> KissHandler.forget(handler.getPlayer()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> KissHandler.clear());
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> SmoochConfig.load());
    }
}
