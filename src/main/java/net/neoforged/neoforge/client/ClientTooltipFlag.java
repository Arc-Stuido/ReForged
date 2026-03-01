package net.neoforged.neoforge.client;

import net.minecraft.world.item.TooltipFlag;

/**
 * Stub: Extended tooltip flag with creative tab context.
 */
public class ClientTooltipFlag implements TooltipFlag {
    private final boolean advanced;

    public ClientTooltipFlag(boolean advanced) {
        this.advanced = advanced;
    }

    @Override
    public boolean isAdvanced() {
        return advanced;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
