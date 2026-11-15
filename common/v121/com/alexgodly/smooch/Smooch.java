package com.alexgodly.smooch;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public final class Smooch {
    public static final String MOD_ID = "smooch";
    public static final Logger LOGGER = LoggerFactory.getLogger("Smooch");

    public static Supplier<SoundEvent> KISS;
    public static Supplier<SoundEvent> SPARKLE;
    public static Supplier<SoundEvent> EFFECTS;

    private Smooch() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
