"""Generate Forge 1.20.4+ projects (ChannelBuilder networking)."""
import json, os, shutil, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PKG = "src/main/java/com/alexgodly/smooch/forge"


def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, "w").write(text)


MAIN_OLD = '''package com.alexgodly.smooch.forge;

import com.alexgodly.smooch.KissHandler;
import com.alexgodly.smooch.Smooch;
import com.alexgodly.smooch.SmoochConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod(Smooch.MOD_ID)
public class SmoochForge {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Smooch.MOD_ID);
    private static final RegistryObject<SoundEvent> KISS = sound("kiss");
    private static final RegistryObject<SoundEvent> SPARKLE = sound("sparkle");
    private static final RegistryObject<SoundEvent> EFFECTS = sound("effects");

    private static RegistryObject<SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Smooch.id(name)));
    }

    public SmoochForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        SOUNDS.register(modBus);
        Smooch.KISS = KISS;
        Smooch.SPARKLE = SPARKLE;
        Smooch.EFFECTS = EFFECTS;

        SmoochConfig.init(FMLPaths.CONFIGDIR.get());
        Network.register();

        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) KissHandler.tick(ServerLifecycleHooks.getCurrentServer());
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) KissHandler.forget(player);
        });
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> KissHandler.clear());

        if (FMLEnvironment.dist == Dist.CLIENT) SmoochForgeClient.init(modBus);
    }
}
'''

MAIN_EB7 = '''package com.alexgodly.smooch.forge;

import com.alexgodly.smooch.KissHandler;
import com.alexgodly.smooch.Smooch;
import com.alexgodly.smooch.SmoochConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Smooch.MOD_ID)
public final class SmoochForge {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Smooch.MOD_ID);
    private static final RegistryObject<SoundEvent> KISS = sound("kiss");
    private static final RegistryObject<SoundEvent> SPARKLE = sound("sparkle");
    private static final RegistryObject<SoundEvent> EFFECTS = sound("effects");

    private static RegistryObject<SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Smooch.id(name)));
    }

    public SmoochForge(FMLJavaModLoadingContext context) {
        SOUNDS.register(context.getModBusGroup());
        Smooch.KISS = KISS;
        Smooch.SPARKLE = SPARKLE;
        Smooch.EFFECTS = EFFECTS;

        SmoochConfig.init(FMLPaths.CONFIGDIR.get());
        Network.register();

        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> KissHandler.tick(event.server()));
        PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer player) KissHandler.forget(player);
        });
        ServerStoppedEvent.BUS.addListener(event -> KissHandler.clear());

        if (FMLEnvironment.dist == Dist.CLIENT) SmoochForgeClient.init(context);
    }
}
'''

NETWORK = '''package com.alexgodly.smooch.forge;

import com.alexgodly.smooch.KissHandler;
import com.alexgodly.smooch.Smooch;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class Network {
    static final SimpleChannel CHANNEL = ChannelBuilder.named(Smooch.id("kiss"))
            .networkProtocolVersion(1)
            .optional()
            .simpleChannel();

    private Network() {}

    public record Kiss() {}

    static void register() {
        CHANNEL.messageBuilder(Kiss.class, NetworkDirection.PLAY_TO_SERVER)
                .encoder((msg, buf) -> {})
                .decoder(buf -> new Kiss())
                .consumerMainThread((msg, ctx) -> {
                    ServerPlayer player = ctx.getSender();
                    if (player != null) KissHandler.onKeybind(player);
                })
                .add();
    }

    static boolean serverHasMod(Connection connection) {
        return CHANNEL.isRemotePresent(connection);
    }

    static void sendKiss() {
        CHANNEL.send(new Kiss(), PacketDistributor.SERVER.noArg());
    }
}
'''

