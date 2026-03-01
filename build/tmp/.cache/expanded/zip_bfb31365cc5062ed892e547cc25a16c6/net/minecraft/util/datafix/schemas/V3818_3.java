package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3818_3 extends NamespacedSchema {
    public V3818_3(int p_333453_, Schema p_330765_) {
        super(p_333453_, p_330765_);
    }

    @Override
    public void registerTypes(Schema p_332951_, Map<String, Supplier<TypeTemplate>> p_332616_, Map<String, Supplier<TypeTemplate>> p_331121_) {
        super.registerTypes(p_332951_, p_332616_, p_331121_);
        p_332951_.registerType(
            true,
            References.DATA_COMPONENTS,
            () -> DSL.optionalFields(
                    Pair.of("minecraft:bees", DSL.list(DSL.optionalFields("entity_data", References.ENTITY_TREE.in(p_332951_)))),
                    Pair.of("minecraft:block_entity_data", References.BLOCK_ENTITY.in(p_332951_)),
                    Pair.of("minecraft:bundle_contents", DSL.list(References.ITEM_STACK.in(p_332951_))),
                    Pair.of(
                        "minecraft:can_break",
                        DSL.optionalFields(
                            "predicates",
                            DSL.list(DSL.optionalFields("blocks", DSL.or(References.BLOCK_NAME.in(p_332951_), DSL.list(References.BLOCK_NAME.in(p_332951_)))))
                        )
                    ),
                    Pair.of(
                        "minecraft:can_place_on",
                        DSL.optionalFields(
                            "predicates",
                            DSL.list(DSL.optionalFields("blocks", DSL.or(References.BLOCK_NAME.in(p_332951_), DSL.list(References.BLOCK_NAME.in(p_332951_)))))
                        )
                    ),
                    Pair.of("minecraft:charged_projectiles", DSL.list(References.ITEM_STACK.in(p_332951_))),
                    Pair.of("minecraft:container", DSL.list(DSL.optionalFields("item", References.ITEM_STACK.in(p_332951_)))),
                    Pair.of("minecraft:entity_data", References.ENTITY_TREE.in(p_332951_)),
                    Pair.of("minecraft:pot_decorations", DSL.list(References.ITEM_NAME.in(p_332951_))),
                    Pair.of("minecraft:food", DSL.optionalFields("using_converts_to", References.ITEM_STACK.in(p_332951_)))
                )
        );
    }
}