package com.alexgodly.smooch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SmoochConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("smooch.json");

    public enum CooldownMode { PER_PLAYER, PER_PAIR }

    public static class EffectEntry {
        public String id;
        public boolean enabled = true;
        public int seconds = 20;
        public int level = 1;

        public EffectEntry() {}

        EffectEntry(String id, int seconds) {
            this.id = id;
            this.seconds = seconds;
        }
    }

    public boolean kissByCrouching = true;
    public boolean kissByKeybind = true;
    public double reach = 3.0;
    public boolean heartsOnKiss = true;
    public boolean heartsWhileHolding = true;
    public boolean sounds = true;
    public boolean cooldownEnabled = true;
    public int cooldownSeconds = 300;
    public CooldownMode cooldownMode = CooldownMode.PER_PLAYER;
    public List<EffectEntry> effects = defaultEffects();

    private static SmoochConfig instance;

    public static SmoochConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static List<EffectEntry> defaultEffects() {
        List<EffectEntry> list = new ArrayList<>();
        for (String id : new String[]{"regeneration", "absorption", "resistance", "strength", "speed", "haste",
                "jump_boost", "health_boost", "fire_resistance", "water_breathing", "night_vision", "luck",
                "slow_falling", "hero_of_the_village"}) {
            list.add(new EffectEntry("minecraft:" + id, 20));
        }
        list.add(new EffectEntry("minecraft:saturation", 1));
        return list;
    }

    public static void load() {
        SmoochConfig cfg = null;
        if (Files.exists(FILE)) {
            try (Reader r = Files.newBufferedReader(FILE)) {
                cfg = GSON.fromJson(r, SmoochConfig.class);
            } catch (Exception e) {
                Smooch.LOGGER.warn("Couldn't read {}, using defaults: {}", FILE, e.getMessage());
            }
        }
        if (cfg == null) cfg = new SmoochConfig();
        cfg.sanitize();
        instance = cfg;
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE)) {
                GSON.toJson(instance, w);
            }
        } catch (IOException e) {
            Smooch.LOGGER.warn("Couldn't save {}: {}", FILE, e.getMessage());
        }
    }

    public static void reset() {
        instance = new SmoochConfig();
        save();
    }

    private void sanitize() {
        if (effects == null) effects = defaultEffects();
        if (cooldownMode == null) cooldownMode = CooldownMode.PER_PLAYER;
        reach = Math.max(1.0, Math.min(16.0, reach));
        cooldownSeconds = Math.max(0, cooldownSeconds);
        effects.removeIf(e -> e == null || e.id == null || e.id.isBlank());
        for (EffectEntry e : effects) {
            e.seconds = Math.max(1, e.seconds);
            e.level = Math.max(1, Math.min(255, e.level));
        }
    }
}
