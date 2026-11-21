"""Rewrite Mojang class names to the MCP names Forge used up to 1.16.5."""
import os
import re
import sys

CLASSES = {
    "com.mojang.blaze3d.platform.InputConstants": "net.minecraft.client.util.InputMappings",
    "net.minecraft.client.KeyMapping": "net.minecraft.client.settings.KeyBinding",
    "net.minecraft.client.gui.screens.Screen": "net.minecraft.client.gui.screen.Screen",
    "net.minecraft.core.Registry": "net.minecraft.util.registry.Registry",
    "net.minecraft.core.particles.ParticleTypes": "net.minecraft.particles.ParticleTypes",
    "net.minecraft.network.Connection": "net.minecraft.network.NetworkManager",
    "net.minecraft.network.chat.Component": "net.minecraft.util.text.ITextComponent",
    "net.minecraft.network.chat.TranslatableComponent": "net.minecraft.util.text.TranslationTextComponent",
    "net.minecraft.resources.ResourceLocation": "net.minecraft.util.ResourceLocation",
    "net.minecraft.server.level.ServerLevel": "net.minecraft.world.server.ServerWorld",
    "net.minecraft.server.level.ServerPlayer": "net.minecraft.entity.player.ServerPlayerEntity",
    "net.minecraft.sounds.SoundEvent": "net.minecraft.util.SoundEvent",
    "net.minecraft.sounds.SoundSource": "net.minecraft.util.SoundCategory",
    "net.minecraft.world.effect.MobEffect": "net.minecraft.potion.Effect",
    "net.minecraft.world.effect.MobEffectInstance": "net.minecraft.potion.EffectInstance",
    "net.minecraft.world.phys.Vec3": "net.minecraft.util.math.vector.Vector3d",
}
OLD_VEC = {"net.minecraft.util.math.vector.Vector3d": "net.minecraft.util.math.Vec3d"}


def convert(text, old_vec=False):
    table = dict(CLASSES)
    if old_vec:
        table = {k: OLD_VEC.get(v, v) for k, v in table.items()}
    for full, new in sorted(table.items(), key=lambda kv: -len(kv[0])):
        text = text.replace("import " + full + ";", "import " + new + ";")
        short_old, short_new = full.rsplit(".", 1)[1], new.rsplit(".", 1)[1]
        if short_old != short_new:
            text = re.sub(r"(?<![\w.])" + short_old + r"\b", short_new, text)
    return text


def main(src, dst, old_vec=False):
    for root, _, files in os.walk(src):
        for f in files:
            if not f.endswith(".java"):
                continue
            path = os.path.join(root, f)
            out = os.path.join(dst, os.path.relpath(path, src))
            os.makedirs(os.path.dirname(out), exist_ok=True)
            with open(path) as fh:
                text = fh.read()
            with open(out, "w") as fh:
                fh.write(convert(text, old_vec))


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2], "--old-vec" in sys.argv)
