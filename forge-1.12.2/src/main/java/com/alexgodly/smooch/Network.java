package com.alexgodly.smooch;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class Network {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Smooch.MOD_ID);

    private Network() {}

    static void register() {
        CHANNEL.registerMessage(KissHandlerMessage.class, Kiss.class, 0, Side.SERVER);
    }

    public static class Kiss implements IMessage {
        @Override
        public void fromBytes(ByteBuf buf) {}

        @Override
        public void toBytes(ByteBuf buf) {}
    }

    public static class KissHandlerMessage implements IMessageHandler<Kiss, IMessage> {
        @Override
        public IMessage onMessage(Kiss message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> KissHandler.onKeybind(player));
            return null;
        }
    }
}
