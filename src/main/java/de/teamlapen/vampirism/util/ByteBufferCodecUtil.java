package de.teamlapen.vampirism.util;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.UUID;

public class ByteBufferCodecUtil {
    public static final StreamCodec<ByteBuf, Vector3d> VECTOR3D = new StreamCodec<>() {
        @Override
        public @NotNull Vector3d decode(ByteBuf p_320376_) {
            return new Vector3d(p_320376_.readDouble(), p_320376_.readDouble(), p_320376_.readDouble());
        }

        @Override
        public void encode(ByteBuf p_320158_, Vector3d p_320396_) {
            p_320158_.writeDouble(p_320396_.x);
            p_320158_.writeDouble(p_320396_.y);
            p_320158_.writeDouble(p_320396_.z);
        }
    };

    public static final StreamCodec<ByteBuf, UUID> UUID = UUIDUtil.STREAM_CODEC;

    public static <B, T, Z> StreamCodec<B, Pair<T, Z>> pair(StreamCodec<? super B, T> stream1, StreamCodec<? super B, Z> stream2) {
        return new StreamCodec<>() {
            @Override
            public @NotNull Pair<T, Z> decode(@NotNull B buffer) {
                return Pair.of(stream1.decode(buffer), stream2.decode(buffer));
            }

            @Override
            public void encode(@NotNull B buffer, @NotNull Pair<T, Z> pair) {
                stream1.encode(buffer, pair.getFirst());
                stream2.encode(buffer, pair.getSecond());
            }
        };
    }
}
