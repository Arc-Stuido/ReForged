package org.xiyu.reforged.bridge;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ConditionalOps;

/**
 * Hosts NeoForge-only recipe fields that cannot be added to vanilla/Forge classes.
 */
public final class RecipeBridge {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final Codec CONDITIONAL_CODEC = ConditionalOps.createConditionalCodecWithConditions(Recipe.CODEC);

    private RecipeBridge() {
    }
}
