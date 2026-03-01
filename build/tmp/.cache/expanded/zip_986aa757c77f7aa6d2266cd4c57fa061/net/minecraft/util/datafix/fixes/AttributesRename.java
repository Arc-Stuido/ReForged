package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class AttributesRename extends DataFix {
    private final String name;
    private final UnaryOperator<String> renames;

    public AttributesRename(Schema p_14671_, String p_335868_, UnaryOperator<String> p_329441_) {
        super(p_14671_, false);
        this.name = p_335868_;
        this.renames = p_329441_;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<?> opticfinder = type.findField("tag");
        return TypeRewriteRule.seq(
            this.fixTypeEverywhereTyped(this.name + " (ItemStack)", type, p_326552_ -> p_326552_.updateTyped(opticfinder, this::fixItemStackTag)),
            this.fixTypeEverywhereTyped(this.name + " (Entity)", this.getInputSchema().getType(References.ENTITY), this::fixEntity),
            this.fixTypeEverywhereTyped(this.name + " (Player)", this.getInputSchema().getType(References.PLAYER), this::fixEntity)
        );
    }

    private Dynamic<?> fixName(Dynamic<?> p_14678_) {
        return DataFixUtils.orElse(p_14678_.asString().result().map(this.renames).map(p_14678_::createString), p_14678_);
    }

    private Typed<?> fixItemStackTag(Typed<?> p_14676_) {
        return p_14676_.update(
            DSL.remainderFinder(),
            p_326547_ -> p_326547_.update(
                    "AttributeModifiers",
                    p_326550_ -> DataFixUtils.orElse(
                            p_326550_.asStreamOpt()
                                .result()
                                .map(p_326549_ -> p_326549_.map(p_326544_ -> p_326544_.update("AttributeName", this::fixName)))
                                .map(p_326550_::createList),
                            p_326550_
                        )
                )
        );
    }

    private Typed<?> fixEntity(Typed<?> p_14684_) {
        return p_14684_.update(
            DSL.remainderFinder(),
            p_326543_ -> p_326543_.update(
                    "Attributes",
                    p_326546_ -> DataFixUtils.orElse(
                            p_326546_.asStreamOpt()
                                .result()
                                .map(p_326548_ -> p_326548_.map(p_326545_ -> p_326545_.update("Name", this::fixName)))
                                .map(p_326546_::createList),
                            p_326546_
                        )
                )
        );
    }
}