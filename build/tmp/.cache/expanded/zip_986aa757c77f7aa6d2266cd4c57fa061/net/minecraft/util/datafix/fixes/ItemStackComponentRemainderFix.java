package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;

public abstract class ItemStackComponentRemainderFix extends DataFix {
    private final String name;
    private final String componentId;
    private final String newComponentId;

    public ItemStackComponentRemainderFix(Schema p_334140_, String p_330429_, String p_335879_) {
        this(p_334140_, p_330429_, p_335879_, p_335879_);
    }

    public ItemStackComponentRemainderFix(Schema p_330360_, String p_329958_, String p_335490_, String p_335902_) {
        super(p_330360_, false);
        this.name = p_329958_;
        this.componentId = p_335490_;
        this.newComponentId = p_335902_;
    }

    @Override
    public final TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<?> opticfinder = type.findField("components");
        return this.fixTypeEverywhereTyped(
            this.name,
            type,
            p_329992_ -> p_329992_.updateTyped(
                    opticfinder,
                    p_332858_ -> p_332858_.update(
                            DSL.remainderFinder(), p_330335_ -> p_330335_.renameAndFixField(this.componentId, this.newComponentId, this::fixComponent)
                        )
                )
        );
    }

    protected abstract <T> Dynamic<T> fixComponent(Dynamic<T> p_330625_);
}