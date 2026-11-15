package com.alexgodly.smooch;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record KissPayload() implements CustomPacketPayload {
    public static final KissPayload INSTANCE = new KissPayload();
    public static final Type<KissPayload> TYPE = new Type<>(Smooch.id("kiss"));
    public static final StreamCodec<ByteBuf, KissPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
