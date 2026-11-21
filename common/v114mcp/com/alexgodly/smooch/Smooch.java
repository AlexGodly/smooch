package com.alexgodly.smooch;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public final class Smooch {
    public static final String MOD_ID = "smooch";
    public static final Logger LOGGER = LogManager.getLogger("Smooch");

    public static final ResourceLocation KISS_PACKET = new ResourceLocation(MOD_ID, "kiss");

    public static Supplier<SoundEvent> KISS;
    public static Supplier<SoundEvent> SPARKLE;
    public static Supplier<SoundEvent> EFFECTS;

    private Smooch() {}

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
