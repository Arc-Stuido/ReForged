package net.neoforged.neoforge.common;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * A boolean attribute that is either 0 or 1.
 */
public class BooleanAttribute extends RangedAttribute {
    public BooleanAttribute(String descriptionId, double defaultValue, double min, double max) {
        super(descriptionId, defaultValue, min, max);
    }
}
