package com.alexgodly.smooch;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public final class ConfigScreens {
    private ConfigScreens() {}

    public static Screen create(Screen parent) {
        SmoochConfig cfg = SmoochConfig.get();
        SmoochConfig def = new SmoochConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new TranslatableComponent("config.smooch.title"))
                .setSavingRunnable(SmoochConfig::save);
        ConfigEntryBuilder eb = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(new TranslatableComponent("config.smooch.general"));
        general.addEntry(eb.startBooleanToggle(new TranslatableComponent("config.smooch.kissByCrouching"), cfg.kissByCrouching)
                .setDefaultValue(def.kissByCrouching)
                .setTooltip(new TranslatableComponent("config.smooch.kissByCrouching.tooltip"))
                .setSaveConsumer(v -> cfg.kissByCrouching = v).build());
        general.addEntry(eb.startBooleanToggle(new TranslatableComponent("config.smooch.kissByKeybind"), cfg.kissByKeybind)
                .setDefaultValue(def.kissByKeybind)
                .setTooltip(new TranslatableComponent("config.smooch.kissByKeybind.tooltip"))
                .setSaveConsumer(v -> cfg.kissByKeybind = v).build());
        general.addEntry(eb.startDoubleField(new TranslatableComponent("config.smooch.reach"), cfg.reach)
                .setDefaultValue(def.reach).setMin(1.0).setMax(16.0)
                .setSaveConsumer(v -> cfg.reach = v).build());
        general.addEntry(eb.startBooleanToggle(new TranslatableComponent("config.smooch.heartsOnKiss"), cfg.heartsOnKiss)
                .setDefaultValue(def.heartsOnKiss)
                .setSaveConsumer(v -> cfg.heartsOnKiss = v).build());
        general.addEntry(eb.startBooleanToggle(new TranslatableComponent("config.smooch.heartsWhileHolding"), cfg.heartsWhileHolding)
                .setDefaultValue(def.heartsWhileHolding)
                .setSaveConsumer(v -> cfg.heartsWhileHolding = v).build());
        general.addEntry(eb.startBooleanToggle(new TranslatableComponent("config.smooch.sounds"), cfg.sounds)
                .setDefaultValue(def.sounds)
                .setSaveConsumer(v -> cfg.sounds = v).build());

        ConfigCategory cooldown = builder.getOrCreateCategory(new TranslatableComponent("config.smooch.cooldown"));
        cooldown.addEntry(eb.startBooleanToggle(new TranslatableComponent("config.smooch.cooldownEnabled"), cfg.cooldownEnabled)
                .setDefaultValue(def.cooldownEnabled)
                .setTooltip(new TranslatableComponent("config.smooch.cooldownEnabled.tooltip"))
                .setSaveConsumer(v -> cfg.cooldownEnabled = v).build());
        cooldown.addEntry(eb.startIntField(new TranslatableComponent("config.smooch.cooldownSeconds"), cfg.cooldownSeconds)
                .setDefaultValue(def.cooldownSeconds).setMin(0)
                .setSaveConsumer(v -> cfg.cooldownSeconds = v).build());
        cooldown.addEntry(eb.startEnumSelector(new TranslatableComponent("config.smooch.cooldownMode"), SmoochConfig.CooldownMode.class, cfg.cooldownMode)
                .setDefaultValue(def.cooldownMode)
                .setEnumNameProvider(m -> new TranslatableComponent("config.smooch.cooldownMode." + m.name().toLowerCase()))
                .setTooltip(new TranslatableComponent("config.smooch.cooldownMode.tooltip"))
                .setSaveConsumer(v -> cfg.cooldownMode = v).build());

        ConfigCategory effects = builder.getOrCreateCategory(new TranslatableComponent("config.smooch.effects"));
        for (SmoochConfig.EffectEntry e : cfg.effects) {
            String path = e.id.contains(":") ? e.id.substring(e.id.indexOf(':') + 1) : e.id;
            SubCategoryBuilder sub = eb.startSubCategory(new TranslatableComponent("effect." + e.id.replace(':', '.')));
            sub.add(eb.startBooleanToggle(new TranslatableComponent("config.smooch.effect.enabled"), e.enabled)
                    .setDefaultValue(true).setSaveConsumer(v -> e.enabled = v).build());
            sub.add(eb.startIntField(new TranslatableComponent("config.smooch.effect.seconds"), e.seconds)
                    .setDefaultValue(path.equals("saturation") ? 1 : 20).setMin(1)
                    .setSaveConsumer(v -> e.seconds = v).build());
            sub.add(eb.startIntSlider(new TranslatableComponent("config.smooch.effect.level"), e.level, 1, 10)
                    .setDefaultValue(1).setSaveConsumer(v -> e.level = v).build());
            effects.addEntry(sub.build());
        }

        return builder.build();
    }
}
