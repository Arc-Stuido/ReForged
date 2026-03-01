package org.xiyu.reforged.shim.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PayloadRegistrar — Shim for NeoForge's network payload registration system.
 *
 * <h3>NeoForge API</h3>
 * <pre>
 * public void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
 *     PayloadRegistrar registrar = event.registrar("1");
 *     registrar.playToServer(MyPayload.TYPE, MyPayload.STREAM_CODEC, MyPayloadHandler::handle);
 * }
 * </pre>
 *
 * <h3>Forge Bridging</h3>
 * <p>Each registered payload is bridged to a Forge {@code SimpleChannel} message via
 * {@link PayloadChannelRegistry}. The NeoForge StreamCodec is adapted to Forge's
 * encoder/decoder pattern, and handlers are wrapped with a bridged {@link ReForgedPayloadContext}.</p>
 */
public final class PayloadRegistrar {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Track all payload registrations for debugging */
    private static final Map<String, PayloadRegistrar> REGISTRARS = new ConcurrentHashMap<>();

    private final String version;
    private boolean optional;

    public PayloadRegistrar(String version) {
        this.version = version;
        this.optional = false;
        REGISTRARS.put(version, this);
        LOGGER.debug("[ReForged] PayloadRegistrar created for version '{}'", version);
    }

    private PayloadRegistrar(PayloadRegistrar source) {
        this.version = source.version;
        this.optional = source.optional;
    }

    // ---- Play Phase ----

    /**
     * Register a payload to be sent from client to server (play phase).
     */
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> PayloadRegistrar playToServer(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.playToServer: type={}, version={}", type.id(), version);
        PayloadChannelRegistry.registerPayload(type, (StreamCodec) codec, handler, PacketFlow.SERVERBOUND);
        return this;
    }

    /**
     * Register a payload to be sent from server to client (play phase).
     */
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> PayloadRegistrar playToClient(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.playToClient: type={}, version={}", type.id(), version);
        PayloadChannelRegistry.registerPayload(type, (StreamCodec) codec, handler, PacketFlow.CLIENTBOUND);
        return this;
    }

    /**
     * Register a bidirectional payload (play phase).
     */
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> PayloadRegistrar playBidirectional(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.playBidirectional: type={}, version={}", type.id(), version);
        PayloadChannelRegistry.registerPayload(type, (StreamCodec) codec, handler, null);
        return this;
    }

