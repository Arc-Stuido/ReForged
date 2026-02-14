package net.neoforged.neoforge.common;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.function.Supplier;

/**
 * NeoForge's DeferredSpawnEggItem — accepts a Supplier for the EntityType
 * so the type can be resolved lazily (during registry events).
 */
public class DeferredSpawnEggItem extends net.minecraft.world.item.SpawnEggItem {

    private final Supplier<? extends EntityType<? extends Mob>> typeSupplier;

    /**
     * NeoForge-style constructor: Supplier-based lazy EntityType resolution.
     */
    @SuppressWarnings("DataFlowIssue")
    public DeferredSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type,
                                int backgroundColor, int highlightColor, Properties props) {
        super((EntityType<? extends Mob>) null, backgroundColor, highlightColor, props);
        this.typeSupplier = type;
    }

    /**
     * Direct EntityType constructor (Forge compatibility).
     */
    public DeferredSpawnEggItem(EntityType<? extends Mob> type,
                                int backgroundColor, int highlightColor, Properties props) {
        super(type, backgroundColor, highlightColor, props);
        this.typeSupplier = () -> type;
    }
}
