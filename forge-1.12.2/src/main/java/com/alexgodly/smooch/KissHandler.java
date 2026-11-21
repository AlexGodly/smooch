package com.alexgodly.smooch;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

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
        if (server == null) return;
        SmoochConfig cfg = SmoochConfig.get();
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUniqueID();
            boolean crouching = player.isSneaking() && !player.isSpectator();
            boolean started = crouching && !wasCrouching.contains(id);
            if (crouching) wasCrouching.add(id); else wasCrouching.remove(id);

            int timer = heartTimer.getOrDefault(id, 0);
            if (timer > 0) heartTimer.put(id, timer - 1);

            if (!crouching || !cfg.kissByCrouching) continue;
            EntityPlayerMP target = findTarget(player, cfg.reach);
            if (target == null) continue;

            if (started) {
                kiss(player, target, cfg);
            } else if (cfg.heartsWhileHolding && timer <= 0) {
                hearts(player, target);
                heartTimer.put(id, HOLD_HEART_INTERVAL);
            }
        }
    }

    public static void onKeybind(EntityPlayerMP player) {
        SmoochConfig cfg = SmoochConfig.get();
        if (!cfg.kissByKeybind || player.isSpectator()) return;
        EntityPlayerMP target = findTarget(player, cfg.reach);
        if (target != null) kiss(player, target, cfg);
    }

    static EntityPlayerMP findTarget(EntityPlayerMP player, double reach) {
        Vec3d eye = player.getPositionEyes(1.0F);
        Vec3d look = player.getLook(1.0F);
        EntityPlayerMP best = null;
        double bestDist = Double.MAX_VALUE;
        for (EntityPlayer o : player.getServerWorld().playerEntities) {
            if (!(o instanceof EntityPlayerMP) || o == player || o.isSpectator()) continue;
            EntityPlayerMP other = (EntityPlayerMP) o;
            Vec3d toHead = other.getPositionEyes(1.0F).subtract(eye);
            double along = toHead.dotProduct(look);
            if (along <= 0 || along > reach) continue;
            double off = toHead.subtract(look.scale(along)).length();
            if (off > 0.5 || along >= bestDist) continue;
            if (!player.canEntityBeSeen(other)) continue;
            best = other;
            bestDist = along;
        }
        return best;
    }

    private static void play(EntityPlayerMP p, double y, SoundEvent sound, float volume) {
        p.getServerWorld().playSound(null, p.posX, y, p.posZ, sound, SoundCategory.PLAYERS, volume, 1.0F);
    }

    private static void kiss(EntityPlayerMP player, EntityPlayerMP target, SmoochConfig cfg) {
        double eyeY = player.posY + player.getEyeHeight();
        if (cfg.heartsOnKiss) {
            hearts(player, target);
            heartTimer.put(player.getUniqueID(), HOLD_HEART_INTERVAL);
            if (cfg.sounds) play(player, eyeY, Smooch.SPARKLE, 0.7F);
        }
        if (cfg.sounds) play(player, eyeY, Smooch.KISS, 1.0F);

        long now = player.getServerWorld().getTotalWorldTime();
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

    private static void playerReward(EntityPlayerMP p, long now, long cooldown, SmoochConfig cfg) {
        if (cooldownUntil.getOrDefault(p.getUniqueID(), 0L) > now) return;
        reward(p, cfg);
        cooldownUntil.put(p.getUniqueID(), now + cooldown);
    }

    private static void pairReward(EntityPlayerMP p, EntityPlayerMP partner, long now, long cooldown, SmoochConfig cfg) {
        UUID id = p.getUniqueID();
        boolean samePartner = partner.getUniqueID().equals(lastPartner.get(id));
        if (samePartner && pairCooldownUntil.getOrDefault(id, 0L) > now) return;
        reward(p, cfg);
        lastPartner.put(id, partner.getUniqueID());
        pairCooldownUntil.put(id, now + cooldown);
    }

    private static void reward(EntityPlayerMP p, SmoochConfig cfg) {
        boolean any = false;
        for (SmoochConfig.EffectEntry e : cfg.effects) {
            if (!e.enabled) continue;
            Potion potion;
            try {
                potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(e.id));
            } catch (RuntimeException ex) {
                continue;
            }
            if (potion == null) continue;
            p.addPotionEffect(new PotionEffect(potion, e.seconds * 20, e.level - 1, false, false));
            any = true;
        }
        if (any && cfg.sounds) play(p, p.posY, Smooch.EFFECTS, 0.5F);
    }

    private static void hearts(EntityPlayerMP player, EntityPlayerMP target) {
        WorldServer world = player.getServerWorld();
        Vec3d a = player.getPositionEyes(1.0F);
        Vec3d b = target.getPositionEyes(1.0F);
        Vec3d mid = a.add(b).scale(0.5);
        world.spawnParticle(EnumParticleTypes.HEART, mid.x, mid.y + 0.3, mid.z, 3, 0.25, 0.2, 0.25, 0.0);
        world.spawnParticle(EnumParticleTypes.HEART, b.x, b.y + 0.4, b.z, 1, 0.2, 0.1, 0.2, 0.0);
    }

    public static void forget(EntityPlayerMP player) {
        UUID id = player.getUniqueID();
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
