package com.alexgodly.smooch;

import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.Vec3d;

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
        for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            boolean crouching = player.isSneaking() && !player.isSpectator();
            boolean started = crouching && !wasCrouching.contains(id);
            if (crouching) wasCrouching.add(id); else wasCrouching.remove(id);

            int timer = heartTimer.getOrDefault(id, 0);
            if (timer > 0) heartTimer.put(id, timer - 1);

            if (!crouching || !cfg.kissByCrouching) continue;
            ServerPlayerEntity target = findTarget(player, cfg.reach);
            if (target == null) continue;

            if (started) {
                kiss(player, target, cfg);
            } else if (cfg.heartsWhileHolding && timer <= 0) {
                hearts(player, target);
                heartTimer.put(id, HOLD_HEART_INTERVAL);
            }
        }
    }

    public static void onKeybind(ServerPlayerEntity player) {
        SmoochConfig cfg = SmoochConfig.get();
        if (!cfg.kissByKeybind || player.isSpectator()) return;
        ServerPlayerEntity target = findTarget(player, cfg.reach);
        if (target != null) kiss(player, target, cfg);
    }

    static ServerPlayerEntity findTarget(ServerPlayerEntity player, double reach) {
        Vec3d eye = player.getEyePosition(1.0F);
        Vec3d look = player.getViewVector(1.0F);
        ServerPlayerEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (ServerPlayerEntity other : player.getLevel().players()) {
            if (other == player || other.isSpectator()) continue;
            Vec3d toHead = other.getEyePosition(1.0F).subtract(eye);
            double along = toHead.dot(look);
            if (along <= 0 || along > reach) continue;
            double off = toHead.subtract(look.scale(along)).length();
            if (off > 0.5 || along >= bestDist) continue;
            if (!player.canSee(other)) continue;
            best = other;
            bestDist = along;
        }
        return best;
    }

    private static void kiss(ServerPlayerEntity player, ServerPlayerEntity target, SmoochConfig cfg) {
        ServerWorld level = player.getLevel();
        if (cfg.heartsOnKiss) {
            hearts(player, target);
            heartTimer.put(player.getUUID(), HOLD_HEART_INTERVAL);
            if (cfg.sounds) {
                level.playSound(null, player.x, (player.y + player.getEyeHeight()), player.z, Smooch.SPARKLE.get(), SoundCategory.PLAYERS, 0.7F, 1.0F);
            }
        }
        if (cfg.sounds) {
            level.playSound(null, player.x, (player.y + player.getEyeHeight()), player.z, Smooch.KISS.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);
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

    private static void playerReward(ServerPlayerEntity p, long now, long cooldown, SmoochConfig cfg) {
        if (cooldownUntil.getOrDefault(p.getUUID(), 0L) > now) return;
        reward(p, cfg);
        cooldownUntil.put(p.getUUID(), now + cooldown);
    }

    private static void pairReward(ServerPlayerEntity p, ServerPlayerEntity partner, long now, long cooldown, SmoochConfig cfg) {
        UUID id = p.getUUID();
        boolean samePartner = partner.getUUID().equals(lastPartner.get(id));
        if (samePartner && pairCooldownUntil.getOrDefault(id, 0L) > now) return;
        reward(p, cfg);
        lastPartner.put(id, partner.getUUID());
        pairCooldownUntil.put(id, now + cooldown);
    }

    private static void reward(ServerPlayerEntity p, SmoochConfig cfg) {
        boolean any = false;
        for (SmoochConfig.EffectEntry e : cfg.effects) {
            if (!e.enabled) continue;
            ResourceLocation key = ResourceLocation.tryParse(e.id);
            if (key == null) continue;
            Effect effect = Registry.MOB_EFFECT.get(key);
            if (effect == null) continue;
            p.addEffect(new EffectInstance(effect, e.seconds * 20, e.level - 1, false, false, true));
            any = true;
        }
        if (any && cfg.sounds) {
            p.getLevel().playSound(null, p.x, p.y, p.z, Smooch.EFFECTS.get(), SoundCategory.PLAYERS, 0.5F, 1.0F);
        }
    }

    private static void hearts(ServerPlayerEntity player, ServerPlayerEntity target) {
        ServerWorld level = player.getLevel();
        Vec3d a = player.getEyePosition(1.0F);
        Vec3d b = target.getEyePosition(1.0F);
        Vec3d mid = a.add(b).scale(0.5);
        level.sendParticles(ParticleTypes.HEART, mid.x, mid.y + 0.3, mid.z, 3, 0.25, 0.2, 0.25, 0.0);
        level.sendParticles(ParticleTypes.HEART, b.x, b.y + 0.4, b.z, 1, 0.2, 0.1, 0.2, 0.0);
    }

    public static void forget(ServerPlayerEntity player) {
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
