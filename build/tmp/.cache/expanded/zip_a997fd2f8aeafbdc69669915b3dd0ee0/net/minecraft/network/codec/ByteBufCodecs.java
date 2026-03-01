package net.minecraft.network.codec;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface ByteBufCodecs {
    int MAX_INITIAL_COLLECTION_SIZE = 65536;
    StreamCodec<ByteBuf, Boolean> BOOL = new StreamCodec<ByteBuf, Boolean>() {
        public Boolean decode(ByteBuf p_332480_) {
            return p_332480_.readBoolean();
        }

        public void encode(ByteBuf p_332710_, Boolean p_330535_) {
            p_332710_.writeBoolean(p_330535_);
        }
    };
    StreamCodec<ByteBuf, Byte> BYTE = new StreamCodec<ByteBuf, Byte>() {
        public Byte decode(ByteBuf p_332150_) {
            return p_332150_.readByte();
        }

        public void encode(ByteBuf p_328538_, Byte p_327835_) {
            p_328538_.writeByte(p_327835_);
        }
    };
    StreamCodec<ByteBuf, Short> SHORT = new StreamCodec<ByteBuf, Short>() {
        public Short decode(ByteBuf p_331682_) {
            return p_331682_.readShort();
        }

        public void encode(ByteBuf p_329734_, Short p_332862_) {
            p_329734_.writeShort(p_332862_);
        }
    };
    StreamCodec<ByteBuf, Integer> UNSIGNED_SHORT = new StreamCodec<ByteBuf, Integer>() {
        public Integer decode(ByteBuf p_333416_) {
            return p_333416_.readUnsignedShort();
        }

        public void encode(ByteBuf p_334768_, Integer p_335195_) {
            p_334768_.writeShort(p_335195_);
        }
    };
    StreamCodec<ByteBuf, Integer> INT = new StreamCodec<ByteBuf, Integer>() {
        public Integer decode(ByteBuf p_334363_) {
            return p_334363_.readInt();
        }

        public void encode(ByteBuf p_328174_, Integer p_329350_) {
            p_328174_.writeInt(p_329350_);
        }
    };
    StreamCodec<ByteBuf, Integer> VAR_INT = new StreamCodec<ByteBuf, Integer>() {
        public Integer decode(ByteBuf p_334861_) {
            return VarInt.read(p_334861_);
        }

        public void encode(ByteBuf p_333121_, Integer p_329976_) {
            VarInt.write(p_333121_, p_329976_);
        }
    };
    StreamCodec<ByteBuf, Long> VAR_LONG = new StreamCodec<ByteBuf, Long>() {
        public Long decode(ByteBuf p_330259_) {
            return VarLong.read(p_330259_);
        }

        public void encode(ByteBuf p_332625_, Long p_327681_) {
            VarLong.write(p_332625_, p_327681_);
        }
    };
    StreamCodec<ByteBuf, Float> FLOAT = new StreamCodec<ByteBuf, Float>() {
        public Float decode(ByteBuf p_335511_) {
            return p_335511_.readFloat();
        }

        public void encode(ByteBuf p_331177_, Float p_328599_) {
            p_331177_.writeFloat(p_328599_);
        }
    };
    StreamCodec<ByteBuf, Double> DOUBLE = new StreamCodec<ByteBuf, Double>() {
        public Double decode(ByteBuf p_330378_) {
            return p_330378_.readDouble();
        }

        public void encode(ByteBuf p_329698_, Double p_331112_) {
            p_329698_.writeDouble(p_331112_);
        }
    };
    StreamCodec<ByteBuf, byte[]> BYTE_ARRAY = new StreamCodec<ByteBuf, byte[]>() {
        public byte[] decode(ByteBuf p_331124_) {
            return FriendlyByteBuf.readByteArray(p_331124_);
        }

        public void encode(ByteBuf p_327898_, byte[] p_335211_) {
            FriendlyByteBuf.writeByteArray(p_327898_, p_335211_);
        }
    };
    StreamCodec<ByteBuf, String> STRING_UTF8 = stringUtf8(32767);
    StreamCodec<ByteBuf, Tag> TAG = tagCodec(() -> NbtAccounter.create(2097152L));
    StreamCodec<ByteBuf, Tag> TRUSTED_TAG = tagCodec(NbtAccounter::unlimitedHeap);
    StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = compoundTagCodec(() -> NbtAccounter.create(2097152L));
    StreamCodec<ByteBuf, CompoundTag> TRUSTED_COMPOUND_TAG = compoundTagCodec(NbtAccounter::unlimitedHeap);
    StreamCodec<ByteBuf, Optional<CompoundTag>> OPTIONAL_COMPOUND_TAG = new StreamCodec<ByteBuf, Optional<CompoundTag>>() {
        public Optional<CompoundTag> decode(ByteBuf p_330806_) {
            return Optional.ofNullable(FriendlyByteBuf.readNbt(p_330806_));
        }

        public void encode(ByteBuf p_327710_, Optional<CompoundTag> p_334595_) {
            FriendlyByteBuf.writeNbt(p_327710_, p_334595_.orElse(null));
        }
    };
    StreamCodec<ByteBuf, Vector3f> VECTOR3F = new StreamCodec<ByteBuf, Vector3f>() {
        public Vector3f decode(ByteBuf p_334787_) {
            return FriendlyByteBuf.readVector3f(p_334787_);
        }

        public void encode(ByteBuf p_332400_, Vector3f p_332259_) {
            FriendlyByteBuf.writeVector3f(p_332400_, p_332259_);
        }
    };
    StreamCodec<ByteBuf, Quaternionf> QUATERNIONF = new StreamCodec<ByteBuf, Quaternionf>() {
        public Quaternionf decode(ByteBuf p_328716_) {
            return FriendlyByteBuf.readQuaternion(p_328716_);
        }

        public void encode(ByteBuf p_327986_, Quaternionf p_330995_) {
            FriendlyByteBuf.writeQuaternion(p_327986_, p_330995_);
        }
    };
    StreamCodec<ByteBuf, PropertyMap> GAME_PROFILE_PROPERTIES = new StreamCodec<ByteBuf, PropertyMap>() {
        private static final int MAX_PROPERTY_NAME_LENGTH = 64;
        private static final int MAX_PROPERTY_VALUE_LENGTH = 32767;
        private static final int MAX_PROPERTY_SIGNATURE_LENGTH = 1024;
        private static final int MAX_PROPERTIES = 16;

        public PropertyMap decode(ByteBuf p_330056_) {
            int i = ByteBufCodecs.readCount(p_330056_, 16);
            PropertyMap propertymap = new PropertyMap();

            for (int j = 0; j < i; j++) {
                String s = Utf8String.read(p_330056_, 64);
                String s1 = Utf8String.read(p_330056_, 32767);
                String s2 = FriendlyByteBuf.readNullable(p_330056_, p_329275_ -> Utf8String.read(p_329275_, 1024));
                Property property = new Property(s, s1, s2);
                propertymap.put(property.name(), property);
            }

            return propertymap;
        }

        public void encode(ByteBuf p_335459_, PropertyMap p_330188_) {
            ByteBufCodecs.writeCount(p_335459_, p_330188_.size(), 16);

            for (Property property : p_330188_.values()) {
                Utf8String.write(p_335459_, property.name(), 64);
                Utf8String.write(p_335459_, property.value(), 32767);
                FriendlyByteBuf.writeNullable(p_335459_, property.signature(), (p_331347_, p_332838_) -> Utf8String.write(p_331347_, p_332838_, 1024));
            }
        }
    };
    StreamCodec<ByteBuf, GameProfile> GAME_PROFILE = new StreamCodec<ByteBuf, GameProfile>() {
        public GameProfile decode(ByteBuf p_327735_) {
            UUID uuid = UUIDUtil.STREAM_CODEC.decode(p_327735_);
            String s = Utf8String.read(p_327735_, 16);
            GameProfile gameprofile = new GameProfile(uuid, s);
            gameprofile.getProperties().putAll(ByteBufCodecs.GAME_PROFILE_PROPERTIES.decode(p_327735_));
            return gameprofile;
        }

        public void encode(ByteBuf p_328631_, GameProfile p_333487_) {
            UUIDUtil.STREAM_CODEC.encode(p_328631_, p_333487_.getId());
            Utf8String.write(p_328631_, p_333487_.getName(), 16);
            ByteBufCodecs.GAME_PROFILE_PROPERTIES.encode(p_328631_, p_333487_.getProperties());
        }
    };

    static StreamCodec<ByteBuf, byte[]> byteArray(final int p_329369_) {
        return new StreamCodec<ByteBuf, byte[]>() {
            public byte[] decode(ByteBuf p_331124_) {
                return FriendlyByteBuf.readByteArray(p_331124_, p_329369_);
            }

            public void encode(ByteBuf p_327898_, byte[] p_335211_) {
                if (p_335211_.length > p_329369_) {
                    throw new EncoderException("ByteArray with size " + p_335211_.length + " is bigger than allowed " + p_329369_);
                } else {
                    FriendlyByteBuf.writeByteArray(p_327898_, p_335211_);
                }
            }
        };
    }

    static StreamCodec<ByteBuf, String> stringUtf8(final int p_332577_) {
        return new StreamCodec<ByteBuf, String>() {
            public String decode(ByteBuf p_333794_) {
                return Utf8String.read(p_333794_, p_332577_);
            }

            public void encode(ByteBuf p_327981_, String p_333577_) {
                Utf8String.write(p_327981_, p_333577_, p_332577_);
            }
        };
    }

    static StreamCodec<ByteBuf, Tag> tagCodec(final Supplier<NbtAccounter> p_334674_) {
        return new StreamCodec<ByteBuf, Tag>() {
            public Tag decode(ByteBuf p_329846_) {
                Tag tag = FriendlyByteBuf.readNbt(p_329846_, p_334674_.get());
                if (tag == null) {
                    throw new DecoderException("Expected non-null compound tag");
                } else {
                    return tag;
                }
            }

            public void encode(ByteBuf p_336297_, Tag p_334056_) {
                if (p_334056_ == EndTag.INSTANCE) {
                    throw new EncoderException("Expected non-null compound tag");
                } else {
                    FriendlyByteBuf.writeNbt(p_336297_, p_334056_);
                }
            }
        };
    }

    static StreamCodec<ByteBuf, CompoundTag> compoundTagCodec(Supplier<NbtAccounter> p_334293_) {
        return tagCodec(p_334293_).map(p_329005_ -> {
            if (p_329005_ instanceof CompoundTag) {
                return (CompoundTag)p_329005_;
            } else {
                throw new DecoderException("Not a compound tag: " + p_329005_);
            }
        }, p_331817_ -> (Tag)p_331817_);
    }

    static <T> StreamCodec<ByteBuf, T> fromCodecTrusted(Codec<T> p_332454_) {
        return fromCodec(p_332454_, NbtAccounter::unlimitedHeap);
    }

    static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> p_330766_) {
        return fromCodec(p_330766_, () -> NbtAccounter.create(2097152L));
    }

    static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> p_332152_, Supplier<NbtAccounter> p_333221_) {
        return tagCodec(p_333221_)
            .map(
                p_328837_ -> p_332152_.parse(NbtOps.INSTANCE, p_328837_)
                        .getOrThrow(p_328190_ -> new DecoderException("Failed to decode: " + p_328190_ + " " + p_328837_)),
                p_329084_ -> p_332152_.encodeStart(NbtOps.INSTANCE, (T)p_329084_)
                        .getOrThrow(p_332410_ -> new EncoderException("Failed to encode: " + p_332410_ + " " + p_329084_))
            );
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistriesTrusted(Codec<T> p_331690_) {
        return fromCodecWithRegistries(p_331690_, NbtAccounter::unlimitedHeap);
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(Codec<T> p_334037_) {
        return fromCodecWithRegistries(p_334037_, () -> NbtAccounter.create(2097152L));
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(final Codec<T> p_332747_, Supplier<NbtAccounter> p_329046_) {
        final StreamCodec<ByteBuf, Tag> streamcodec = tagCodec(p_329046_);
        return new StreamCodec<RegistryFriendlyByteBuf, T>() {
            public T decode(RegistryFriendlyByteBuf p_330498_) {
                Tag tag = streamcodec.decode(p_330498_);
                RegistryOps<Tag> registryops = p_330498_.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                return p_332747_.parse(registryops, tag).getOrThrow(p_335458_ -> new DecoderException("Failed to decode: " + p_335458_ + " " + tag));
            }

            public void encode(RegistryFriendlyByteBuf p_329923_, T p_332982_) {
                RegistryOps<Tag> registryops = p_329923_.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                Tag tag = p_332747_.encodeStart(registryops, p_332982_)
                    .getOrThrow(p_335774_ -> new EncoderException("Failed to encode: " + p_335774_ + " " + p_332982_));
                streamcodec.encode(p_329923_, tag);
            }
        };
    }

    static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optional(final StreamCodec<B, V> p_333614_) {
        return new StreamCodec<B, Optional<V>>() {
            public Optional<V> decode(B p_335035_) {
                return p_335035_.readBoolean() ? Optional.of(p_333614_.decode(p_335035_)) : Optional.empty();
            }

            public void encode(B p_328446_, Optional<V> p_334635_) {
                if (p_334635_.isPresent()) {
                    p_328446_.writeBoolean(true);
                    p_333614_.encode(p_328446_, p_334635_.get());
                } else {
                    p_328446_.writeBoolean(false);
                }
            }
        };
    }

    static int readCount(ByteBuf p_335948_, int p_329745_) {
        int i = VarInt.read(p_335948_);
        if (i > p_329745_) {
            throw new DecoderException(i + " elements exceeded max size of: " + p_329745_);
        } else {
            return i;
        }
    }

    static void writeCount(ByteBuf p_332743_, int p_332779_, int p_330804_) {
        if (p_332779_ > p_330804_) {
            throw new EncoderException(p_332779_ + " elements exceeded max size of: " + p_330804_);
        } else {
            VarInt.write(p_332743_, p_332779_);
        }
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(IntFunction<C> p_329603_, StreamCodec<? super B, V> p_335274_) {
        return collection(p_329603_, p_335274_, Integer.MAX_VALUE);
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(
        final IntFunction<C> p_330282_, final StreamCodec<? super B, V> p_329504_, final int p_331395_
    ) {
        return new StreamCodec<B, C>() {
            public C decode(B p_331156_) {
                int i = ByteBufCodecs.readCount(p_331156_, p_331395_);
                C c = p_330282_.apply(Math.min(i, 65536));

                for (int j = 0; j < i; j++) {
                    c.add(p_329504_.decode(p_331156_));
                }

                return c;
            }

            public void encode(B p_328803_, C p_335908_) {
                ByteBufCodecs.writeCount(p_328803_, p_335908_.size(), p_331395_);

                for (V v : p_335908_) {
                    p_329504_.encode(p_328803_, v);
                }
            }
        };
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec.CodecOperation<B, V, C> collection(IntFunction<C> p_333333_) {
        return p_331526_ -> collection(p_333333_, p_331526_);
    }

    static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list() {
        return p_331787_ -> collection(ArrayList::new, p_331787_);
    }

    static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list(int p_331728_) {
        return p_328420_ -> collection(ArrayList::new, p_328420_, p_331728_);
    }

    static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
        IntFunction<? extends M> p_329613_, StreamCodec<? super B, K> p_335749_, StreamCodec<? super B, V> p_332695_
    ) {
        return map(p_329613_, p_335749_, p_332695_, Integer.MAX_VALUE);
    }

    static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
        final IntFunction<? extends M> p_331225_, final StreamCodec<? super B, K> p_334555_, final StreamCodec<? super B, V> p_330391_, final int p_331122_
    ) {
        return new StreamCodec<B, M>() {
            public void encode(B p_335209_, M p_328192_) {
                ByteBufCodecs.writeCount(p_335209_, p_328192_.size(), p_331122_);
                p_328192_.forEach((p_328033_, p_330347_) -> {
                    p_334555_.encode(p_335209_, (K)p_328033_);
                    p_330391_.encode(p_335209_, (V)p_330347_);
                });
            }

            public M decode(B p_329844_) {
                int i = ByteBufCodecs.readCount(p_329844_, p_331122_);
                M m = (M)p_331225_.apply(Math.min(i, 65536));

                for (int j = 0; j < i; j++) {
                    K k = p_334555_.decode(p_329844_);
                    V v = p_330391_.decode(p_329844_);
                    m.put(k, v);
                }

                return m;
            }
        };
    }

    static <B extends ByteBuf, L, R> StreamCodec<B, Either<L, R>> either(
        final StreamCodec<? super B, L> p_330563_, final StreamCodec<? super B, R> p_328664_
    ) {
        return new StreamCodec<B, Either<L, R>>() {
            public Either<L, R> decode(B p_336330_) {
                return p_336330_.readBoolean() ? Either.left(p_330563_.decode(p_336330_)) : Either.right(p_328664_.decode(p_336330_));
            }

            public void encode(B p_329166_, Either<L, R> p_329568_) {
                p_329568_.ifLeft(p_331103_ -> {
                    p_329166_.writeBoolean(true);
                    p_330563_.encode(p_329166_, (L)p_331103_);
                }).ifRight(p_330033_ -> {
                    p_329166_.writeBoolean(false);
                    p_328664_.encode(p_329166_, (R)p_330033_);
                });
            }
        };
    }

    static <T> StreamCodec<ByteBuf, T> idMapper(final IntFunction<T> p_333433_, final ToIntFunction<T> p_334959_) {
        return new StreamCodec<ByteBuf, T>() {
            public T decode(ByteBuf p_328010_) {
                int i = VarInt.read(p_328010_);
                return p_333433_.apply(i);
            }

            public void encode(ByteBuf p_335266_, T p_330920_) {
                int i = p_334959_.applyAsInt(p_330920_);
                VarInt.write(p_335266_, i);
            }
        };
    }

    static <T> StreamCodec<ByteBuf, T> idMapper(IdMap<T> p_332036_) {
        return idMapper(p_332036_::byIdOrThrow, p_332036_::getIdOrThrow);
    }

    private static <T, R> StreamCodec<RegistryFriendlyByteBuf, R> registry(
        final ResourceKey<? extends Registry<T>> p_332046_, final Function<Registry<T>, IdMap<R>> p_332827_
    ) {
        return new StreamCodec<RegistryFriendlyByteBuf, R>() {
            private IdMap<R> getRegistryOrThrow(RegistryFriendlyByteBuf p_336378_) {
                return p_332827_.apply(p_336378_.registryAccess().registryOrThrow(p_332046_));
            }

            public R decode(RegistryFriendlyByteBuf p_328896_) {
                int i = VarInt.read(p_328896_);
                return (R)this.getRegistryOrThrow(p_328896_).byIdOrThrow(i);
            }

            public void encode(RegistryFriendlyByteBuf p_335592_, R p_330248_) {
                int i = this.getRegistryOrThrow(p_335592_).getIdOrThrow(p_330248_);
                VarInt.write(p_335592_, i);
            }
        };
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> registry(ResourceKey<? extends Registry<T>> p_332712_) {
        return registry(p_332712_, p_335792_ -> p_335792_);
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderRegistry(ResourceKey<? extends Registry<T>> p_332639_) {
        return registry(p_332639_, Registry::asHolderIdMap);
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holder(
        final ResourceKey<? extends Registry<T>> p_335347_, final StreamCodec<? super RegistryFriendlyByteBuf, T> p_329304_
    ) {
        return new StreamCodec<RegistryFriendlyByteBuf, Holder<T>>() {
            private static final int DIRECT_HOLDER_ID = 0;

            private IdMap<Holder<T>> getRegistryOrThrow(RegistryFriendlyByteBuf p_335253_) {
                return p_335253_.registryAccess().registryOrThrow(p_335347_).asHolderIdMap();
            }

            public Holder<T> decode(RegistryFriendlyByteBuf p_333302_) {
                int i = VarInt.read(p_333302_);
                return i == 0 ? Holder.direct(p_329304_.decode(p_333302_)) : (Holder)this.getRegistryOrThrow(p_333302_).byIdOrThrow(i - 1);
            }

            public void encode(RegistryFriendlyByteBuf p_333309_, Holder<T> p_334420_) {
                switch (p_334420_.kind()) {
                    case REFERENCE:
                        int i = this.getRegistryOrThrow(p_333309_).getIdOrThrow(p_334420_);
                        VarInt.write(p_333309_, i + 1);
                        break;
                    case DIRECT:
                        VarInt.write(p_333309_, 0);
                        p_329304_.encode(p_333309_, p_334420_.value());
                }
            }
        };
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>> holderSet(final ResourceKey<? extends Registry<T>> p_328506_) {
        return new StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>>() {
            private static final int NAMED_SET = -1;
            private final StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderCodec = ByteBufCodecs.holderRegistry(p_328506_);

            public HolderSet<T> decode(RegistryFriendlyByteBuf p_327957_) {
                int i = VarInt.read(p_327957_) - 1;
                if (i == -1) {
                    Registry<T> registry = p_327957_.registryAccess().registryOrThrow(p_328506_);
                    return registry.getTag(TagKey.create(p_328506_, ResourceLocation.STREAM_CODEC.decode(p_327957_))).orElseThrow();
                } else {
                    List<Holder<T>> list = new ArrayList<>(Math.min(i, 65536));

                    for (int j = 0; j < i; j++) {
                        list.add(this.holderCodec.decode(p_327957_));
                    }

                    return HolderSet.direct(list);
                }
            }

            public void encode(RegistryFriendlyByteBuf p_327905_, HolderSet<T> p_331611_) {
                Optional<TagKey<T>> optional = p_331611_.unwrapKey();
                if (optional.isPresent()) {
                    VarInt.write(p_327905_, 0);
                    ResourceLocation.STREAM_CODEC.encode(p_327905_, optional.get().location());
                } else {
                    VarInt.write(p_327905_, p_331611_.size() + 1);

                    for (Holder<T> holder : p_331611_) {
                        this.holderCodec.encode(p_327905_, holder);
                    }
                }
            }
        };
    }
}
