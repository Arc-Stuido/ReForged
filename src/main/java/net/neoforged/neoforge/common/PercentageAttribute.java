package net.neoforged.neoforge.common;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * A percentage attribute where modifiers display as percentages.
 */
public class PercentageAttribute extends RangedAttribute {
    public PercentageAttribute(String descriptionId, double defaultValue, double min, double max) {
        super(descriptionId, defaultValue, min, max);
    }
}