CLIENT_OLD = '''package com.alexgodly.smooch.forge;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

final class SmoochForgeClient {
    static final KeyMapping KISS_KEY = new KeyMapping("key.smooch.kiss", InputConstants.Type.KEYSYM, 75, "key.categories.smooch");

    private SmoochForgeClient() {}

    static void init(IEventBus modBus) {
        modBus.addListener((RegisterKeyMappingsEvent event) -> event.register(KISS_KEY));
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) onTick();
        });
        ConfigHook.register();
    }

    static void onTick() {
        Minecraft mc = Minecraft.getInstance();
        while (KISS_KEY.consumeClick()) {
            if (mc.player != null && mc.getConnection() != null && Network.serverHasMod(mc.getConnection().getConnection())) {
                Network.sendKiss();
            }
        }
    }
}
'''

CLIENT_EB7 = '''package com.alexgodly.smooch.forge;

import com.alexgodly.smooch.Smooch;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

final class SmoochForgeClient {
    static final KeyMapping KISS_KEY = new KeyMapping("key.smooch.kiss", InputConstants.Type.KEYSYM, 75, @CATEGORY@);

    private SmoochForgeClient() {}

    static void init(FMLJavaModLoadingContext context) {
        RegisterKeyMappingsEvent.BUS.addListener(event -> event.register(KISS_KEY));
        TickEvent.ClientTickEvent.Post.BUS.addListener(event -> onTick());
        ConfigHook.register();
    }

    static void onTick() {
        Minecraft mc = Minecraft.getInstance();
        while (KISS_KEY.consumeClick()) {
            if (mc.player != null && mc.getConnection() != null && Network.serverHasMod(mc.getConnection().getConnection())) {
                Network.sendKiss();
            }
        }
    }
}
'''

CONFIG_HOOK = '''package com.alexgodly.smooch.forge;

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
'''

CLOTH_SCREEN = '''package com.alexgodly.smooch.forge;

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
'''

MODS_TOML = '''modLoader = "javafml"
loaderVersion = "[@MAJOR@,)"
license = "All Rights Reserved"

[[mods]]
modId = "smooch"
version = "${version}"
displayName = "Smooch: Kiss Other Players"
authors = "Alex Godly"
logoFile = "assets/smooch/icon.png"
description = \'\'\'Crouch or press K while looking at another player to kiss them. Hearts, kiss sounds and good effects for both of you.\'\'\'

[[dependencies.smooch]]
modId = "forge"
mandatory = true
versionRange = "[@MAJOR@,)"
ordering = "NONE"
side = "BOTH"

[[dependencies.smooch]]
modId = "minecraft"
mandatory = true
versionRange = "@RANGE@"
ordering = "NONE"
side = "BOTH"
'''

BUILD_FG7 = '''plugins {
    id 'java'
    id 'net.minecraftforge.gradle' version '@FG@'
}

version = "${mod_version}+forge-${minecraft_version}"
group = 'com.alexgodly'

base {
    archivesName = 'smooch'
}

java.toolchain.languageVersion = JavaLanguageVersion.of(@JAVA@)

minecraft {
@MAPPINGS@
}

sourceSets {
    main {
        java.srcDir '../common/@COMMON@'
        resources.srcDir '../common/assets'
        if (cloth_config_version == 'none') {
            java.exclude '**/ConfigScreens.java', '**/ClothScreen.java'
        }
    }
}

repositories {
    minecraft.mavenizer(it)
    maven fg.forgeMaven
    maven fg.minecraftLibsMaven
    maven { url = 'https://maven.shedaniel.me/' }
    mavenCentral()
}

dependencies {
    implementation minecraft.dependency("net.minecraftforge:forge:${minecraft_version}-${forge_version}")
    if (cloth_config_version != 'none') {
        compileOnly("me.shedaniel.cloth:cloth-config-forge:${cloth_config_version}") { transitive = false }
    }
}

processResources {
    inputs.property 'version', mod_version
    filesMatching('META-INF/mods.toml') {
        expand 'version': mod_version
    }
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
}
'''

