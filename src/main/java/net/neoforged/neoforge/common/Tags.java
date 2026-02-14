package net.neoforged.neoforge.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;

/**
 * Proxy: NeoForge's common Tags — complete mirror of NeoForge 1.21.1.
 */
public class Tags {

    public static class Blocks {
        // neoforge namespace tags
        public static final TagKey<Block> ENDERMAN_PLACE_ON_BLACKLIST = neoforgeTag("enderman_place_on_blacklist");
        public static final TagKey<Block> NEEDS_WOOD_TOOL = neoforgeTag("needs_wood_tool");
        public static final TagKey<Block> NEEDS_GOLD_TOOL = neoforgeTag("needs_gold_tool");
        public static final TagKey<Block> NEEDS_NETHERITE_TOOL = neoforgeTag("needs_netherite_tool");

        // c namespace tags
        public static final TagKey<Block> BARRELS = tag("barrels");
        public static final TagKey<Block> BARRELS_WOODEN = tag("barrels/wooden");
        public static final TagKey<Block> BOOKSHELVES = tag("bookshelves");
        public static final TagKey<Block> BUDDING_BLOCKS = tag("budding_blocks");
        public static final TagKey<Block> BUDS = tag("buds");
        public static final TagKey<Block> CHAINS = tag("chains");
        public static final TagKey<Block> CHESTS = tag("chests");
        public static final TagKey<Block> CHESTS_ENDER = tag("chests/ender");
        public static final TagKey<Block> CHESTS_TRAPPED = tag("chests/trapped");
        public static final TagKey<Block> CHESTS_WOODEN = tag("chests/wooden");
        public static final TagKey<Block> CLUSTERS = tag("clusters");
        public static final TagKey<Block> COBBLESTONES = tag("cobblestones");
        public static final TagKey<Block> COBBLESTONES_NORMAL = tag("cobblestones/normal");
        public static final TagKey<Block> COBBLESTONES_INFESTED = tag("cobblestones/infested");
        public static final TagKey<Block> COBBLESTONES_MOSSY = tag("cobblestones/mossy");
        public static final TagKey<Block> COBBLESTONES_DEEPSLATE = tag("cobblestones/deepslate");
        public static final TagKey<Block> CONCRETES = tag("concretes");
        public static final TagKey<Block> DYED = tag("dyed");
        public static final TagKey<Block> DYED_BLACK = tag("dyed/black");
        public static final TagKey<Block> DYED_BLUE = tag("dyed/blue");
        public static final TagKey<Block> DYED_BROWN = tag("dyed/brown");
        public static final TagKey<Block> DYED_CYAN = tag("dyed/cyan");
        public static final TagKey<Block> DYED_GRAY = tag("dyed/gray");
        public static final TagKey<Block> DYED_GREEN = tag("dyed/green");
        public static final TagKey<Block> DYED_LIGHT_BLUE = tag("dyed/light_blue");
        public static final TagKey<Block> DYED_LIGHT_GRAY = tag("dyed/light_gray");
        public static final TagKey<Block> DYED_LIME = tag("dyed/lime");
        public static final TagKey<Block> DYED_MAGENTA = tag("dyed/magenta");
        public static final TagKey<Block> DYED_ORANGE = tag("dyed/orange");
        public static final TagKey<Block> DYED_PINK = tag("dyed/pink");
        public static final TagKey<Block> DYED_PURPLE = tag("dyed/purple");
        public static final TagKey<Block> DYED_RED = tag("dyed/red");
        public static final TagKey<Block> DYED_WHITE = tag("dyed/white");
        public static final TagKey<Block> DYED_YELLOW = tag("dyed/yellow");
        public static final TagKey<Block> END_STONES = tag("end_stones");
        public static final TagKey<Block> FENCE_GATES = tag("fence_gates");
        public static final TagKey<Block> FENCE_GATES_WOODEN = tag("fence_gates/wooden");
        public static final TagKey<Block> FENCES = tag("fences");
        public static final TagKey<Block> FENCES_NETHER_BRICK = tag("fences/nether_brick");
        public static final TagKey<Block> FENCES_WOODEN = tag("fences/wooden");
        public static final TagKey<Block> GLASS_BLOCKS = tag("glass_blocks");
        public static final TagKey<Block> GLASS_BLOCKS_COLORLESS = tag("glass_blocks/colorless");
        public static final TagKey<Block> GLASS_BLOCKS_CHEAP = tag("glass_blocks/cheap");
        public static final TagKey<Block> GLASS_BLOCKS_TINTED = tag("glass_blocks/tinted");
        public static final TagKey<Block> GLASS_PANES = tag("glass_panes");
        public static final TagKey<Block> GLASS_PANES_COLORLESS = tag("glass_panes/colorless");
        public static final TagKey<Block> GLAZED_TERRACOTTAS = tag("glazed_terracottas");
        public static final TagKey<Block> GRAVELS = tag("gravels");
        public static final TagKey<Block> HIDDEN_FROM_RECIPE_VIEWERS = tag("hidden_from_recipe_viewers");
        public static final TagKey<Block> NETHERRACKS = tag("netherracks");
        public static final TagKey<Block> OBSIDIANS = tag("obsidians");
        public static final TagKey<Block> OBSIDIANS_NORMAL = tag("obsidians/normal");
        public static final TagKey<Block> OBSIDIANS_CRYING = tag("obsidians/crying");
        public static final TagKey<Block> ORE_BEARING_GROUND_DEEPSLATE = tag("ore_bearing_ground/deepslate");
        public static final TagKey<Block> ORE_BEARING_GROUND_NETHERRACK = tag("ore_bearing_ground/netherrack");
        public static final TagKey<Block> ORE_BEARING_GROUND_STONE = tag("ore_bearing_ground/stone");
        public static final TagKey<Block> ORE_RATES_DENSE = tag("ore_rates/dense");
        public static final TagKey<Block> ORE_RATES_SINGULAR = tag("ore_rates/singular");
        public static final TagKey<Block> ORE_RATES_SPARSE = tag("ore_rates/sparse");
        public static final TagKey<Block> ORES = tag("ores");
        public static final TagKey<Block> ORES_COAL = tag("ores/coal");
        public static final TagKey<Block> ORES_COPPER = tag("ores/copper");
        public static final TagKey<Block> ORES_DIAMOND = tag("ores/diamond");
        public static final TagKey<Block> ORES_EMERALD = tag("ores/emerald");
        public static final TagKey<Block> ORES_GOLD = tag("ores/gold");
        public static final TagKey<Block> ORES_IRON = tag("ores/iron");
        public static final TagKey<Block> ORES_LAPIS = tag("ores/lapis");
        public static final TagKey<Block> ORES_NETHERITE_SCRAP = tag("ores/netherite_scrap");
        public static final TagKey<Block> ORES_QUARTZ = tag("ores/quartz");
        public static final TagKey<Block> ORES_REDSTONE = tag("ores/redstone");
        public static final TagKey<Block> ORES_IN_GROUND_DEEPSLATE = tag("ores_in_ground/deepslate");
        public static final TagKey<Block> ORES_IN_GROUND_NETHERRACK = tag("ores_in_ground/netherrack");
        public static final TagKey<Block> ORES_IN_GROUND_STONE = tag("ores_in_ground/stone");
        public static final TagKey<Block> PUMPKINS = tag("pumpkins");
        public static final TagKey<Block> PUMPKINS_NORMAL = tag("pumpkins/normal");
        public static final TagKey<Block> PUMPKINS_CARVED = tag("pumpkins/carved");
        public static final TagKey<Block> PUMPKINS_JACK_O_LANTERNS = tag("pumpkins/jack_o_lanterns");
        public static final TagKey<Block> PLAYER_WORKSTATIONS_CRAFTING_TABLES = tag("player_workstations/crafting_tables");
        public static final TagKey<Block> PLAYER_WORKSTATIONS_FURNACES = tag("player_workstations/furnaces");
        public static final TagKey<Block> RELOCATION_NOT_SUPPORTED = tag("relocation_not_supported");
        public static final TagKey<Block> ROPES = tag("ropes");
        public static final TagKey<Block> SANDS = tag("sands");
        public static final TagKey<Block> SANDS_COLORLESS = tag("sands/colorless");
        public static final TagKey<Block> SANDS_RED = tag("sands/red");
        public static final TagKey<Block> SANDSTONE_BLOCKS = tag("sandstone/blocks");
        public static final TagKey<Block> SANDSTONE_SLABS = tag("sandstone/slabs");
        public static final TagKey<Block> SANDSTONE_STAIRS = tag("sandstone/stairs");
        public static final TagKey<Block> SANDSTONE_RED_BLOCKS = tag("sandstone/red_blocks");
        public static final TagKey<Block> SANDSTONE_RED_SLABS = tag("sandstone/red_slabs");
        public static final TagKey<Block> SANDSTONE_RED_STAIRS = tag("sandstone/red_stairs");
        public static final TagKey<Block> SANDSTONE_UNCOLORED_BLOCKS = tag("sandstone/uncolored_blocks");
        public static final TagKey<Block> SANDSTONE_UNCOLORED_SLABS = tag("sandstone/uncolored_slabs");
        public static final TagKey<Block> SANDSTONE_UNCOLORED_STAIRS = tag("sandstone/uncolored_stairs");
        public static final TagKey<Block> SKULLS = tag("skulls");
        public static final TagKey<Block> STONES = tag("stones");
        public static final TagKey<Block> STORAGE_BLOCKS = tag("storage_blocks");
        public static final TagKey<Block> STORAGE_BLOCKS_BONE_MEAL = tag("storage_blocks/bone_meal");
        public static final TagKey<Block> STORAGE_BLOCKS_COAL = tag("storage_blocks/coal");
        public static final TagKey<Block> STORAGE_BLOCKS_COPPER = tag("storage_blocks/copper");
        public static final TagKey<Block> STORAGE_BLOCKS_DIAMOND = tag("storage_blocks/diamond");
        public static final TagKey<Block> STORAGE_BLOCKS_DRIED_KELP = tag("storage_blocks/dried_kelp");
        public static final TagKey<Block> STORAGE_BLOCKS_EMERALD = tag("storage_blocks/emerald");
        public static final TagKey<Block> STORAGE_BLOCKS_GOLD = tag("storage_blocks/gold");
        public static final TagKey<Block> STORAGE_BLOCKS_IRON = tag("storage_blocks/iron");
        public static final TagKey<Block> STORAGE_BLOCKS_LAPIS = tag("storage_blocks/lapis");
        public static final TagKey<Block> STORAGE_BLOCKS_NETHERITE = tag("storage_blocks/netherite");
        public static final TagKey<Block> STORAGE_BLOCKS_RAW_COPPER = tag("storage_blocks/raw_copper");
        public static final TagKey<Block> STORAGE_BLOCKS_RAW_GOLD = tag("storage_blocks/raw_gold");
        public static final TagKey<Block> STORAGE_BLOCKS_RAW_IRON = tag("storage_blocks/raw_iron");
        public static final TagKey<Block> STORAGE_BLOCKS_REDSTONE = tag("storage_blocks/redstone");
        public static final TagKey<Block> STORAGE_BLOCKS_SLIME = tag("storage_blocks/slime");
        public static final TagKey<Block> STORAGE_BLOCKS_WHEAT = tag("storage_blocks/wheat");
        public static final TagKey<Block> STRIPPED_LOGS = tag("stripped_logs");
        public static final TagKey<Block> STRIPPED_WOODS = tag("stripped_woods");
        public static final TagKey<Block> VILLAGER_JOB_SITES = tag("villager_job_sites");
        public static final TagKey<Block> VILLAGER_FARMLANDS = neoforgeTag("villager_farmlands");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }

