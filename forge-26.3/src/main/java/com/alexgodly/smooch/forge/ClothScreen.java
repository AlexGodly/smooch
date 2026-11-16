package com.alexgodly.smooch.forge;

import com.alexgodly.smooch.ConfigScreens;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public final class ClothScreen {
    private ClothScreen() {}

    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> ConfigScreens.create(parent)));
    }
}
