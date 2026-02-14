package net.neoforged.neoforge.entity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;

/**
 * NeoForge PartEntity shim — mirrors Forge's {@link net.minecraftforge.entity.PartEntity}.
 * <p>
 * Must extend {@link Entity} so that the JVM verifier accepts NeoForge mod classes
 * (e.g. Twilight Forest's SpikeBlock) wherever an Entity reference is expected.
 * </p>
 */
public abstract class PartEntity<T extends Entity> extends Entity {

    private final T parent;

    public PartEntity(T parent) {
        super(parent.getType(), parent.level());
        this.parent = parent;
    }

    public T getParent() {
        return parent;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        throw new UnsupportedOperationException();
    }
}
