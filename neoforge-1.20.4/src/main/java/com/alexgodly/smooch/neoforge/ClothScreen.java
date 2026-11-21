package com.alexgodly.smooch.neoforge;

import com.alexgodly.smooch.ConfigScreens;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.ConfigScreenHandler;

public final class ClothScreen {
    private ClothScreen() {}

    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> ConfigScreens.create(parent)));
    }
}
