package net.neoforged.neoforge.fluids;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Registry for source-fluid interactions with surrounding blocks and fluids.
 */
public final class FluidInteractionRegistry {
    private static final Map<FluidType, List<InteractionInformation>> INTERACTIONS = new HashMap<>();

    private FluidInteractionRegistry() {}

    public static synchronized void addInteraction(FluidType source, InteractionInformation interaction) {
        INTERACTIONS.computeIfAbsent(source, s -> new ArrayList<>()).add(interaction);
    }

    /**
     * Performs the first matching interaction for the fluid at {@code pos}.
     */
    public static boolean canInteract(Level level, BlockPos pos) {
        FluidState state = level.getFluidState(pos);
        FluidType sourceType = FluidType.wrap(state.getFluidType());
        if (sourceType == null) {
            return false;
        }

        for (Direction direction : LiquidBlock.POSSIBLE_FLOW_DIRECTIONS) {
            BlockPos relativePos = pos.relative(direction.getOpposite());
            List<InteractionInformation> interactions = INTERACTIONS.getOrDefault(sourceType, Collections.emptyList());
            for (InteractionInformation interaction : interactions) {
                if (interaction.predicate().test(level, pos, relativePos, state)) {
                    interaction.interaction().interact(level, pos, relativePos, state);
                    return true;
                }
            }
        }

        return false;
    }

    static {
        addInteraction(FluidType.wrap(NeoForgeMod.LAVA_TYPE.value()), new InteractionInformation(
                FluidType.wrap(NeoForgeMod.WATER_TYPE.value()),
                fluidState -> fluidState.isSource() ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState()));

        addInteraction(FluidType.wrap(NeoForgeMod.LAVA_TYPE.value()), new InteractionInformation(
                (level, currentPos, relativePos, currentState) -> level.getBlockState(currentPos.below()).is(Blocks.SOUL_SOIL)
                        && level.getBlockState(relativePos).is(Blocks.BLUE_ICE),
                Blocks.BASALT.defaultBlockState()));
    }

    public record InteractionInformation(HasFluidInteraction predicate, FluidInteraction interaction) {
        public InteractionInformation(FluidType type, BlockState state) {
            this(type, fluidState -> state);
        }

        public InteractionInformation(HasFluidInteraction predicate, BlockState state) {
            this(predicate, fluidState -> state);
        }

        public InteractionInformation(FluidType type, Function<FluidState, BlockState> getState) {
            this((level, currentPos, relativePos, currentState) -> type != null
                    && type.equals(FluidType.wrap(level.getFluidState(relativePos).getFluidType())), getState);
        }

        public InteractionInformation(HasFluidInteraction predicate, Function<FluidState, BlockState> getState) {
            this(predicate, (level, currentPos, relativePos, currentState) -> {
                BlockState result = getState.apply(currentState);
                if (result != null) {
                    level.setBlockAndUpdate(currentPos,
                            EventHooks.fireFluidPlaceBlockEvent(level, currentPos, currentPos, result));
                    level.levelEvent(1501, currentPos, 0);
                }
            });
        }
    }

    @FunctionalInterface
    public interface HasFluidInteraction {
        boolean test(Level level, BlockPos currentPos, BlockPos relativePos, FluidState currentState);
    }

    @FunctionalInterface
    public interface FluidInteraction {
        void interact(Level level, BlockPos currentPos, BlockPos relativePos, FluidState currentState);
    }
}
