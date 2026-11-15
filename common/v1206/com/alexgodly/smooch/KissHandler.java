package com.alexgodly.smooch;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class KissHandler {
    private static final int HOLD_HEART_INTERVAL = 5;

    private static final Set<UUID> wasCrouching = new HashSet<>();
    private static final Map<UUID, Integer> heartTimer = new HashMap<>();
    private static final Map<UUID, Long> cooldownUntil = new HashMap<>();
    private static final Map<UUID, UUID> lastPartner = new HashMap<>();
    private static final Map<UUID, Long> pairCooldownUntil = new HashMap<>();

    private KissHandler() {}

    public static void tick(MinecraftServer server) {
        SmoochConfig cfg = SmoochConfig.get();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            boolean crouching = player.isShiftKeyDown() && !player.isSpectator();
            boolean started = crouching && !wasCrouching.contains(id);
            if (crouching) wasCrouching.add(id); else wasCrouching.remove(id);

            int timer = heartTimer.getOrDefault(id, 0);
            if (timer > 0) heartTimer.put(id, timer - 1);

            if (!crouching || !cfg.kissByCrouching) continue;
            ServerPlayer target = findTarget(player, cfg.reach);
            if (target == null) continue;

            if (started) {
                kiss(player, target, cfg);
            } else if (cfg.heartsWhileHolding && timer <= 0) {
                hearts(player, target);
                heartTimer.put(id, HOLD_HEART_INTERVAL);
            }
        }
    }

    public static void onKeybind(ServerPlayer player) {
        SmoochConfig cfg = SmoochConfig.get();
        if (!cfg.kissByKeybind || player.isSpectator()) return;
        ServerPlayer target = findTarget(player, cfg.reach);
        if (target != null) kiss(player, target, cfg);
    }

    static ServerPlayer findTarget(ServerPlayer player, double reach) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        ServerPlayer best = null;
        double bestDist = Double.MAX_VALUE;
        for (ServerPlayer other : player.serverLevel().players()) {
            if (other == player || other.isSpectator()) continue;
            Vec3 toHead = other.getEyePosition().subtract(eye);
            double along = toHead.dot(look);
            if (along <= 0 || along > reach) continue;
            double off = toHead.subtract(look.scale(along)).length();
            if (off > 0.5 || along >= bestDist) continue;
            if (!player.hasLineOfSight(other)) continue;
            best = other;
            bestDist = along;
        }
        return best;
    }

    private static void kiss(ServerPlayer player, ServerPlayer target, SmoochConfig cfg) {
        ServerLevel level = player.serverLevel();
        if (cfg.heartsOnKiss) {
            hearts(player, target);
            heartTimer.put(player.getUUID(), HOLD_HEART_INTERVAL);
            if (cfg.sounds) {
                level.playSound(null, player.getX(), player.getEyeY(), player.getZ(), Smooch.SPARKLE.get(), SoundSource.PLAYERS, 0.7F, 1.0F);
            }
        }
        if (cfg.sounds) {
            level.playSound(null, player.getX(), player.getEyeY(), player.getZ(), Smooch.KISS.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        long now = level.getGameTime();
        long cooldown = cfg.cooldownSeconds * 20L;
        if (!cfg.cooldownEnabled) {
            reward(player, cfg);
            reward(target, cfg);
        } else if (cfg.cooldownMode == SmoochConfig.CooldownMode.PER_PAIR) {
            pairReward(player, target, now, cooldown, cfg);
            pairReward(target, player, now, cooldown, cfg);
        } else {
            playerReward(player, now, cooldown, cfg);
            playerReward(target, now, cooldown, cfg);
        }
    }

    private static void playerReward(ServerPlayer p, long now, long cooldown, SmoochConfig cfg) {
        if (cooldownUntil.getOrDefault(p.getUUID(), 0L) > now) return;
        reward(p, cfg);
        cooldownUntil.put(p.getUUID(), now + cooldown);
    }

    private static void pairReward(ServerPlayer p, ServerPlayer partner, long now, long cooldown, SmoochConfig cfg) {
        UUID id = p.getUUID();
        boolean samePartner = partner.getUUID().equals(lastPartner.get(id));
        if (samePartner && pairCooldownUntil.getOrDefault(id, 0L) > now) return;
        reward(p, cfg);
        lastPartner.put(id, partner.getUUID());
        pairCooldownUntil.put(id, now + cooldown);
    }

    private static void reward(ServerPlayer p, SmoochConfig cfg) {
        boolean any = false;
        for (SmoochConfig.EffectEntry e : cfg.effects) {
            if (!e.enabled) continue;
            ResourceLocation key = ResourceLocation.tryParse(e.id);
            if (key == null) continue;
            MobEffect raw = BuiltInRegistries.MOB_EFFECT.get(key);
            if (raw == null) continue;
            Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(raw);
            p.addEffect(new MobEffectInstance(effect, e.seconds * 20, e.level - 1, false, false, true));
            any = true;
        }
        if (any && cfg.sounds) {
            p.serverLevel().playSound(null, p.getX(), p.getY(), p.getZ(), Smooch.EFFECTS.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
        }
    }

    private static void hearts(ServerPlayer player, ServerPlayer target) {
        ServerLevel level = player.serverLevel();
        Vec3 a = player.getEyePosition();
        Vec3 b = target.getEyePosition();
        Vec3 mid = a.add(b).scale(0.5);
        level.sendParticles(ParticleTypes.HEART, mid.x, mid.y + 0.3, mid.z, 3, 0.25, 0.2, 0.25, 0.0);
        level.sendParticles(ParticleTypes.HEART, b.x, b.y + 0.4, b.z, 1, 0.2, 0.1, 0.2, 0.0);
    }

    public static void forget(ServerPlayer player) {
        UUID id = player.getUUID();
        wasCrouching.remove(id);
        heartTimer.remove(id);
    }

    public static void clear() {
        wasCrouching.clear();
        heartTimer.clear();
        cooldownUntil.clear();
        lastPartner.clear();
        pairCooldownUntil.clear();
    }
}
