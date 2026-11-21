package com.alexgodly.smooch.neoforge;

import com.alexgodly.smooch.KissHandler;
import com.alexgodly.smooch.Smooch;
import com.alexgodly.smooch.SmoochConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
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

    public SmoochNeoForge(IEventBus modBus) {
        SOUNDS.register(modBus);
        Smooch.KISS = KISS;
        Smooch.SPARKLE = SPARKLE;
        Smooch.EFFECTS = EFFECTS;

        SmoochConfig.init(FMLPaths.CONFIGDIR.get());
        Network.register(modBus);

        NeoForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) KissHandler.tick(event.getServer());
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) KissHandler.forget(player);
        });
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> KissHandler.clear());

        if (FMLEnvironment.dist == Dist.CLIENT) SmoochNeoForgeClient.init(modBus);
    }
}