SETTINGS = '''pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = 'https://maven.minecraftforge.net/' }
    }
}
plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '1.0.0'
}
rootProject.name = 'smooch'
'''


def make(mc, *, style, common, java, rng, pack, fg='[7.0.3,8)', gradle='9.5.0', category='"key.categories.smooch"', obf=True):
    d = os.path.join(ROOT, f'forge-{mc}')
    if os.path.exists(d):
        shutil.rmtree(d)
    os.makedirs(d)
    base = os.path.join(ROOT, 'forge-1.20.1')
    shutil.copytree(os.path.join(base, 'gradle'), os.path.join(d, 'gradle'))
    shutil.copy(os.path.join(base, 'gradlew'), d)
    shutil.copy(os.path.join(base, 'gradlew.bat'), d)
    props = os.path.join(d, 'gradle', 'wrapper', 'gradle-wrapper.properties')
    s = open(props).read()
    import re
    s = re.sub(r'gradle-[0-9.]+-bin', f'gradle-{gradle}-bin', s)
    open(props, 'w').write(s)
    write(os.path.join(d, 'settings.gradle'), SETTINGS)
    write(os.path.join(d, 'gradle.properties'),
          f'org.gradle.jvmargs=-Xmx3G\nminecraft_version={mc}\nforge_version=auto\ncloth_config_version=auto\ncloth_loader=forge\nmod_version=1.0.0\n')
    mappings = "    mappings channel: 'official', version: minecraft_version" if obf else ''
    write(os.path.join(d, 'build.gradle'), BUILD_FG7.replace('@FG@', fg).replace('@JAVA@', str(java))
          .replace('@COMMON@', common).replace('@MAPPINGS@', mappings))
    write(os.path.join(d, PKG, 'SmoochForge.java'), MAIN_OLD if style == 'old' else MAIN_EB7)
    write(os.path.join(d, PKG, 'Network.java'), NETWORK)
    write(os.path.join(d, PKG, 'SmoochForgeClient.java'), CLIENT_OLD if style == 'old' else CLIENT_EB7.replace('@CATEGORY@', category))
    write(os.path.join(d, PKG, 'ConfigHook.java'), CONFIG_HOOK)
    write(os.path.join(d, PKG, 'ClothScreen.java'), CLOTH_SCREEN)
    major = '@MAJOR@'
    write(os.path.join(d, 'src/main/resources/META-INF/mods.toml'), MODS_TOML.replace('@RANGE@', rng))
    write(os.path.join(d, 'src/main/resources/pack.mcmeta'),
          json.dumps({"pack": {"description": "Smooch resources", "pack_format": pack}}, indent=2))
    return d


if __name__ == '__main__':
    cat_new = 'KeyMapping.Category.register(Smooch.id("main"))'
    make('1.21.1', style='old', common='v121', java=21, rng='[1.21,1.21.2)', pack=34)
    make('1.21.3', style='old', common='v1212', java=21, rng='[1.21.2,1.21.4)', pack=42)
    make('1.21.4', style='old', common='v1212', java=21, rng='[1.21.4,1.21.5)', pack=46)
    make('1.21.5', style='old', common='v1212', java=21, rng='[1.21.5,1.21.6)', pack=55)
    make('1.21.8', style='eb7', common='v1212', java=21, rng='[1.21.6,1.21.9)', pack=64)
    make('1.21.10', style='eb7', common='v1212', java=21, rng='[1.21.9,1.21.11)', pack=69, category=cat_new)
    make('1.21.11', style='eb7', common='v263', java=21, rng='[1.21.11,1.22)', pack=75, category=cat_new)
    for mc, pack in [('26.1.2', 84), ('26.2', 100), ('26.3', 119)]:
        make(mc, style='eb7', common='v263', java=25, rng=f'[{mc.rsplit(".",1)[0] if mc.count(".")==2 else mc},{mc}.99)', pack=pack,
             fg='[7.0.17,8)', gradle='9.5.0', category=cat_new, obf=False)
