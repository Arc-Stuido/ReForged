package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetCustomModelDataFunction extends LootItemConditionalFunction {
    static final MapCodec<SetCustomModelDataFunction> CODEC = RecordCodecBuilder.mapCodec(
        p_332110_ -> commonFields(p_332110_)
                .and(NumberProviders.CODEC.fieldOf("value").forGetter(p_334766_ -> p_334766_.valueProvider))
                .apply(p_332110_, SetCustomModelDataFunction::new)
    );
    private final NumberProvider valueProvider;

    private SetCustomModelDataFunction(List<LootItemCondition> p_335890_, NumberProvider p_333004_) {
        super(p_335890_);
        this.valueProvider = p_333004_;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.valueProvider.getReferencedContextParams();
    }

    @Override
    public LootItemFunctionType<SetCustomModelDataFunction> getType() {
        return LootItemFunctions.SET_CUSTOM_MODEL_DATA;
    }

    @Override
    public ItemStack run(ItemStack p_328099_, LootContext p_333702_) {
        p_328099_.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(this.valueProvider.getInt(p_333702_)));
        return p_328099_;
    }
}