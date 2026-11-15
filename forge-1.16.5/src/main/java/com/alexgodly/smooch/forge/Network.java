package com.alexgodly.smooch.forge;

import com.alexgodly.smooch.KissHandler;
import com.alexgodly.smooch.Smooch;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class Network {
    private static final String VERSION = "1";
    static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(Smooch.KISS_PACKET,
            () -> VERSION, v -> true, v -> true);

    private Network() {}

    public static final class Kiss {}

    static void register() {
        CHANNEL.messageBuilder(Kiss.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder((msg, buf) -> {})
                .decoder(buf -> new Kiss())
                .consumer((msg, ctx) -> {
                    ctx.get().enqueueWork(() -> {
                        ServerPlayer player = ctx.get().getSender();
                        if (player != null) KissHandler.onKeybind(player);
                    });
                    ctx.get().setPacketHandled(true);
                })
                .add();
    }

    static boolean serverHasMod(Connection connection) {
        return CHANNEL.isRemotePresent(connection);
    }
}
