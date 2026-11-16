package com.alexgodly.smooch.forge;

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
