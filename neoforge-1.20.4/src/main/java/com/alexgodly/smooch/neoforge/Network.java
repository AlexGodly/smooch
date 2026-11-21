package com.alexgodly.smooch.neoforge;

import com.alexgodly.smooch.KissHandler;
import com.alexgodly.smooch.Smooch;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;

final class Network {
    private Network() {}

    record Kiss() implements CustomPacketPayload {
        static final ResourceLocation ID = Smooch.KISS_PACKET;

        static Kiss read(FriendlyByteBuf buf) {
            return new Kiss();
        }

        @Override
        public void write(FriendlyByteBuf buf) {}

        @Override
        public ResourceLocation id() {
            return ID;
        }
    }

    static void register(IEventBus modBus) {
        modBus.addListener((RegisterPayloadHandlerEvent event) -> event.registrar(Smooch.MOD_ID).versioned("1").optional()
                .play(Kiss.ID, Kiss::read, handler -> handler.server((msg, ctx) -> ctx.workHandler().execute(() ->
                        ctx.player().ifPresent(p -> {
                            if (p instanceof ServerPlayer player) KissHandler.onKeybind(player);
                        })))));
    }

    static boolean serverHasMod() {
        return Minecraft.getInstance().getConnection().isConnected(Kiss.ID);
    }

    static void sendKiss() {
        PacketDistributor.SERVER.noArg().send(new Kiss());
    }
}
