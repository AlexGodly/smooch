package com.alexgodly.smooch.forge;

import net.minecraftforge.fml.ModList;

final class ConfigHook {
    private ConfigHook() {}

    static void register() {
        if (!ModList.get().isLoaded("cloth_config")) return;
        try {
            Class.forName("com.alexgodly.smooch.forge.ClothScreen").getMethod("register").invoke(null);
        } catch (ReflectiveOperationException ignored) {
            // built without Cloth Config support
        }
    }
}