        private static TagKey<Block> neoforgeTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath("neoforge", name));
        }
    }

    public static class EntityTypes {
        public static final TagKey<EntityType<?>> BOSSES = tag("bosses");
        public static final TagKey<EntityType<?>> MINECARTS = tag("minecarts");
        public static final TagKey<EntityType<?>> BOATS = tag("boats");
        public static final TagKey<EntityType<?>> CAPTURING_NOT_SUPPORTED = tag("capturing_not_supported");
        public static final TagKey<EntityType<?>> TELEPORTING_NOT_SUPPORTED = tag("teleporting_not_supported");

        private static TagKey<EntityType<?>> tag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }

    public static class Items {
        // neoforge namespace
        public static final TagKey<Item> ENCHANTING_FUELS = neoforgeTag("enchanting_fuels");

        // c namespace
        public static final TagKey<Item> BARRELS = tag("barrels");
        public static final TagKey<Item> BARRELS_WOODEN = tag("barrels/wooden");
        public static final TagKey<Item> BONES = tag("bones");
        public static final TagKey<Item> BOOKSHELVES = tag("bookshelves");
        public static final TagKey<Item> BRICKS = tag("bricks");
        public static final TagKey<Item> BRICKS_NORMAL = tag("bricks/normal");
        public static final TagKey<Item> BRICKS_NETHER = tag("bricks/nether");
        public static final TagKey<Item> BUCKETS = tag("buckets");
        public static final TagKey<Item> BUCKETS_EMPTY = tag("buckets/empty");
        public static final TagKey<Item> BUCKETS_WATER = tag("buckets/water");
        public static final TagKey<Item> BUCKETS_LAVA = tag("buckets/lava");
        public static final TagKey<Item> BUCKETS_MILK = tag("buckets/milk");
        public static final TagKey<Item> BUCKETS_POWDER_SNOW = tag("buckets/powder_snow");
        public static final TagKey<Item> BUCKETS_ENTITY_WATER = tag("buckets/entity_water");
        public static final TagKey<Item> BUDDING_BLOCKS = tag("budding_blocks");
        public static final TagKey<Item> BUDS = tag("buds");
        public static final TagKey<Item> CHAINS = tag("chains");
        public static final TagKey<Item> CHESTS = tag("chests");
        public static final TagKey<Item> CHESTS_ENDER = tag("chests/ender");
        public static final TagKey<Item> CHESTS_TRAPPED = tag("chests/trapped");
        public static final TagKey<Item> CHESTS_WOODEN = tag("chests/wooden");
        public static final TagKey<Item> COBBLESTONES = tag("cobblestones");
        public static final TagKey<Item> COBBLESTONES_NORMAL = tag("cobblestones/normal");
        public static final TagKey<Item> COBBLESTONES_INFESTED = tag("cobblestones/infested");
        public static final TagKey<Item> COBBLESTONES_MOSSY = tag("cobblestones/mossy");
        public static final TagKey<Item> COBBLESTONES_DEEPSLATE = tag("cobblestones/deepslate");
        public static final TagKey<Item> CONCRETES = tag("concretes");
        public static final TagKey<Item> CONCRETE_POWDERS = tag("concrete_powders");
        public static final TagKey<Item> CLUSTERS = tag("clusters");
        public static final TagKey<Item> CROPS = tag("crops");
        public static final TagKey<Item> CROPS_BEETROOT = tag("crops/beetroot");
        public static final TagKey<Item> CROPS_CACTUS = tag("crops/cactus");
        public static final TagKey<Item> CROPS_CARROT = tag("crops/carrot");
        public static final TagKey<Item> CROPS_COCOA_BEAN = tag("crops/cocoa_bean");
        public static final TagKey<Item> CROPS_MELON = tag("crops/melon");
        public static final TagKey<Item> CROPS_NETHER_WART = tag("crops/nether_wart");
        public static final TagKey<Item> CROPS_POTATO = tag("crops/potato");
        public static final TagKey<Item> CROPS_PUMPKIN = tag("crops/pumpkin");
        public static final TagKey<Item> CROPS_SUGAR_CANE = tag("crops/sugar_cane");
        public static final TagKey<Item> CROPS_WHEAT = tag("crops/wheat");
        public static final TagKey<Item> DRINKS = tag("drinks");
        public static final TagKey<Item> DRINKS_WATER = tag("drinks/water");
        public static final TagKey<Item> DRINKS_WATERY = tag("drinks/watery");
        public static final TagKey<Item> DRINKS_MILK = tag("drinks/milk");
        public static final TagKey<Item> DRINKS_HONEY = tag("drinks/honey");
        public static final TagKey<Item> DRINKS_MAGIC = tag("drinks/magic");
        public static final TagKey<Item> DRINKS_OMINOUS = tag("drinks/ominous");
        public static final TagKey<Item> DRINKS_JUICE = tag("drinks/juice");
        public static final TagKey<Item> DRINK_CONTAINING_BOTTLE = tag("drink_containing/bottle");
        public static final TagKey<Item> DRINK_CONTAINING_BUCKET = tag("drink_containing/bucket");
        public static final TagKey<Item> DUSTS = tag("dusts");
        public static final TagKey<Item> DUSTS_REDSTONE = tag("dusts/redstone");
        public static final TagKey<Item> DUSTS_GLOWSTONE = tag("dusts/glowstone");
        public static final TagKey<Item> DYED = tag("dyed");
        public static final TagKey<Item> DYED_BLACK = tag("dyed/black");
        public static final TagKey<Item> DYED_BLUE = tag("dyed/blue");
        public static final TagKey<Item> DYED_BROWN = tag("dyed/brown");
        public static final TagKey<Item> DYED_CYAN = tag("dyed/cyan");
        public static final TagKey<Item> DYED_GRAY = tag("dyed/gray");
        public static final TagKey<Item> DYED_GREEN = tag("dyed/green");
        public static final TagKey<Item> DYED_LIGHT_BLUE = tag("dyed/light_blue");
        public static final TagKey<Item> DYED_LIGHT_GRAY = tag("dyed/light_gray");
        public static final TagKey<Item> DYED_LIME = tag("dyed/lime");
        public static final TagKey<Item> DYED_MAGENTA = tag("dyed/magenta");
        public static final TagKey<Item> DYED_ORANGE = tag("dyed/orange");
        public static final TagKey<Item> DYED_PINK = tag("dyed/pink");
        public static final TagKey<Item> DYED_PURPLE = tag("dyed/purple");
        public static final TagKey<Item> DYED_RED = tag("dyed/red");
        public static final TagKey<Item> DYED_WHITE = tag("dyed/white");
        public static final TagKey<Item> DYED_YELLOW = tag("dyed/yellow");
        public static final TagKey<Item> DYES = tag("dyes");
        public static final TagKey<Item> DYES_BLACK = tag("dyes/black");
        public static final TagKey<Item> DYES_RED = tag("dyes/red");
        public static final TagKey<Item> DYES_GREEN = tag("dyes/green");
        public static final TagKey<Item> DYES_BROWN = tag("dyes/brown");
        public static final TagKey<Item> DYES_BLUE = tag("dyes/blue");
        public static final TagKey<Item> DYES_PURPLE = tag("dyes/purple");
        public static final TagKey<Item> DYES_CYAN = tag("dyes/cyan");
        public static final TagKey<Item> DYES_LIGHT_GRAY = tag("dyes/light_gray");
        public static final TagKey<Item> DYES_GRAY = tag("dyes/gray");
        public static final TagKey<Item> DYES_PINK = tag("dyes/pink");
        public static final TagKey<Item> DYES_LIME = tag("dyes/lime");
        public static final TagKey<Item> DYES_YELLOW = tag("dyes/yellow");
        public static final TagKey<Item> DYES_LIGHT_BLUE = tag("dyes/light_blue");
        public static final TagKey<Item> DYES_MAGENTA = tag("dyes/magenta");
        public static final TagKey<Item> DYES_ORANGE = tag("dyes/orange");
        public static final TagKey<Item> DYES_WHITE = tag("dyes/white");
        public static final TagKey<Item> EGGS = tag("eggs");
        public static final TagKey<Item> END_STONES = tag("end_stones");
        public static final TagKey<Item> ENDER_PEARLS = tag("ender_pearls");
        public static final TagKey<Item> FEATHERS = tag("feathers");
        public static final TagKey<Item> FENCE_GATES = tag("fence_gates");
        public static final TagKey<Item> FENCE_GATES_WOODEN = tag("fence_gates/wooden");
        public static final TagKey<Item> FENCES = tag("fences");
        public static final TagKey<Item> FENCES_NETHER_BRICK = tag("fences/nether_brick");
        public static final TagKey<Item> FENCES_WOODEN = tag("fences/wooden");
        public static final TagKey<Item> FERTILIZERS = tag("fertilizers");
        public static final TagKey<Item> FOODS = tag("foods");
        public static final TagKey<Item> FOODS_FRUIT = tag("foods/fruit");
        public static final TagKey<Item> FOODS_VEGETABLE = tag("foods/vegetable");
        public static final TagKey<Item> FOODS_BERRY = tag("foods/berry");
        public static final TagKey<Item> FOODS_BREAD = tag("foods/bread");
        public static final TagKey<Item> FOODS_COOKIE = tag("foods/cookie");
        public static final TagKey<Item> FOODS_DOUGH = tag("foods/dough");
        public static final TagKey<Item> FOODS_RAW_MEAT = tag("foods/raw_meat");
        public static final TagKey<Item> FOODS_COOKED_MEAT = tag("foods/cooked_meat");
        public static final TagKey<Item> FOODS_RAW_FISH = tag("foods/raw_fish");
        public static final TagKey<Item> FOODS_COOKED_FISH = tag("foods/cooked_fish");
        public static final TagKey<Item> FOODS_SOUP = tag("foods/soup");
        public static final TagKey<Item> FOODS_CANDY = tag("foods/candy");
        public static final TagKey<Item> FOODS_PIE = tag("foods/pie");
        public static final TagKey<Item> FOODS_GOLDEN = tag("foods/golden");
        public static final TagKey<Item> FOODS_EDIBLE_WHEN_PLACED = tag("foods/edible_when_placed");
        public static final TagKey<Item> FOODS_FOOD_POISONING = tag("foods/food_poisoning");
        public static final TagKey<Item> ANIMAL_FOODS = tag("animal_foods");
        public static final TagKey<Item> GEMS = tag("gems");
        public static final TagKey<Item> GEMS_DIAMOND = tag("gems/diamond");
        public static final TagKey<Item> GEMS_EMERALD = tag("gems/emerald");
        public static final TagKey<Item> GEMS_AMETHYST = tag("gems/amethyst");
        public static final TagKey<Item> GEMS_LAPIS = tag("gems/lapis");
        public static final TagKey<Item> GEMS_PRISMARINE = tag("gems/prismarine");
        public static final TagKey<Item> GEMS_QUARTZ = tag("gems/quartz");
        public static final TagKey<Item> GLASS_BLOCKS = tag("glass_blocks");
        public static final TagKey<Item> GLASS_BLOCKS_COLORLESS = tag("glass_blocks/colorless");
        public static final TagKey<Item> GLASS_BLOCKS_CHEAP = tag("glass_blocks/cheap");
        public static final TagKey<Item> GLASS_BLOCKS_TINTED = tag("glass_blocks/tinted");
        public static final TagKey<Item> GLASS_PANES = tag("glass_panes");
        public static final TagKey<Item> GLASS_PANES_COLORLESS = tag("glass_panes/colorless");
        public static final TagKey<Item> GLAZED_TERRACOTTAS = tag("glazed_terracottas");
        public static final TagKey<Item> GRAVELS = tag("gravels");
        public static final TagKey<Item> GUNPOWDERS = tag("gunpowders");
        public static final TagKey<Item> HIDDEN_FROM_RECIPE_VIEWERS = tag("hidden_from_recipe_viewers");
        public static final TagKey<Item> INGOTS = tag("ingots");
        public static final TagKey<Item> INGOTS_COPPER = tag("ingots/copper");
        public static final TagKey<Item> INGOTS_GOLD = tag("ingots/gold");
        public static final TagKey<Item> INGOTS_IRON = tag("ingots/iron");
        public static final TagKey<Item> INGOTS_NETHERITE = tag("ingots/netherite");
        public static final TagKey<Item> LEATHERS = tag("leathers");
        @Deprecated public static final TagKey<Item> LEATHER = LEATHERS; // old alias
        public static final TagKey<Item> MUSHROOMS = tag("mushrooms");
        public static final TagKey<Item> MUSIC_DISCS = tag("music_discs");
        public static final TagKey<Item> NETHER_STARS = tag("nether_stars");
        public static final TagKey<Item> NETHERRACKS = tag("netherracks");
        public static final TagKey<Item> NUGGETS = tag("nuggets");
        public static final TagKey<Item> NUGGETS_GOLD = tag("nuggets/gold");
        public static final TagKey<Item> NUGGETS_IRON = tag("nuggets/iron");
        public static final TagKey<Item> OBSIDIANS = tag("obsidians");
        public static final TagKey<Item> OBSIDIANS_NORMAL = tag("obsidians/normal");
        public static final TagKey<Item> OBSIDIANS_CRYING = tag("obsidians/crying");
        public static final TagKey<Item> ORE_BEARING_GROUND_DEEPSLATE = tag("ore_bearing_ground/deepslate");
        public static final TagKey<Item> ORE_BEARING_GROUND_NETHERRACK = tag("ore_bearing_ground/netherrack");
        public static final TagKey<Item> ORE_BEARING_GROUND_STONE = tag("ore_bearing_ground/stone");
        public static final TagKey<Item> ORE_RATES_DENSE = tag("ore_rates/dense");
        public static final TagKey<Item> ORE_RATES_SINGULAR = tag("ore_rates/singular");
        public static final TagKey<Item> ORE_RATES_SPARSE = tag("ore_rates/sparse");
        public static final TagKey<Item> ORES = tag("ores");
        public static final TagKey<Item> ORES_COAL = tag("ores/coal");
        public static final TagKey<Item> ORES_COPPER = tag("ores/copper");
        public static final TagKey<Item> ORES_DIAMOND = tag("ores/diamond");
        public static final TagKey<Item> ORES_EMERALD = tag("ores/emerald");
        public static final TagKey<Item> ORES_GOLD = tag("ores/gold");
        public static final TagKey<Item> ORES_IRON = tag("ores/iron");
        public static final TagKey<Item> ORES_LAPIS = tag("ores/lapis");
        public static final TagKey<Item> ORES_NETHERITE_SCRAP = tag("ores/netherite_scrap");
        public static final TagKey<Item> ORES_QUARTZ = tag("ores/quartz");
        public static final TagKey<Item> ORES_REDSTONE = tag("ores/redstone");
        public static final TagKey<Item> ORES_IN_GROUND_DEEPSLATE = tag("ores_in_ground/deepslate");
        public static final TagKey<Item> ORES_IN_GROUND_NETHERRACK = tag("ores_in_ground/netherrack");
        public static final TagKey<Item> ORES_IN_GROUND_STONE = tag("ores_in_ground/stone");
        public static final TagKey<Item> PLAYER_WORKSTATIONS_CRAFTING_TABLES = tag("player_workstations/crafting_tables");
        public static final TagKey<Item> PLAYER_WORKSTATIONS_FURNACES = tag("player_workstations/furnaces");
        public static final TagKey<Item> POTIONS = tag("potions");
        public static final TagKey<Item> POTION_BOTTLE = tag("potions/bottle");
        public static final TagKey<Item> PUMPKINS = tag("pumpkins");
        public static final TagKey<Item> PUMPKINS_NORMAL = tag("pumpkins/normal");
        public static final TagKey<Item> PUMPKINS_CARVED = tag("pumpkins/carved");
        public static final TagKey<Item> PUMPKINS_JACK_O_LANTERNS = tag("pumpkins/jack_o_lanterns");
        public static final TagKey<Item> RAW_MATERIALS = tag("raw_materials");
        public static final TagKey<Item> RAW_MATERIALS_COPPER = tag("raw_materials/copper");
        public static final TagKey<Item> RAW_MATERIALS_GOLD = tag("raw_materials/gold");
        public static final TagKey<Item> RAW_MATERIALS_IRON = tag("raw_materials/iron");
        public static final TagKey<Item> RODS = tag("rods");
        public static final TagKey<Item> RODS_BLAZE = tag("rods/blaze");
        public static final TagKey<Item> RODS_BREEZE = tag("rods/breeze");
        public static final TagKey<Item> RODS_WOODEN = tag("rods/wooden");
        public static final TagKey<Item> ROPES = tag("ropes");
        public static final TagKey<Item> SANDS = tag("sands");
        public static final TagKey<Item> SANDS_COLORLESS = tag("sands/colorless");
        public static final TagKey<Item> SANDS_RED = tag("sands/red");
        public static final TagKey<Item> SANDSTONE_BLOCKS = tag("sandstone/blocks");
        public static final TagKey<Item> SANDSTONE_SLABS = tag("sandstone/slabs");
        public static final TagKey<Item> SANDSTONE_STAIRS = tag("sandstone/stairs");
        public static final TagKey<Item> SANDSTONE_RED_BLOCKS = tag("sandstone/red_blocks");
        public static final TagKey<Item> SANDSTONE_RED_SLABS = tag("sandstone/red_slabs");
        public static final TagKey<Item> SANDSTONE_RED_STAIRS = tag("sandstone/red_stairs");
        public static final TagKey<Item> SANDSTONE_UNCOLORED_BLOCKS = tag("sandstone/uncolored_blocks");
        public static final TagKey<Item> SANDSTONE_UNCOLORED_SLABS = tag("sandstone/uncolored_slabs");
        public static final TagKey<Item> SANDSTONE_UNCOLORED_STAIRS = tag("sandstone/uncolored_stairs");
        public static final TagKey<Item> SEEDS = tag("seeds");
        public static final TagKey<Item> SEEDS_BEETROOT = tag("seeds/beetroot");
        public static final TagKey<Item> SEEDS_MELON = tag("seeds/melon");
        public static final TagKey<Item> SEEDS_PUMPKIN = tag("seeds/pumpkin");
        public static final TagKey<Item> SEEDS_TORCHFLOWER = tag("seeds/torchflower");
        public static final TagKey<Item> SEEDS_WHEAT = tag("seeds/wheat");
        public static final TagKey<Item> SHULKER_BOXES = tag("shulker_boxes");
        public static final TagKey<Item> SLIME_BALLS = tag("slime_balls");
        @Deprecated public static final TagKey<Item> SLIMEBALLS = tag("slimeballs");
        public static final TagKey<Item> STONES = tag("stones");
        public static final TagKey<Item> STORAGE_BLOCKS = tag("storage_blocks");
        public static final TagKey<Item> STORAGE_BLOCKS_BONE_MEAL = tag("storage_blocks/bone_meal");
        public static final TagKey<Item> STORAGE_BLOCKS_COAL = tag("storage_blocks/coal");
        public static final TagKey<Item> STORAGE_BLOCKS_COPPER = tag("storage_blocks/copper");
        public static final TagKey<Item> STORAGE_BLOCKS_DIAMOND = tag("storage_blocks/diamond");
        public static final TagKey<Item> STORAGE_BLOCKS_DRIED_KELP = tag("storage_blocks/dried_kelp");
        public static final TagKey<Item> STORAGE_BLOCKS_EMERALD = tag("storage_blocks/emerald");
        public static final TagKey<Item> STORAGE_BLOCKS_GOLD = tag("storage_blocks/gold");
        public static final TagKey<Item> STORAGE_BLOCKS_IRON = tag("storage_blocks/iron");
        public static final TagKey<Item> STORAGE_BLOCKS_LAPIS = tag("storage_blocks/lapis");
        public static final TagKey<Item> STORAGE_BLOCKS_NETHERITE = tag("storage_blocks/netherite");
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_COPPER = tag("storage_blocks/raw_copper");
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_GOLD = tag("storage_blocks/raw_gold");
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_IRON = tag("storage_blocks/raw_iron");
        public static final TagKey<Item> STORAGE_BLOCKS_REDSTONE = tag("storage_blocks/redstone");
        public static final TagKey<Item> STORAGE_BLOCKS_SLIME = tag("storage_blocks/slime");
        public static final TagKey<Item> STORAGE_BLOCKS_WHEAT = tag("storage_blocks/wheat");
        public static final TagKey<Item> STRINGS = tag("strings");
        public static final TagKey<Item> STRIPPED_LOGS = tag("stripped_logs");
        public static final TagKey<Item> STRIPPED_WOODS = tag("stripped_woods");
        public static final TagKey<Item> VILLAGER_JOB_SITES = tag("villager_job_sites");

        // Tools and Armors
        public static final TagKey<Item> TOOLS = tag("tools");
        public static final TagKey<Item> TOOLS_SHIELD = tag("tools/shield");
        public static final TagKey<Item> TOOLS_BOW = tag("tools/bow");
        public static final TagKey<Item> TOOLS_CROSSBOW = tag("tools/crossbow");
        public static final TagKey<Item> TOOLS_FISHING_ROD = tag("tools/fishing_rod");
        public static final TagKey<Item> TOOLS_SPEAR = tag("tools/spear");
        public static final TagKey<Item> TOOLS_SHEAR = tag("tools/shear");
        public static final TagKey<Item> TOOLS_BRUSH = tag("tools/brush");
        public static final TagKey<Item> TOOLS_IGNITER = tag("tools/igniter");
        public static final TagKey<Item> TOOLS_MACE = tag("tools/mace");
        public static final TagKey<Item> TOOLS_WRENCH = tag("tools/wrench");
        public static final TagKey<Item> MELEE_WEAPON_TOOLS = tag("tools/melee_weapon");
        public static final TagKey<Item> RANGED_WEAPON_TOOLS = tag("tools/ranged_weapon");
        public static final TagKey<Item> MINING_TOOL_TOOLS = tag("tools/mining_tool");
        public static final TagKey<Item> ARMORS = tag("armors");
        public static final TagKey<Item> ENCHANTABLES = tag("enchantables");

        // Plural aliases used by some mods (Forge-style)
        public static final TagKey<Item> TOOLS_SWORDS = tag("tools/swords");
        public static final TagKey<Item> TOOLS_AXES = tag("tools/axes");
        public static final TagKey<Item> TOOLS_PICKAXES = tag("tools/pickaxes");
        public static final TagKey<Item> TOOLS_SHOVELS = tag("tools/shovels");
        public static final TagKey<Item> TOOLS_HOES = tag("tools/hoes");
        public static final TagKey<Item> TOOLS_SHIELDS = tag("tools/shields");
        public static final TagKey<Item> TOOLS_BOWS = tag("tools/bows");
        public static final TagKey<Item> TOOLS_CROSSBOWS = tag("tools/crossbows");
        public static final TagKey<Item> TOOLS_FISHING_RODS = tag("tools/fishing_rods");
        public static final TagKey<Item> TOOLS_TRIDENTS = tag("tools/tridents");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }

        private static TagKey<Item> neoforgeTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath("neoforge", name));
        }
    }

    public static class Fluids {
        public static final TagKey<Fluid> WATER = tag("water");
        public static final TagKey<Fluid> LAVA = tag("lava");
        public static final TagKey<Fluid> MILK = tag("milk");
        public static final TagKey<Fluid> GASEOUS = tag("gaseous");
        public static final TagKey<Fluid> HONEY = tag("honey");
        public static final TagKey<Fluid> EXPERIENCE = tag("experience");
        public static final TagKey<Fluid> POTION = tag("potion");
        public static final TagKey<Fluid> SUSPICIOUS_STEW = tag("suspicious_stew");
        public static final TagKey<Fluid> MUSHROOM_STEW = tag("mushroom_stew");
        public static final TagKey<Fluid> RABBIT_STEW = tag("rabbit_stew");
        public static final TagKey<Fluid> BEETROOT_SOUP = tag("beetroot_soup");
        public static final TagKey<Fluid> HIDDEN_FROM_RECIPE_VIEWERS = tag("hidden_from_recipe_viewers");

        private static TagKey<Fluid> tag(String name) {
            return FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }

    public static class Enchantments {
        public static final TagKey<Enchantment> INCREASE_BLOCK_DROPS = tag("increase_block_drops");
        public static final TagKey<Enchantment> INCREASE_ENTITY_DROPS = tag("increase_entity_drops");
        public static final TagKey<Enchantment> WEAPON_DAMAGE_ENHANCEMENTS = tag("weapon_damage_enhancements");
        public static final TagKey<Enchantment> ENTITY_SPEED_ENHANCEMENTS = tag("entity_speed_enhancements");
        public static final TagKey<Enchantment> ENTITY_AUXILIARY_MOVEMENT_ENHANCEMENTS = tag("entity_auxiliary_movement_enhancements");
        public static final TagKey<Enchantment> ENTITY_DEFENSE_ENHANCEMENTS = tag("entity_defense_enhancements");

        private static TagKey<Enchantment> tag(String name) {
            return TagKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }

    public static class Biomes {
        public static final TagKey<Biome> NO_DEFAULT_MONSTERS = tag("no_default_monsters");
        public static final TagKey<Biome> HIDDEN_FROM_LOCATOR_SELECTION = tag("hidden_from_locator_selection");
        public static final TagKey<Biome> IS_VOID = tag("is_void");
        public static final TagKey<Biome> IS_HOT = tag("is_hot");
        public static final TagKey<Biome> IS_HOT_OVERWORLD = tag("is_hot/overworld");
        public static final TagKey<Biome> IS_HOT_NETHER = tag("is_hot/nether");
        public static final TagKey<Biome> IS_HOT_END = tag("is_hot/end");
        public static final TagKey<Biome> IS_TEMPERATE = tag("is_temperate");
        public static final TagKey<Biome> IS_TEMPERATE_OVERWORLD = tag("is_temperate/overworld");
        public static final TagKey<Biome> IS_TEMPERATE_NETHER = tag("is_temperate/nether");
        public static final TagKey<Biome> IS_TEMPERATE_END = tag("is_temperate/end");
        public static final TagKey<Biome> IS_COLD = tag("is_cold");
        public static final TagKey<Biome> IS_COLD_OVERWORLD = tag("is_cold/overworld");
        public static final TagKey<Biome> IS_COLD_NETHER = tag("is_cold/nether");
        public static final TagKey<Biome> IS_COLD_END = tag("is_cold/end");
        public static final TagKey<Biome> IS_SPARSE_VEGETATION = tag("is_sparse_vegetation");
        public static final TagKey<Biome> IS_SPARSE_VEGETATION_OVERWORLD = tag("is_sparse_vegetation/overworld");
        public static final TagKey<Biome> IS_SPARSE_VEGETATION_NETHER = tag("is_sparse_vegetation/nether");
        public static final TagKey<Biome> IS_SPARSE_VEGETATION_END = tag("is_sparse_vegetation/end");
        public static final TagKey<Biome> IS_DENSE_VEGETATION = tag("is_dense_vegetation");
        public static final TagKey<Biome> IS_DENSE_VEGETATION_OVERWORLD = tag("is_dense_vegetation/overworld");
        public static final TagKey<Biome> IS_DENSE_VEGETATION_NETHER = tag("is_dense_vegetation/nether");
        public static final TagKey<Biome> IS_DENSE_VEGETATION_END = tag("is_dense_vegetation/end");
        public static final TagKey<Biome> IS_WET = tag("is_wet");
        public static final TagKey<Biome> IS_WET_OVERWORLD = tag("is_wet/overworld");
        public static final TagKey<Biome> IS_WET_NETHER = tag("is_wet/nether");
        public static final TagKey<Biome> IS_WET_END = tag("is_wet/end");
        public static final TagKey<Biome> IS_DRY = tag("is_dry");
        public static final TagKey<Biome> IS_DRY_OVERWORLD = tag("is_dry/overworld");
        public static final TagKey<Biome> IS_DRY_NETHER = tag("is_dry/nether");
        public static final TagKey<Biome> IS_DRY_END = tag("is_dry/end");
        public static final TagKey<Biome> IS_OVERWORLD = tag("is_overworld");
        public static final TagKey<Biome> IS_CONIFEROUS_TREE = tag("is_tree/coniferous");
        public static final TagKey<Biome> IS_SAVANNA_TREE = tag("is_tree/savanna");
        public static final TagKey<Biome> IS_JUNGLE_TREE = tag("is_tree/jungle");
        public static final TagKey<Biome> IS_DECIDUOUS_TREE = tag("is_tree/deciduous");
        public static final TagKey<Biome> IS_MOUNTAIN = tag("is_mountain");
        public static final TagKey<Biome> IS_MOUNTAIN_PEAK = tag("is_mountain/peak");
        public static final TagKey<Biome> IS_MOUNTAIN_SLOPE = tag("is_mountain/slope");
        public static final TagKey<Biome> IS_PLAINS = tag("is_plains");
        public static final TagKey<Biome> IS_SNOWY_PLAINS = tag("is_snowy_plains");
        public static final TagKey<Biome> IS_FOREST = tag("is_forest");
        public static final TagKey<Biome> IS_BIRCH_FOREST = tag("is_birch_forest");
        public static final TagKey<Biome> IS_FLOWER_FOREST = tag("is_flower_forest");
        public static final TagKey<Biome> IS_TAIGA = tag("is_taiga");
        public static final TagKey<Biome> IS_OLD_GROWTH = tag("is_old_growth");
        public static final TagKey<Biome> IS_HILL = tag("is_hill");
        public static final TagKey<Biome> IS_WINDSWEPT = tag("is_windswept");
        public static final TagKey<Biome> IS_JUNGLE = tag("is_jungle");
        public static final TagKey<Biome> IS_SAVANNA = tag("is_savanna");
        public static final TagKey<Biome> IS_SWAMP = tag("is_swamp");
        public static final TagKey<Biome> IS_DESERT = tag("is_desert");
        public static final TagKey<Biome> IS_BADLANDS = tag("is_badlands");
        public static final TagKey<Biome> IS_BEACH = tag("is_beach");
        public static final TagKey<Biome> IS_STONY_SHORES = tag("is_stony_shores");
        public static final TagKey<Biome> IS_MUSHROOM = tag("is_mushroom");
        public static final TagKey<Biome> IS_RIVER = tag("is_river");
        public static final TagKey<Biome> IS_OCEAN = tag("is_ocean");
        public static final TagKey<Biome> IS_DEEP_OCEAN = tag("is_deep_ocean");
        public static final TagKey<Biome> IS_SHALLOW_OCEAN = tag("is_shallow_ocean");
        public static final TagKey<Biome> IS_UNDERGROUND = tag("is_underground");
        public static final TagKey<Biome> IS_CAVE = tag("is_cave");
        public static final TagKey<Biome> IS_LUSH = tag("is_lush");
        public static final TagKey<Biome> IS_MAGICAL = tag("is_magical");
        public static final TagKey<Biome> IS_RARE = tag("is_rare");
        public static final TagKey<Biome> IS_PLATEAU = tag("is_plateau");
        public static final TagKey<Biome> IS_SPOOKY = tag("is_spooky");
        public static final TagKey<Biome> IS_WASTELAND = tag("is_wasteland");
        public static final TagKey<Biome> IS_DEAD = tag("is_dead");
        public static final TagKey<Biome> IS_FLORAL = tag("is_floral");
        public static final TagKey<Biome> IS_SANDY = tag("is_sandy");
        public static final TagKey<Biome> IS_SNOWY = tag("is_snowy");
        public static final TagKey<Biome> IS_ICY = tag("is_icy");
        public static final TagKey<Biome> IS_AQUATIC = tag("is_aquatic");
        public static final TagKey<Biome> IS_AQUATIC_ICY = tag("is_aquatic_icy");
        public static final TagKey<Biome> IS_NETHER = tag("is_nether");
        public static final TagKey<Biome> IS_NETHER_FOREST = tag("is_nether_forest");
        public static final TagKey<Biome> IS_END = tag("is_end");
        public static final TagKey<Biome> IS_OUTER_END_ISLAND = tag("is_outer_end_island");
        @Deprecated public static final TagKey<Biome> IS_MODIFIED = tag("is_modified");

        private static TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }

    public static class Structures {
        public static final TagKey<Structure> HIDDEN_FROM_DISPLAYERS = tag("hidden_from_displayers");
        public static final TagKey<Structure> HIDDEN_FROM_LOCATOR_SELECTION = tag("hidden_from_locator_selection");

        private static TagKey<Structure> tag(String name) {
            return TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }

    public static class DamageTypes {
        public static final TagKey<DamageType> IS_MAGIC = neoforgeTag("is_magic");
        public static final TagKey<DamageType> IS_POISON = neoforgeTag("is_poison");
        public static final TagKey<DamageType> IS_WITHER = neoforgeTag("is_wither");
        public static final TagKey<DamageType> IS_ENVIRONMENT = neoforgeTag("is_environment");
        public static final TagKey<DamageType> IS_PHYSICAL = neoforgeTag("is_physical");
        public static final TagKey<DamageType> IS_TECHNICAL = neoforgeTag("is_technical");
        public static final TagKey<DamageType> NO_FLINCH = neoforgeTag("no_flinch");

        private static TagKey<DamageType> neoforgeTag(String name) {
            return TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("neoforge", name));
        }
    }

    public static String getTagTranslationKey(TagKey<?> tagKey) {
        StringBuilder sb = new StringBuilder();
        sb.append("tag.");
        ResourceLocation registryId = tagKey.registry().location();
        ResourceLocation tagId = tagKey.location();
        sb.append(registryId.toShortLanguageKey().replace("/", "."))
                .append(".")
                .append(tagId.getNamespace())
                .append(".")
                .append(tagId.getPath().replace("/", "."));
        return sb.toString();
    }
}
