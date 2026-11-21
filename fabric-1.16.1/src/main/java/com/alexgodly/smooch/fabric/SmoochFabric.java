package com.alexgodly.smooch.fabric;

import com.alexgodly.smooch.KissHandler;
import com.alexgodly.smooch.Smooch;
import com.alexgodly.smooch.SmoochConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;

public class SmoochFabric implements ModInitializer {
    private static SoundEvent sound(String name) {
        ResourceLocation id = Smooch.id(name);
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }

    @Override
    public void onInitialize() {
        SoundEvent kiss = sound("kiss"), sparkle = sound("sparkle"), effects = sound("effects");
        Smooch.KISS = () -> kiss;
        Smooch.SPARKLE = () -> sparkle;
        Smooch.EFFECTS = () -> effects;

        SmoochConfig.init(FabricLoader.getInstance().getConfigDirectory().toPath());

        ServerSidePacketRegistry.INSTANCE.register(Smooch.KISS_PACKET, (context, buf) ->
                context.getTaskQueue().execute(() -> {
                    if (context.getPlayer() instanceof ServerPlayer) KissHandler.onKeybind((ServerPlayer) context.getPlayer());
                }));

        ServerTickCallback.EVENT.register(KissHandler::tick);
        ServerStopCallback.EVENT.register(server -> KissHandler.clear());
    }
}
