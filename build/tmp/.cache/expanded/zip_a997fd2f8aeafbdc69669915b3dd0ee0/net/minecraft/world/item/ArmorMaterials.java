package net.minecraft.world.item;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.crafting.Ingredient;

public class ArmorMaterials {
    public static final Holder<ArmorMaterial> LEATHER = register(
        "leather",
        Util.make(new EnumMap<>(ArmorItem.Type.class), p_327101_ -> {
            p_327101_.put(ArmorItem.Type.BOOTS, 1);
            p_327101_.put(ArmorItem.Type.LEGGINGS, 2);
            p_327101_.put(ArmorItem.Type.CHESTPLATE, 3);
            p_327101_.put(ArmorItem.Type.HELMET, 1);
            p_327101_.put(ArmorItem.Type.BODY, 3);
        }),
        15,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        0.0F,
        0.0F,
        () -> Ingredient.of(Items.LEATHER),
        List.of(
            new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("leather"), "", true),
            new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("leather"), "_overlay", false)
        )
    );
    public static final Holder<ArmorMaterial> CHAIN = register("chainmail", Util.make(new EnumMap<>(ArmorItem.Type.class), p_327098_ -> {
        p_327098_.put(ArmorItem.Type.BOOTS, 1);
        p_327098_.put(ArmorItem.Type.LEGGINGS, 4);
        p_327098_.put(ArmorItem.Type.CHESTPLATE, 5);
        p_327098_.put(ArmorItem.Type.HELMET, 2);
        p_327098_.put(ArmorItem.Type.BODY, 4);
    }), 12, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT));
    public static final Holder<ArmorMaterial> IRON = register("iron", Util.make(new EnumMap<>(ArmorItem.Type.class), p_327096_ -> {
        p_327096_.put(ArmorItem.Type.BOOTS, 2);
        p_327096_.put(ArmorItem.Type.LEGGINGS, 5);
        p_327096_.put(ArmorItem.Type.CHESTPLATE, 6);
        p_327096_.put(ArmorItem.Type.HELMET, 2);
        p_327096_.put(ArmorItem.Type.BODY, 5);
    }), 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT));
    public static final Holder<ArmorMaterial> GOLD = register("gold", Util.make(new EnumMap<>(ArmorItem.Type.class), p_327100_ -> {
        p_327100_.put(ArmorItem.Type.BOOTS, 1);
        p_327100_.put(ArmorItem.Type.LEGGINGS, 3);
        p_327100_.put(ArmorItem.Type.CHESTPLATE, 5);
        p_327100_.put(ArmorItem.Type.HELMET, 2);
        p_327100_.put(ArmorItem.Type.BODY, 7);
    }), 25, SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, () -> Ingredient.of(Items.GOLD_INGOT));
    public static final Holder<ArmorMaterial> DIAMOND = register("diamond", Util.make(new EnumMap<>(ArmorItem.Type.class), p_327102_ -> {
        p_327102_.put(ArmorItem.Type.BOOTS, 3);
        p_327102_.put(ArmorItem.Type.LEGGINGS, 6);
        p_327102_.put(ArmorItem.Type.CHESTPLATE, 8);
        p_327102_.put(ArmorItem.Type.HELMET, 3);
        p_327102_.put(ArmorItem.Type.BODY, 11);
    }), 10, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, () -> Ingredient.of(Items.DIAMOND));
    public static final Holder<ArmorMaterial> TURTLE = register("turtle", Util.make(new EnumMap<>(ArmorItem.Type.class), p_327099_ -> {
        p_327099_.put(ArmorItem.Type.BOOTS, 2);
        p_327099_.put(ArmorItem.Type.LEGGINGS, 5);
        p_327099_.put(ArmorItem.Type.CHESTPLATE, 6);
        p_327099_.put(ArmorItem.Type.HELMET, 2);
        p_327099_.put(ArmorItem.Type.BODY, 5);
    }), 9, SoundEvents.ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, () -> Ingredient.of(Items.TURTLE_SCUTE));
    public static final Holder<ArmorMaterial> NETHERITE = register("netherite", Util.make(new EnumMap<>(ArmorItem.Type.class), p_327103_ -> {
        p_327103_.put(ArmorItem.Type.BOOTS, 3);
        p_327103_.put(ArmorItem.Type.LEGGINGS, 6);
        p_327103_.put(ArmorItem.Type.CHESTPLATE, 8);
        p_327103_.put(ArmorItem.Type.HELMET, 3);
        p_327103_.put(ArmorItem.Type.BODY, 11);
    }), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, () -> Ingredient.of(Items.NETHERITE_INGOT));
    public static final Holder<ArmorMaterial> ARMADILLO = register("armadillo", Util.make(new EnumMap<>(ArmorItem.Type.class), p_327097_ -> {
        p_327097_.put(ArmorItem.Type.BOOTS, 3);
        p_327097_.put(ArmorItem.Type.LEGGINGS, 6);
        p_327097_.put(ArmorItem.Type.CHESTPLATE, 8);
        p_327097_.put(ArmorItem.Type.HELMET, 3);
        p_327097_.put(ArmorItem.Type.BODY, 11);
    }), 10, SoundEvents.ARMOR_EQUIP_WOLF, 0.0F, 0.0F, () -> Ingredient.of(Items.ARMADILLO_SCUTE));

    public static Holder<ArmorMaterial> bootstrap(Registry<ArmorMaterial> p_332591_) {
        return LEATHER;
    }

    private static Holder<ArmorMaterial> register(
        String p_334359_,
        EnumMap<ArmorItem.Type, Integer> p_329993_,
        int p_332696_,
        Holder<SoundEvent> p_333975_,
        float p_329381_,
        float p_334853_,
        Supplier<Ingredient> p_333678_
    ) {
        List<ArmorMaterial.Layer> list = List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace(p_334359_)));
        return register(p_334359_, p_329993_, p_332696_, p_333975_, p_329381_, p_334853_, p_333678_, list);
    }

    private static Holder<ArmorMaterial> register(
        String p_332406_,
        EnumMap<ArmorItem.Type, Integer> p_331524_,
        int p_331490_,
        Holder<SoundEvent> p_331648_,
        float p_327988_,
        float p_328616_,
        Supplier<Ingredient> p_334412_,
        List<ArmorMaterial.Layer> p_330855_
    ) {
        EnumMap<ArmorItem.Type, Integer> enummap = new EnumMap<>(ArmorItem.Type.class);

        for (ArmorItem.Type armoritem$type : ArmorItem.Type.values()) {
            enummap.put(armoritem$type, p_331524_.get(armoritem$type));
        }

        return Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.withDefaultNamespace(p_332406_),
            new ArmorMaterial(enummap, p_331490_, p_331648_, p_334412_, p_330855_, p_327988_, p_328616_)
        );
    }
}