package com.alexgodly.smooch.neoforge;

import com.alexgodly.smooch.KissHandler;
import com.alexgodly.smooch.Smooch;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.simple.SimpleChannel;

final class Network {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(Smooch.KISS_PACKET,
            () -> VERSION, v -> true, v -> true);

    private Network() {}

    record Kiss() {}

    static void register(IEventBus modBus) {
        CHANNEL.messageBuilder(Kiss.class, 0, PlayNetworkDirection.PLAY_TO_SERVER)
                .encoder((msg, buf) -> {})
                .decoder(buf -> new Kiss())
                .consumerMainThread((msg, ctx) -> {
                    ServerPlayer player = ctx.getSender();
                    if (player != null) KissHandler.onKeybind(player);
                })
                .add();
    }

    static boolean serverHasMod() {
        return CHANNEL.isRemotePresent(Minecraft.getInstance().getConnection().getConnection());
    }

    static void sendKiss() {
        CHANNEL.sendToServer(new Kiss());
    }
}
