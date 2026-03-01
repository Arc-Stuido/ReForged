package net.minecraft.world.item;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class AnimalArmorItem extends ArmorItem {
    private final ResourceLocation textureLocation;
    @Nullable
    private final ResourceLocation overlayTextureLocation;
    private final AnimalArmorItem.BodyType bodyType;

    public AnimalArmorItem(Holder<ArmorMaterial> p_329749_, AnimalArmorItem.BodyType p_330915_, boolean p_329552_, Item.Properties p_333708_) {
        super(p_329749_, ArmorItem.Type.BODY, p_333708_);
        this.bodyType = p_330915_;
        ResourceLocation resourcelocation = p_330915_.textureLocator.apply(p_329749_.unwrapKey().orElseThrow().location());
        this.textureLocation = resourcelocation.withSuffix(".png");
        if (p_329552_) {
            this.overlayTextureLocation = resourcelocation.withSuffix("_overlay.png");
        } else {
            this.overlayTextureLocation = null;
        }
    }

    public ResourceLocation getTexture() {
        return this.textureLocation;
    }

    @Nullable
    public ResourceLocation getOverlayTexture() {
        return this.overlayTextureLocation;
    }

    public AnimalArmorItem.BodyType getBodyType() {
        return this.bodyType;
    }

    @Override
    public SoundEvent getBreakingSound() {
        return this.bodyType.breakingSound;
    }

    @Override
    public boolean isEnchantable(ItemStack p_329133_) {
        return false;
    }

    public static enum BodyType implements net.minecraftforge.common.IExtensibleEnum {
        EQUESTRIAN(p_331659_ -> p_331659_.withPath(p_329177_ -> "textures/entity/horse/armor/horse_armor_" + p_329177_), SoundEvents.ITEM_BREAK),
        CANINE(p_333424_ -> p_333424_.withPath("textures/entity/wolf/wolf_armor"), SoundEvents.WOLF_ARMOR_BREAK);

        final Function<ResourceLocation, ResourceLocation> textureLocator;
        final SoundEvent breakingSound;

        private BodyType(final Function<ResourceLocation, ResourceLocation> p_332420_, final SoundEvent p_335661_) {
            this.textureLocator = p_332420_;
            this.breakingSound = p_335661_;
        }

        public static BodyType create(String name, final Function<ResourceLocation, ResourceLocation> path, SoundEvent sound) {
            throw new IllegalStateException("Enum not extended");
        }
    }
}
