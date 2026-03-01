package net.minecraft.world.entity;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

public enum EquipmentSlot implements StringRepresentable {
    MAINHAND(EquipmentSlot.Type.HAND, 0, 0, "mainhand"),
    OFFHAND(EquipmentSlot.Type.HAND, 1, 5, "offhand"),
    FEET(EquipmentSlot.Type.HUMANOID_ARMOR, 0, 1, 1, "feet"),
    LEGS(EquipmentSlot.Type.HUMANOID_ARMOR, 1, 1, 2, "legs"),
    CHEST(EquipmentSlot.Type.HUMANOID_ARMOR, 2, 1, 3, "chest"),
    HEAD(EquipmentSlot.Type.HUMANOID_ARMOR, 3, 1, 4, "head"),
    BODY(EquipmentSlot.Type.ANIMAL_ARMOR, 0, 1, 6, "body");

    public static final int NO_COUNT_LIMIT = 0;
    public static final StringRepresentable.EnumCodec<EquipmentSlot> CODEC = StringRepresentable.fromEnum(EquipmentSlot::values);
    private final EquipmentSlot.Type type;
    private final int index;
    private final int countLimit;
    private final int filterFlag;
    private final String name;

    private EquipmentSlot(final EquipmentSlot.Type p_342397_, final int p_343935_, final int p_343943_, final int p_345166_, final String p_345481_) {
        this.type = p_342397_;
        this.index = p_343935_;
        this.countLimit = p_343943_;
        this.filterFlag = p_345166_;
        this.name = p_345481_;
    }

    private EquipmentSlot(final EquipmentSlot.Type p_20739_, final int p_20740_, final int p_20741_, final String p_20742_) {
        this(p_20739_, p_20740_, 0, p_20741_, p_20742_);
    }

    public EquipmentSlot.Type getType() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }

    public int getIndex(int p_147069_) {
        return p_147069_ + this.index;
    }

    public ItemStack limit(ItemStack p_343527_) {
        return this.countLimit > 0 ? p_343527_.split(this.countLimit) : p_343527_;
    }

    public int getFilterFlag() {
        return this.filterFlag;
    }

    public String getName() {
        return this.name;
    }

    public boolean isArmor() {
        return this.type == EquipmentSlot.Type.HUMANOID_ARMOR || this.type == EquipmentSlot.Type.ANIMAL_ARMOR;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static EquipmentSlot byName(String p_20748_) {
        EquipmentSlot equipmentslot = CODEC.byName(p_20748_);
        if (equipmentslot != null) {
            return equipmentslot;
        } else {
            throw new IllegalArgumentException("Invalid slot '" + p_20748_ + "'");
        }
    }

    public static enum Type {
        HAND,
        HUMANOID_ARMOR,
        ANIMAL_ARMOR;
    }
}