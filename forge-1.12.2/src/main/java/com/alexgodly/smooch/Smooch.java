package com.alexgodly.smooch;

import com.alexgodly.smooch.client.SmoochClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Smooch.MOD_ID, name = "Smooch", version = "1.0.0", acceptableRemoteVersions = "*")
public class Smooch {
    public static final String MOD_ID = "smooch";
    public static final Logger LOGGER = LogManager.getLogger("Smooch");

    public static final SoundEvent KISS = sound("kiss");
    public static final SoundEvent SPARKLE = sound("sparkle");
    public static final SoundEvent EFFECTS = sound("effects");

    private static SoundEvent sound(String name) {
        ResourceLocation id = new ResourceLocation(MOD_ID, name);
        return new SoundEvent(id).setRegistryName(id);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        SmoochConfig.init(event.getModConfigurationDirectory().toPath());
        Network.register();
        MinecraftForge.EVENT_BUS.register(this);
        if (event.getSide().isClient()) SmoochClient.init();
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        KissHandler.clear();
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(KISS, SPARKLE, EFFECTS);
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            KissHandler.tick(FMLCommonHandler.instance().getMinecraftServerInstance());
        }
    }

    @SubscribeEvent
    public void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) KissHandler.forget((EntityPlayerMP) event.player);
    }
}