    // ---- Configuration Phase ----

    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> PayloadRegistrar configurationToServer(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super FriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.configurationToServer: type={}", type.id());
        PayloadChannelRegistry.registerPayload(type, codec, handler, PacketFlow.SERVERBOUND);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> PayloadRegistrar configurationToClient(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super FriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.configurationToClient: type={}", type.id());
        PayloadChannelRegistry.registerPayload(type, codec, handler, PacketFlow.CLIENTBOUND);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> PayloadRegistrar configurationBidirectional(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super FriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.configurationBidirectional: type={}", type.id());
        PayloadChannelRegistry.registerPayload(type, codec, handler, null);
        return this;
    }

    // ---- Common (Play + Configuration) ----

    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> PayloadRegistrar commonToServer(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super FriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.commonToServer: type={}", type.id());
        PayloadChannelRegistry.registerPayload(type, codec, handler, PacketFlow.SERVERBOUND);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> PayloadRegistrar commonToClient(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super FriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.commonToClient: type={}", type.id());
        PayloadChannelRegistry.registerPayload(type, codec, handler, PacketFlow.CLIENTBOUND);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> PayloadRegistrar commonBidirectional(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super FriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.commonBidirectional: type={}", type.id());
        PayloadChannelRegistry.registerPayload(type, codec, handler, null);
        return this;
    }

    // ---- Fallback overloads accepting Object (for ASM-rewritten code with erased types) ----

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> PayloadRegistrar playToServer(Object type, Object streamCodec, Object handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.playToServer (Object): type={}, version={}", type, version);
        if (type instanceof CustomPacketPayload.Type<?> t && streamCodec instanceof StreamCodec sc && handler instanceof IPayloadHandler h) {
            PayloadChannelRegistry.registerPayload((CustomPacketPayload.Type) t, sc, h, PacketFlow.SERVERBOUND);
        } else {
            LOGGER.warn("[ReForged] PayloadRegistrar.playToServer: unrecognized types — type={}, codec={}, handler={}", 
                    type != null ? type.getClass() : null, 
                    streamCodec != null ? streamCodec.getClass() : null,
                    handler != null ? handler.getClass() : null);
        }
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> PayloadRegistrar playToClient(Object type, Object streamCodec, Object handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.playToClient (Object): type={}, version={}", type, version);
        if (type instanceof CustomPacketPayload.Type<?> t && streamCodec instanceof StreamCodec sc && handler instanceof IPayloadHandler h) {
            PayloadChannelRegistry.registerPayload((CustomPacketPayload.Type) t, sc, h, PacketFlow.CLIENTBOUND);
        } else {
            LOGGER.warn("[ReForged] PayloadRegistrar.playToClient: unrecognized types");
        }
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> PayloadRegistrar playBidirectional(Object type, Object streamCodec, Object handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.playBidirectional (Object): type={}, version={}", type, version);
        if (type instanceof CustomPacketPayload.Type<?> t && streamCodec instanceof StreamCodec sc && handler instanceof IPayloadHandler h) {
            PayloadChannelRegistry.registerPayload((CustomPacketPayload.Type) t, sc, h, null);
        } else {
            LOGGER.warn("[ReForged] PayloadRegistrar.playBidirectional: unrecognized types");
        }
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> PayloadRegistrar configurationToServer(Object type, Object streamCodec, Object handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.configurationToServer (Object): type={}", type);
        if (type instanceof CustomPacketPayload.Type<?> t && streamCodec instanceof StreamCodec sc && handler instanceof IPayloadHandler h) {
            PayloadChannelRegistry.registerPayload((CustomPacketPayload.Type) t, sc, h, PacketFlow.SERVERBOUND);
        }
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> PayloadRegistrar configurationToClient(Object type, Object streamCodec, Object handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.configurationToClient (Object): type={}", type);
        if (type instanceof CustomPacketPayload.Type<?> t && streamCodec instanceof StreamCodec sc && handler instanceof IPayloadHandler h) {
            PayloadChannelRegistry.registerPayload((CustomPacketPayload.Type) t, sc, h, PacketFlow.CLIENTBOUND);
        }
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> PayloadRegistrar configurationBidirectional(Object type, Object streamCodec, Object handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.configurationBidirectional (Object): type={}", type);
        if (type instanceof CustomPacketPayload.Type<?> t && streamCodec instanceof StreamCodec sc && handler instanceof IPayloadHandler h) {
            PayloadChannelRegistry.registerPayload((CustomPacketPayload.Type) t, sc, h, null);
        }
        return this;
    }

    // ---- Modifiers ----

    /**
     * Mark payloads registered with this registrar as optional.
     */
    public PayloadRegistrar optional() {
        PayloadRegistrar clone = new PayloadRegistrar(this);
        clone.optional = true;
        LOGGER.debug("[ReForged] PayloadRegistrar optional set for version '{}'", version);
        return clone;
    }

    /**
     * Returns a new registrar with a different version.
     */
    public PayloadRegistrar versioned(String version) {
        return new PayloadRegistrar(version);
    }

    /**
     * Returns a new registrar that executes handlers on a specific thread.
     * In Forge, we always use consumerMainThread, so this is a no-op.
     */
    public PayloadRegistrar executesOn(Object thread) {
        LOGGER.debug("[ReForged] PayloadRegistrar.executesOn() called (no-op on Forge)");
        return this;
    }

    public String getVersion() {
        return version;
    }
}
