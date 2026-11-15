package com.alexgodly.smooch.neoforge;

import com.alexgodly.smooch.KissHandler;
import com.alexgodly.smooch.KissPayload;
import com.alexgodly.smooch.Smooch;
import com.alexgodly.smooch.SmoochConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Smooch.MOD_ID)
public class SmoochNeoForge {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Smooch.MOD_ID);
    private static final DeferredHolder<SoundEvent, SoundEvent> KISS = sound("kiss");
    private static final DeferredHolder<SoundEvent, SoundEvent> SPARKLE = sound("sparkle");
    private static final DeferredHolder<SoundEvent, SoundEvent> EFFECTS = sound("effects");

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Smooch.id(name)));
    }

    public SmoochNeoForge(IEventBus modBus, ModContainer container) {
        SOUNDS.register(modBus);
        Smooch.KISS = KISS;
        Smooch.SPARKLE = SPARKLE;
        Smooch.EFFECTS = EFFECTS;

        SmoochConfig.init(FMLPaths.CONFIGDIR.get());

        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> event.registrar("1").optional()
                .playToServer(KissPayload.TYPE, KissPayload.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer player) KissHandler.onKeybind(player);
                })));

        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> KissHandler.tick(event.getServer()));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) KissHandler.forget(player);
        });
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> KissHandler.clear());
    }
}
