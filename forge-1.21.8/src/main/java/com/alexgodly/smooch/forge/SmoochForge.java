package com.alexgodly.smooch.forge;

import com.alexgodly.smooch.KissHandler;
import com.alexgodly.smooch.Smooch;
import com.alexgodly.smooch.SmoochConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Smooch.MOD_ID)
public final class SmoochForge {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Smooch.MOD_ID);
    private static final RegistryObject<SoundEvent> KISS = sound("kiss");
    private static final RegistryObject<SoundEvent> SPARKLE = sound("sparkle");
    private static final RegistryObject<SoundEvent> EFFECTS = sound("effects");

    private static RegistryObject<SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Smooch.id(name)));
    }

    public SmoochForge(FMLJavaModLoadingContext context) {
        SOUNDS.register(context.getModBusGroup());
        Smooch.KISS = KISS;
        Smooch.SPARKLE = SPARKLE;
        Smooch.EFFECTS = EFFECTS;

        SmoochConfig.init(FMLPaths.CONFIGDIR.get());
        Network.register();

        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> KissHandler.tick(event.getServer()));
        PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer player) KissHandler.forget(player);
        });
        ServerStoppedEvent.BUS.addListener(event -> KissHandler.clear());

        if (FMLEnvironment.dist == Dist.CLIENT) SmoochForgeClient.init(context);
    }
}
