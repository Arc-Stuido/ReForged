package net.minecraft.client.resources.model;

import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ModelResourceLocation(ResourceLocation id, String variant) {
    public static final String INVENTORY_VARIANT = "inventory";

    public ModelResourceLocation(ResourceLocation id, String variant) {
        variant = lowercaseVariant(variant);
        this.id = id;
        this.variant = variant;
    }

    public static ModelResourceLocation vanilla(String p_251132_, String p_248987_) {
        return new ModelResourceLocation(ResourceLocation.withDefaultNamespace(p_251132_), p_248987_);
    }

    public static ModelResourceLocation inventory(ResourceLocation p_343915_) {
        return new ModelResourceLocation(p_343915_, "inventory");
    }

    private static String lowercaseVariant(String p_248567_) {
        return p_248567_.toLowerCase(Locale.ROOT);
    }

    public String getVariant() {
        return this.variant;
    }

    @Override
    public String toString() {
        return this.id + "#" + this.variant;
    }
}