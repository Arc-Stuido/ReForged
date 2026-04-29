package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * Fired at various stages during level rendering, allowing injection of custom rendering.
 */
public class RenderLevelStageEvent extends net.neoforged.bus.api.Event {
    private final Stage stage;
    private final LevelRenderer levelRenderer;
    @Nullable
    private final PoseStack poseStack;
    private final Matrix4f modelViewMatrix;
    private final Matrix4f projectionMatrix;
    private final int renderTick;
    private final DeltaTracker partialTick;
    private final Camera camera;
    private final Frustum frustum;

    public RenderLevelStageEvent(Stage stage, LevelRenderer levelRenderer, @Nullable PoseStack poseStack,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix, int renderTick,
            DeltaTracker partialTick, Camera camera, Frustum frustum) {
        this.stage = stage;
        this.levelRenderer = levelRenderer;
        this.poseStack = poseStack;
        this.modelViewMatrix = modelViewMatrix;
        this.projectionMatrix = projectionMatrix;
        this.renderTick = renderTick;
        this.partialTick = partialTick;
        this.camera = camera;
        this.frustum = frustum;
    }

    public RenderLevelStageEvent(net.minecraftforge.client.event.RenderLevelStageEvent forge) {
        this(Stage.fromForge(forge.getStage()),
                forge.getLevelRenderer(),
                syntheticPoseStack(forge.getPoseStack()),
                new Matrix4f(forge.getPoseStack()),
                new Matrix4f(forge.getProjectionMatrix()),
                forge.getRenderTick(),
                Minecraft.getInstance().getTimer(),
                forge.getCamera(),
                forge.getFrustum());
    }

    public Stage getStage() { return stage; }
    public LevelRenderer getLevelRenderer() { return levelRenderer; }
    @Nullable
    public PoseStack getPoseStack() { return poseStack; }
    public Matrix4f getModelViewMatrix() { return modelViewMatrix; }
    public Matrix4f getProjectionMatrix() { return projectionMatrix; }
    public int getRenderTick() { return renderTick; }
    public DeltaTracker getPartialTick() { return partialTick; }
    public Camera getCamera() { return camera; }
    public Frustum getFrustum() { return frustum; }

    private static PoseStack syntheticPoseStack(Matrix4f modelViewMatrix) {
        PoseStack stack = new PoseStack();
        stack.mulPose(new Matrix4f(modelViewMatrix));
        return stack;
    }

    /**
     * Enum of rendering stages at which this event can be fired.
     */
    public static class Stage {
        public static final Stage AFTER_SKY = new Stage("AFTER_SKY");
        public static final Stage AFTER_SOLID_BLOCKS = new Stage("AFTER_SOLID_BLOCKS");
        public static final Stage AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS = new Stage("AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS");
        public static final Stage AFTER_CUTOUT_BLOCKS = new Stage("AFTER_CUTOUT_BLOCKS");
        public static final Stage AFTER_ENTITIES = new Stage("AFTER_ENTITIES");
        public static final Stage AFTER_BLOCK_ENTITIES = new Stage("AFTER_BLOCK_ENTITIES");
        public static final Stage AFTER_TRANSLUCENT_BLOCKS = new Stage("AFTER_TRANSLUCENT_BLOCKS");
        public static final Stage AFTER_TRIPWIRE_BLOCKS = new Stage("AFTER_TRIPWIRE_BLOCKS");
        public static final Stage AFTER_PARTICLES = new Stage("AFTER_PARTICLES");
        public static final Stage AFTER_WEATHER = new Stage("AFTER_WEATHER");
        public static final Stage AFTER_LEVEL = new Stage("AFTER_LEVEL");

        private final String name;

        private Stage(String name) {
            this.name = name;
        }

        private static Stage fromForge(net.minecraftforge.client.event.RenderLevelStageEvent.Stage forgeStage) {
            if (forgeStage == net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_SKY) return AFTER_SKY;
            if (forgeStage == net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) return AFTER_SOLID_BLOCKS;
            if (forgeStage == net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS) return AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS;
            if (forgeStage == net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) return AFTER_CUTOUT_BLOCKS;
            if (forgeStage == net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_ENTITIES) return AFTER_ENTITIES;
            if (forgeStage == net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return AFTER_BLOCK_ENTITIES;
            if (forgeStage == net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return AFTER_TRANSLUCENT_BLOCKS;
            if (forgeStage == net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) return AFTER_TRIPWIRE_BLOCKS;
            if (forgeStage == net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_PARTICLES) return AFTER_PARTICLES;
            if (forgeStage == net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_WEATHER) return AFTER_WEATHER;
            if (forgeStage == net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_LEVEL) return AFTER_LEVEL;
            return AFTER_LEVEL;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
