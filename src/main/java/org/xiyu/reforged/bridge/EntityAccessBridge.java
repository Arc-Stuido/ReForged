package org.xiyu.reforged.bridge;

import net.minecraft.world.entity.Entity;

import java.lang.reflect.Field;

/**
 * Small access bridge for NeoForge mods compiled with widened Minecraft fields.
 */
public final class EntityAccessBridge {
    private static volatile Field boardingCooldownField;

    private EntityAccessBridge() {}

    public static void setBoardingCooldown(Entity entity, int cooldown) {
        if (entity == null) return;
        try {
            Field field = boardingCooldownField;
            if (field == null) {
                field = Entity.class.getDeclaredField("boardingCooldown");
                field.setAccessible(true);
                boardingCooldownField = field;
            }
            field.setInt(entity, cooldown);
        } catch (Throwable ignored) {
            // Non-critical cooldown; keep the vehicle class verifiable even if reflection is denied.
        }
    }
}
