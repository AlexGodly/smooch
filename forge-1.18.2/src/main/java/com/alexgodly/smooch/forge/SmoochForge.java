package com.alexgodly.smooch.forge;

import com.alexgodly.smooch.ConfigScreens;
import com.alexgodly.smooch.KissHandler;
import com.alexgodly.smooch.Smooch;
import com.alexgodly.smooch.SmoochConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod(Smooch.MOD_ID)
public class SmoochForge {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Smooch.MOD_ID);
    private static final RegistryObject<SoundEvent> KISS = sound("kiss");
    private static final RegistryObject<SoundEvent> SPARKLE = sound("sparkle");
    private static final RegistryObject<SoundEvent> EFFECTS = sound("effects");

    private static RegistryObject<SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(Smooch.id(name)));
    }

    public SmoochForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        SOUNDS.register(modBus);
        Smooch.KISS = KISS;
        Smooch.SPARKLE = SPARKLE;
        Smooch.EFFECTS = EFFECTS;

        SmoochConfig.init(FMLPaths.CONFIGDIR.get());
        Network.register();
        MinecraftForge.EVENT_BUS.register(this);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            SmoochForgeClient.init(modBus);
            if (ModList.get().isLoaded("cloth_config")) {
                ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                        () -> new ConfigGuiHandler.ConfigGuiFactory((mc, parent) -> ConfigScreens.create(parent)));
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) KissHandler.tick(ServerLifecycleHooks.getCurrentServer());
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer) KissHandler.forget((ServerPlayer) event.getPlayer());
    }

    @SubscribeEvent
    public void onStopped(ServerStoppedEvent event) {
        KissHandler.clear();
    }
}
