package com.alexgodly.smooch;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public final class ConfigScreens {
    private ConfigScreens() {}

    public static Screen create(Screen parent) {
        SmoochConfig cfg = SmoochConfig.get();
        SmoochConfig def = new SmoochConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new TranslationTextComponent("config.smooch.title"))
                .setSavingRunnable(SmoochConfig::save);
        ConfigEntryBuilder eb = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(new TranslationTextComponent("config.smooch.general"));
        general.addEntry(eb.startBooleanToggle(new TranslationTextComponent("config.smooch.kissByCrouching"), cfg.kissByCrouching)
                .setDefaultValue(def.kissByCrouching)
                .setTooltip(new TranslationTextComponent("config.smooch.kissByCrouching.tooltip"))
                .setSaveConsumer(v -> cfg.kissByCrouching = v).build());
        general.addEntry(eb.startBooleanToggle(new TranslationTextComponent("config.smooch.kissByKeybind"), cfg.kissByKeybind)
                .setDefaultValue(def.kissByKeybind)
                .setTooltip(new TranslationTextComponent("config.smooch.kissByKeybind.tooltip"))
                .setSaveConsumer(v -> cfg.kissByKeybind = v).build());
        general.addEntry(eb.startDoubleField(new TranslationTextComponent("config.smooch.reach"), cfg.reach)
                .setDefaultValue(def.reach).setMin(1.0).setMax(16.0)
                .setSaveConsumer(v -> cfg.reach = v).build());
        general.addEntry(eb.startBooleanToggle(new TranslationTextComponent("config.smooch.heartsOnKiss"), cfg.heartsOnKiss)
                .setDefaultValue(def.heartsOnKiss)
                .setSaveConsumer(v -> cfg.heartsOnKiss = v).build());
        general.addEntry(eb.startBooleanToggle(new TranslationTextComponent("config.smooch.heartsWhileHolding"), cfg.heartsWhileHolding)
                .setDefaultValue(def.heartsWhileHolding)
                .setSaveConsumer(v -> cfg.heartsWhileHolding = v).build());
        general.addEntry(eb.startBooleanToggle(new TranslationTextComponent("config.smooch.sounds"), cfg.sounds)
                .setDefaultValue(def.sounds)
                .setSaveConsumer(v -> cfg.sounds = v).build());

        ConfigCategory cooldown = builder.getOrCreateCategory(new TranslationTextComponent("config.smooch.cooldown"));
        cooldown.addEntry(eb.startBooleanToggle(new TranslationTextComponent("config.smooch.cooldownEnabled"), cfg.cooldownEnabled)
                .setDefaultValue(def.cooldownEnabled)
                .setTooltip(new TranslationTextComponent("config.smooch.cooldownEnabled.tooltip"))
                .setSaveConsumer(v -> cfg.cooldownEnabled = v).build());
        cooldown.addEntry(eb.startIntField(new TranslationTextComponent("config.smooch.cooldownSeconds"), cfg.cooldownSeconds)
                .setDefaultValue(def.cooldownSeconds).setMin(0)
                .setSaveConsumer(v -> cfg.cooldownSeconds = v).build());
        cooldown.addEntry(eb.startEnumSelector(new TranslationTextComponent("config.smooch.cooldownMode"), SmoochConfig.CooldownMode.class, cfg.cooldownMode)
                .setDefaultValue(def.cooldownMode)
                .setEnumNameProvider(m -> new TranslationTextComponent("config.smooch.cooldownMode." + m.name().toLowerCase()))
                .setTooltip(new TranslationTextComponent("config.smooch.cooldownMode.tooltip"))
                .setSaveConsumer(v -> cfg.cooldownMode = v).build());

        ConfigCategory effects = builder.getOrCreateCategory(new TranslationTextComponent("config.smooch.effects"));
        for (SmoochConfig.EffectEntry e : cfg.effects) {
            String path = e.id.contains(":") ? e.id.substring(e.id.indexOf(':') + 1) : e.id;
            SubCategoryBuilder sub = eb.startSubCategory(new TranslationTextComponent("effect." + e.id.replace(':', '.')));
            sub.add(eb.startBooleanToggle(new TranslationTextComponent("config.smooch.effect.enabled"), e.enabled)
                    .setDefaultValue(true).setSaveConsumer(v -> e.enabled = v).build());
            sub.add(eb.startIntField(new TranslationTextComponent("config.smooch.effect.seconds"), e.seconds)
                    .setDefaultValue(path.equals("saturation") ? 1 : 20).setMin(1)
                    .setSaveConsumer(v -> e.seconds = v).build());
            sub.add(eb.startIntSlider(new TranslationTextComponent("config.smooch.effect.level"), e.level, 1, 10)
                    .setDefaultValue(1).setSaveConsumer(v -> e.level = v).build());
            effects.addEntry(sub.build());
        }

        return builder.build();
    }
}
