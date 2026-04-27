package org.xiyu.reforged.bridge;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.xiyu.reforged.core.NeoForgeModLoader;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Reflection-based bridge that calls Flywheel APIs through the NeoModClassLoader.
 *
 * <p>Because Flywheel classes are only loaded by NeoModClassLoader (a child-first classloader),
 * code running in the TransformingClassLoader (including our Mixins) cannot reference Flywheel
 * types directly. This bridge uses reflection to cross the classloader boundary.</p>
 *
 * <p>All methods are fail-safe and log errors rather than crashing.</p>
 */
public final class FlywheelRenderBridge {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEBUG_LOGGING = Boolean.getBoolean("reforged.debug.flywheel");
    private static final int DEBUG_FRAME_LOG_LIMIT = 3;
    private static final int DEBUG_BE_LOG_LIMIT = 12;
    private static final int FAILURE_LOG_LIMIT = 5;
    private static final int RELOAD_READD_DELAY_FRAMES = 40;

    // =============================================================  
    // Initialization state
    // =============================================================
    private static volatile boolean initialized = false;
    private static volatile boolean available = false;

    // =============================================================
    // Cached reflection handles (set during init)
    // =============================================================

    // FlwImpl.freezeRegistries()
    private static Method mFreezeRegistries;

    // EndClientResourceReloadEvent constructor
    private static Constructor<?> ctorEndReloadEvent;

    // ReloadLevelRendererEvent constructor
    private static Constructor<?> ctorReloadLevelRendererEvent;

    // RenderContextImpl.create(LevelRenderer, ClientLevel, RenderBuffers, Matrix4fc, Matrix4f, Camera, float)
    private static Method mCreateRenderContext;

    // VisualizationManager.get(LevelAccessor) and .supportsVisualization(LevelAccessor)
    private static Method mVMGet;
    private static Method mVMSupportsVisualization;
    private static Method mVMRenderOrigin;
    private static Method mVMOnLightUpdate;

    // VisualizationManager.renderDispatcher(), .blockEntities(), and .entities()
    private static Method mVMRenderDispatcher;
    private static Method mVMBlockEntities;
    private static Method mVMEntities;

    // RenderDispatcher.onStartLevelRender(RenderContext), .afterEntities(RenderContext), .beforeCrumbling(RenderContext, Long2ObjectMap)
    private static Method mOnStartLevelRender;
    private static Method mAfterEntities;
    private static Method mBeforeCrumbling;

    // VisualManager.queueAdd(Object), .queueRemove(Object), .queueUpdate(Object)
    private static Method mQueueAdd;
    private static Method mQueueRemove;
    private static Method mQueueUpdate;

    // VisualizationHelper.skipVanillaRender(Entity), .skipVanillaRender(BlockEntity)
    private static Method mSkipVanillaRender;
    private static Method mSkipVanillaRenderBE;

    // FlwImplXplat.INSTANCE field
    private static Object flwImplXplatInstance;
    private static Method mDispatchReloadLevelRendererEvent;

    // GlStateTracker fields — needed because Flywheel's GlStateManagerMixin doesn't apply
    // (Flywheel is loaded by NeoModClassLoader, its mixins target TransformingClassLoader classes)
    private static Field fGlStateTrackerBUFFERS; // int[]
    private static Field fGlStateTrackerVao;     // int
    private static Field fGlStateTrackerProgram; // int
    private static int[] glBindingEnums;         // cached GlBufferType.glBindingEnum values

    // LevelUniforms light directions — needed because Flywheel's GlStateManagerMixin
    // method flywheel$onSetupLevelDiffuseLighting doesn't apply
    private static org.joml.Vector3f flwLight0Direction; // LevelUniforms.LIGHT0_DIRECTION reference
    private static org.joml.Vector3f flwLight1Direction; // LevelUniforms.LIGHT1_DIRECTION reference
    private static boolean lightDirectionsSynced = false;

    // FogUniforms.update() — needed because Flywheel's RenderSystemMixin doesn't apply
    private static Method mFogUniformsUpdate;
    private static Field fFrameUniformsFirstWrite;
    private static Method mVMGetEngineImpl;

    // Per-frame mutable state (only read/written on render thread)
    private static Object currentRenderContext;
    private static int beginRenderLogCount = 0;
    private static int backendLogCount = 0;
    private static int beforeBELogCount = 0;
    private static int addLogCount = 0;
    private static int beginRenderFailureCount = 0;
    private static int beforeBlockEntitiesFailureCount = 0;
    private static int blockEntityLifecycleFailureCount = 0;
    private static net.minecraft.core.Vec3i lastRenderOrigin = null;
    private static Object lastCameraType = null;

    // Deferred refresh: after reload/origin events, wait N frames before rebuilding loaded visuals.
    private static int reAddDelayFrames = -1;
    private static ClientLevel reAddPendingLevel = null;
    private static String reAddReason = "reload";

    private FlywheelRenderBridge() {}

    private static void debugInfo(String message, Object... args) {
        if (DEBUG_LOGGING) {
            LOGGER.info(message, args);
        }
    }

    private static boolean shouldLogDebugFrame(int counter, int limit) {
        return DEBUG_LOGGING && counter < limit;
    }

    private static boolean supportsVisualization(Level level) {
        if (level == null) {
            return false;
        }
        try {
            return (boolean) mVMSupportsVisualization.invoke(null, level);
        } catch (Throwable t) {
            return false;
        }
    }

    private static Object getVisualizationManager(Level level) {
        if (level == null || !supportsVisualization(level)) {
            return null;
        }
        try {
            return mVMGet.invoke(null, level);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Object getBlockEntityStorage(Level level) throws Exception {
        Object visMgr = getVisualizationManager(level);
        return visMgr == null ? null : mVMBlockEntities.invoke(visMgr);
    }

    private static Object getEntityStorage(Level level) throws Exception {
        Object visMgr = getVisualizationManager(level);
        return visMgr == null ? null : mVMEntities.invoke(visMgr);
    }

    private static void scheduleVisualRefresh(ClientLevel level, int reAddDelay, String reason) {
        reAddDelayFrames = reAddDelay;
        reAddPendingLevel = level;
        reAddReason = reason;
        lastRenderOrigin = null;
        debugInfo("[ReForged] FlywheelRenderBridge: scheduled deferred visual refresh in {} frames ({})",
                reAddDelay, reason);
    }

    // =============================================================
    // Lazy initialization
    // =============================================================
    private static void ensureInit() {
        if (initialized) return;
        synchronized (FlywheelRenderBridge.class) {
            if (initialized) return;
            try {
                doInit();
                available = true;
                LOGGER.info("[ReForged] FlywheelRenderBridge initialized successfully");
            } catch (Throwable t) {
                LOGGER.warn("[ReForged] FlywheelRenderBridge init failed — Flywheel rendering will be unavailable: {}", t.getMessage());
                available = false;
            } finally {
                initialized = true;
            }
        }
    }

    private static void doInit() throws Exception {
        ClassLoader cl = NeoForgeModLoader.getNeoModClassLoader();
        if (cl == null) {
            throw new IllegalStateException("NeoModClassLoader not available yet");
        }

        // FlwImpl
        Class<?> cFlwImpl = cl.loadClass("dev.engine_room.flywheel.impl.FlwImpl");
        mFreezeRegistries = cFlwImpl.getMethod("freezeRegistries");

        // EndClientResourceReloadEvent
        Class<?> cEndReloadEvent = cl.loadClass("dev.engine_room.flywheel.api.event.EndClientResourceReloadEvent");
        ctorEndReloadEvent = cEndReloadEvent.getConstructor(Minecraft.class, ResourceManager.class, boolean.class, Optional.class);

        // ReloadLevelRendererEvent
        Class<?> cReloadLREvent = cl.loadClass("dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent");
        ctorReloadLevelRendererEvent = cReloadLREvent.getConstructor(ClientLevel.class);

        // FlwImplXplat.INSTANCE
        Class<?> cFlwImplXplat = cl.loadClass("dev.engine_room.flywheel.impl.FlwImplXplat");
        Field fInstance = cFlwImplXplat.getField("INSTANCE");
        flwImplXplatInstance = fInstance.get(null);
        mDispatchReloadLevelRendererEvent = cFlwImplXplat.getMethod("dispatchReloadLevelRendererEvent", ClientLevel.class);

        // RenderContextImpl
        Class<?> cRenderContextImpl = cl.loadClass("dev.engine_room.flywheel.impl.event.RenderContextImpl");
        // create(LevelRenderer, ClientLevel, RenderBuffers, Matrix4fc, Matrix4f, Camera, float)
        Class<?> cMatrix4fc = org.joml.Matrix4fc.class;
        mCreateRenderContext = cRenderContextImpl.getMethod("create",
                LevelRenderer.class, ClientLevel.class, RenderBuffers.class,
                cMatrix4fc, Matrix4f.class, Camera.class, float.class);

        // VisualizationManager
        Class<?> cVisMgr = cl.loadClass("dev.engine_room.flywheel.api.visualization.VisualizationManager");
        mVMGet = cVisMgr.getMethod("get", net.minecraft.world.level.LevelAccessor.class);
        mVMSupportsVisualization = cVisMgr.getMethod("supportsVisualization", net.minecraft.world.level.LevelAccessor.class);
        mVMRenderOrigin = cVisMgr.getMethod("renderOrigin");
        mVMRenderDispatcher = cVisMgr.getMethod("renderDispatcher");
        mVMBlockEntities = cVisMgr.getMethod("blockEntities");
        mVMEntities = cVisMgr.getMethod("entities");

        Class<?> cVisMgrImpl = cl.loadClass("dev.engine_room.flywheel.impl.visualization.VisualizationManagerImpl");
        mVMOnLightUpdate = cVisMgrImpl.getMethod("onLightUpdate", SectionPos.class, LightLayer.class);
        mVMGetEngineImpl = cVisMgrImpl.getMethod("getEngineImpl");

        // RenderDispatcher
        Class<?> cRenderContext = cl.loadClass("dev.engine_room.flywheel.api.backend.RenderContext");
        Class<?> cRenderDispatcher = cl.loadClass("dev.engine_room.flywheel.api.visualization.VisualizationManager$RenderDispatcher");
        mOnStartLevelRender = cRenderDispatcher.getMethod("onStartLevelRender", cRenderContext);
        mAfterEntities = cRenderDispatcher.getMethod("afterEntities", cRenderContext);
        mBeforeCrumbling = cRenderDispatcher.getMethod("beforeCrumbling", cRenderContext, Long2ObjectMap.class);

        // VisualManager
        Class<?> cVisualManager = cl.loadClass("dev.engine_room.flywheel.api.visualization.VisualManager");
        mQueueAdd = cVisualManager.getMethod("queueAdd", Object.class);
        mQueueRemove = cVisualManager.getMethod("queueRemove", Object.class);
        mQueueUpdate = cVisualManager.getMethod("queueUpdate", Object.class);

        // VisualizationHelper
        Class<?> cVisHelper = cl.loadClass("dev.engine_room.flywheel.lib.visualization.VisualizationHelper");
        mSkipVanillaRender = cVisHelper.getMethod("skipVanillaRender", Entity.class);
        mSkipVanillaRenderBE = cVisHelper.getMethod("skipVanillaRender", BlockEntity.class);

        // GlStateTracker — track GL state that Flywheel's missing mixin would normally provide
        try {
            Class<?> cGlStateTracker = cl.loadClass("dev.engine_room.flywheel.backend.gl.GlStateTracker");
            fGlStateTrackerBUFFERS = cGlStateTracker.getDeclaredField("BUFFERS");
            fGlStateTrackerBUFFERS.setAccessible(true);
            fGlStateTrackerVao = cGlStateTracker.getDeclaredField("vao");
            fGlStateTrackerVao.setAccessible(true);
            fGlStateTrackerProgram = cGlStateTracker.getDeclaredField("program");
            fGlStateTrackerProgram.setAccessible(true);

            Class<?> cGlBufferType = cl.loadClass("dev.engine_room.flywheel.backend.gl.buffer.GlBufferType");
            Method mValues = cGlBufferType.getMethod("values");
            Object[] bufferTypes = (Object[]) mValues.invoke(null);
            Field fGlBindingEnum = cGlBufferType.getField("glBindingEnum");
            glBindingEnums = new int[bufferTypes.length];
            for (int i = 0; i < bufferTypes.length; i++) {
                glBindingEnums[i] = fGlBindingEnum.getInt(bufferTypes[i]);
            }
            debugInfo("[ReForged] FlywheelRenderBridge: GlStateTracker sync initialized ({} buffer types)", bufferTypes.length);
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] FlywheelRenderBridge: GlStateTracker init failed (non-fatal): {}", t.getMessage());
        }

        // LevelUniforms light directions — Flywheel's GlStateManagerMixin captures these
        // from GlStateManager._setupLevelDiffuseLighting(), but the mixin can't apply
        try {
            Class<?> cLevelUniforms = cl.loadClass("dev.engine_room.flywheel.backend.engine.uniform.LevelUniforms");
            Field fLight0 = cLevelUniforms.getField("LIGHT0_DIRECTION");
            Field fLight1 = cLevelUniforms.getField("LIGHT1_DIRECTION");
            flwLight0Direction = (org.joml.Vector3f) fLight0.get(null);
            flwLight1Direction = (org.joml.Vector3f) fLight1.get(null);
            debugInfo("[ReForged] FlywheelRenderBridge: LevelUniforms light direction refs acquired");
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] FlywheelRenderBridge: LevelUniforms light init failed (non-fatal): {}", t.getMessage());
        }

        // FogUniforms — needed because Flywheel's RenderSystemMixin doesn't apply
        try {
            Class<?> cFogUniforms = cl.loadClass("dev.engine_room.flywheel.backend.engine.uniform.FogUniforms");
            mFogUniformsUpdate = cFogUniforms.getMethod("update");
            debugInfo("[ReForged] FlywheelRenderBridge: FogUniforms sync initialized");
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] FlywheelRenderBridge: FogUniforms init failed (non-fatal): {}", t.getMessage());
        }

        // FrameUniforms.firstWrite — reset previous-frame matrices when camera mode changes.
        try {
            Class<?> cFrameUniforms = cl.loadClass("dev.engine_room.flywheel.backend.engine.uniform.FrameUniforms");
            fFrameUniformsFirstWrite = cFrameUniforms.getDeclaredField("firstWrite");
            fFrameUniformsFirstWrite.setAccessible(true);
            debugInfo("[ReForged] FlywheelRenderBridge: FrameUniforms firstWrite access initialized");
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] FlywheelRenderBridge: FrameUniforms init failed (non-fatal): {}", t.getMessage());
        }
    }

    /** Whether Flywheel rendering bridge is available. */
    public static boolean isAvailable() {
        ensureInit();
        return available;
    }

    // =============================================================
    // Event firing
    // =============================================================

    /** Call FlwImpl.freezeRegistries() — must happen before EndClientResourceReloadEvent. */
    public static void freezeRegistries() {
        ensureInit();
        if (!available) return;
        try {
            mFreezeRegistries.invoke(null);
            LOGGER.info("[ReForged] FlywheelRenderBridge: freezeRegistries() called");
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] FlywheelRenderBridge: freezeRegistries() failed: {}", t.getMessage());
        }
    }

    /**
     * Fire EndClientResourceReloadEvent through ModLoader.postEvent() → fallback dispatch.
     */
    public static void fireEndClientResourceReloadEvent(Minecraft mc, ResourceManager rm,
                                                         boolean isInitial, Optional<Throwable> error) {
        ensureInit();
        if (!available) return;
        try {
            Object event = ctorEndReloadEvent.newInstance(mc, rm, isInitial, error);
            NeoForgeEventBusAdapter.dispatchFallback(event);
            LOGGER.info("[ReForged] FlywheelRenderBridge: EndClientResourceReloadEvent dispatched (initial={})", isInitial);
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] FlywheelRenderBridge: EndClientResourceReloadEvent failed: {}", t.getMessage(), t);
        }
    }

    /**
     * Fire ReloadLevelRendererEvent through the game bus fallback dispatch.
     */
    public static void fireReloadLevelRendererEvent(ClientLevel level) {
        dispatchReloadLevelRendererEvent(level, RELOAD_READD_DELAY_FRAMES);
    }

    /**
     * Dispatch Flywheel's level renderer reload event and schedule a block entity re-add pass.
     */
    private static void dispatchReloadLevelRendererEvent(ClientLevel level, int reAddDelay) {
        ensureInit();
        if (!available || level == null) return;
        try {
            // Use FlwImplXplat.INSTANCE.dispatchReloadLevelRendererEvent(level)
            // which internally fires the event on NeoForge.EVENT_BUS
            mDispatchReloadLevelRendererEvent.invoke(flwImplXplatInstance, level);
            LOGGER.info("[ReForged] FlywheelRenderBridge: dispatchReloadLevelRendererEvent called");
        } catch (Throwable t) {
            // Fallback: construct and dispatch directly
            try {
                Object event = ctorReloadLevelRendererEvent.newInstance(level);
                NeoForgeEventBusAdapter.dispatchFallback(event);
                LOGGER.info("[ReForged] FlywheelRenderBridge: ReloadLevelRendererEvent dispatched via fallback");
            } catch (Throwable t2) {
                LOGGER.warn("[ReForged] FlywheelRenderBridge: ReloadLevelRendererEvent failed: {}", t2.getMessage());
            }
        }
        // allChanged()/F3+A uses a longer delay to avoid piling onto chunk rebuild work.
        scheduleVisualRefresh(level, reAddDelay, "level renderer reload");
    }

    /**
     * Recreate all loaded block entity visuals by queueing remove+add. This is used as a
     * targeted recovery path when Flywheel's render origin changes but existing visuals still
     * carry positions relative to the old origin.
     */
    private static void refreshAllBlockEntityVisuals(ClientLevel level) {
        if (!available || level == null) return;
        try {
            Object visMgr = getVisualizationManager(level);
            if (visMgr == null) {
                return;
            }
            Object beStorage = mVMBlockEntities.invoke(visMgr);
            var mc = Minecraft.getInstance();
            var player = mc.player;
            if (player == null) {
                return;
            }
            int viewDist = mc.options.getEffectiveRenderDistance();
            int cx = player.blockPosition().getX() >> 4;
            int cz = player.blockPosition().getZ() >> 4;
            int count = 0;
            var chunkSource = level.getChunkSource();
            for (int x = cx - viewDist; x <= cx + viewDist; x++) {
                for (int z = cz - viewDist; z <= cz + viewDist; z++) {
                    net.minecraft.world.level.chunk.LevelChunk chunk = chunkSource.getChunkNow(x, z);
                    if (chunk == null) continue;
                    for (BlockEntity be : chunk.getBlockEntities().values()) {
                        if (be == null || be.isRemoved()) continue;
                        mQueueRemove.invoke(beStorage, be);
                        mQueueAdd.invoke(beStorage, be);
                        count++;
                    }
                }
            }
            LOGGER.info("[ReForged] refreshAllBlockEntityVisuals: refreshed {} block entities", count);
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] refreshAllBlockEntityVisuals failed: {}", t.getMessage());
        }
    }

    /**
     * Recreate all loaded entity visuals. Create/Flywheel uses this for contraption-like
     * entities; keeping it in the same recovery pass prevents entity visuals from retaining
     * stale GPU state after a renderer reload or render-origin transition.
     */
    private static void refreshAllEntityVisuals(ClientLevel level) {
        if (!available || level == null) return;
        try {
            Object entityStorage = getEntityStorage(level);
            if (entityStorage == null) {
                return;
            }
            int count = 0;
            for (Entity entity : level.entitiesForRendering()) {
                if (entity == null || !entity.isAlive()) continue;
                mQueueRemove.invoke(entityStorage, entity);
                mQueueAdd.invoke(entityStorage, entity);
                count++;
            }
            LOGGER.info("[ReForged] refreshAllEntityVisuals: refreshed {} entities", count);
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] refreshAllEntityVisuals failed: {}", t.getMessage());
        }
    }

    private static void refreshAllLoadedVisuals(ClientLevel level, String reason) {
        refreshAllBlockEntityVisuals(level);
        refreshAllEntityVisuals(level);
        debugInfo("[ReForged] FlywheelRenderBridge: queued loaded visual refresh ({})", reason);
    }

    // =============================================================
    // GL state synchronization
    // =============================================================

    /**
     * Sync the current OpenGL state into Flywheel's {@code GlStateTracker}.
     *
     * <p>Flywheel's {@code GlStateManagerMixin} normally tracks GL state changes (buffer bindings,
     * VAO binding, shader program) so that {@code GlStateTracker.getRestoreState()} captures the
     * correct values before Flywheel starts rendering, and {@code State.restore()} restores them
     * afterward. Since Flywheel's mixins cannot be applied (Flywheel is in NeoModClassLoader,
     * mixins target TransformingClassLoader classes), the tracker fields remain at 0.</p>
     *
     * <p>This method queries the actual GL state via {@code glGetInteger} and writes it directly
     * into the tracker's fields, ensuring correct state save/restore.</p>
     */
    private static void syncGlState() {
        if (fGlStateTrackerProgram == null) return;
        try {
            // Sync shader program
            fGlStateTrackerProgram.setInt(null, GL20.glGetInteger(GL20.GL_CURRENT_PROGRAM));

            // Sync VAO
            fGlStateTrackerVao.setInt(null, GL30.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING));

            // Sync buffer bindings
            int[] buffers = (int[]) fGlStateTrackerBUFFERS.get(null);
            if (glBindingEnums != null) {
                int len = Math.min(buffers.length, glBindingEnums.length);
                for (int i = 0; i < len; i++) {
                    buffers[i] = GL11.glGetInteger(glBindingEnums[i]);
                }
            }
        } catch (Throwable t) {
            // Best-effort — don't log every frame
        }
    }

    /**
     * Sync Minecraft's diffuse light directions into Flywheel's LevelUniforms.
     * Replaces the functionality of GlStateManagerMixin.flywheel$onSetupLevelDiffuseLighting().
     *
     * <p>Minecraft's standard level lighting uses two fixed direction vectors defined in
     * {@code com.mojang.blaze3d.platform.Lighting}: DIFFUSE_LIGHT_0 = normalize(0.2, 1.0, -0.7)
     * and DIFFUSE_LIGHT_1 = normalize(-0.2, 1.0, 0.7). The GlStateManagerMixin would normally
     * capture these values when {@code GlStateManager._setupLevelDiffuseLighting()} is called.
     * Since the mixin cannot apply, we write the values directly.</p>
     */
    private static void syncLightDirections() {
        if (lightDirectionsSynced || flwLight0Direction == null) return;
        // These match Lighting.DIFFUSE_LIGHT_0 and DIFFUSE_LIGHT_1 in Minecraft
        flwLight0Direction.set(0.2f, 1.0f, -0.7f).normalize();
        flwLight1Direction.set(-0.2f, 1.0f, 0.7f).normalize();
        lightDirectionsSynced = true;
        debugInfo("[ReForged] FlywheelRenderBridge: light directions synced: light0={}, light1={}",
                flwLight0Direction, flwLight1Direction);
    }

    /**
     * Update Flywheel's fog uniforms from current RenderSystem state.
     * Replaces the functionality of Flywheel's RenderSystemMixin.
     */
    private static void syncFogUniforms() {
        if (mFogUniformsUpdate == null) return;
        try {
            mFogUniformsUpdate.invoke(null);
        } catch (Throwable t) {
            // Best-effort
        }
    }

    /**
     * Reset Flywheel's cached previous-frame camera matrices so camera mode switches do not
     * interpolate against a stale first/third-person view.
     */
    private static void resetFrameUniformsHistory() {
        if (fFrameUniformsFirstWrite == null) return;
        try {
            fFrameUniformsFirstWrite.setBoolean(null, true);
            // Verify the write actually landed (classloader sanity check).
            boolean readBack = fFrameUniformsFirstWrite.getBoolean(null);
            if (!readBack) {
                LOGGER.warn("[ReForged] resetFrameUniformsHistory: firstWrite set true but read back false (classloader mismatch?)");
            }
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] resetFrameUniformsHistory failed: {}", t.getMessage());
        }
    }

    // =============================================================
    // Per-frame render hooks
    // =============================================================

    /**
     * Called at the start of LevelRenderer.renderLevel() — creates the Flywheel RenderContext.
     */
    public static void beginRender(LevelRenderer renderer, ClientLevel level,
                                    RenderBuffers buffers, Matrix4f viewMatrix,
                                    Matrix4f projMatrix, Camera camera, float partialTick) {
        ensureInit();
        if (!available || level == null) return;
        try {
            // Sync GL state into Flywheel's GlStateTracker before any Flywheel rendering.
            // This replaces the functionality of Flywheel's GlStateManagerMixin which can't apply.
            syncGlState();
            // Sync fog uniforms — replaces Flywheel's RenderSystemMixin
            syncFogUniforms();

            // Handle deferred visual refresh after reload/origin transitions.
            if (reAddDelayFrames > 0) {
                reAddDelayFrames--;
            } else if (reAddDelayFrames == 0) {
                reAddDelayFrames = -1;
                refreshAllLoadedVisuals(reAddPendingLevel, reAddReason);
                reAddPendingLevel = null;
                reAddReason = "reload";
            }

            // IMPORTANT: Copy matrices to prevent aliasing issues — vanilla's renderLevel()
            // may modify these Matrix4f instances after our injection point, and since
            // RenderContextImpl stores the references (not copies), FrameUniforms.update()
            // would see modified values when afterEntities() fires later in the frame.
            Matrix4f viewCopy = new Matrix4f(viewMatrix);
            Matrix4f projCopy = new Matrix4f(projMatrix);

            Object cameraType = Minecraft.getInstance().options.getCameraType();
            if (lastCameraType == null) {
                lastCameraType = cameraType;
            } else if (!lastCameraType.equals(cameraType)) {
                lastCameraType = cameraType;
                // Only clear FrameUniforms.firstWrite so PREV matrices get matched on the
                // next frame — avoiding motion-blur style interpolation spikes across the
                // camera-mode switch. DO NOT dispatch ReloadLevelRendererEvent here: that
                // is more destructive than needed. A bounded visual refresh drops stale
                // static instance transforms while keeping the renderer alive.
                resetFrameUniformsHistory();
                scheduleVisualRefresh(level, 0, "camera mode changed");
                beginRenderLogCount = 0;
                backendLogCount = 0;
                beforeBELogCount = 0;
                debugInfo("[ReForged] FlywheelRenderBridge: camera mode changed to {}, reset frame history",
                        cameraType);
            }

            Object renderCtx = mCreateRenderContext.invoke(null,
                    renderer, level, buffers, viewCopy, projCopy, camera, partialTick);
            currentRenderContext = renderCtx;

            // Brief frame logging for explicitly enabled diagnostics.
            if (shouldLogDebugFrame(beginRenderLogCount, DEBUG_FRAME_LOG_LIMIT)) {
                var camPos = camera.getPosition();
                var camLook = camera.getLookVector();
                LOGGER.info("[ReForged] beginRender frame={} cam={}: camPos=({},{},{}), camRot=(xRot={},yRot={}), look=({},{},{}), pt={}",
                        beginRenderLogCount,
                        cameraType,
                        String.format("%.2f", camPos.x), String.format("%.2f", camPos.y), String.format("%.2f", camPos.z),
                        String.format("%.2f", camera.getXRot()), String.format("%.2f", camera.getYRot()),
                        String.format("%.3f", camLook.x()), String.format("%.3f", camLook.y()), String.format("%.3f", camLook.z()),
                        String.format("%.4f", partialTick));
                LOGGER.info("[ReForged] beginRender matrix frame={}: view.m30/m31/m32=({},{},{}), proj.m30/m31/m32=({},{},{})",
                        beginRenderLogCount,
                        String.format("%.4f", viewCopy.m30()), String.format("%.4f", viewCopy.m31()), String.format("%.4f", viewCopy.m32()),
                        String.format("%.4f", projCopy.m30()), String.format("%.4f", projCopy.m31()), String.format("%.4f", projCopy.m32()));
                // View matrix 3x3 rotation: row 2 (forward/-Z row) tells us where the camera is looking.
                // view.m02/m12/m22 is the -Z (view forward) direction expressed in world space (column-major).
                LOGGER.info("[ReForged] beginRender view3x3 frame={}: fwd(m02,m12,m22)=({},{},{}), up(m01,m11,m21)=({},{},{})",
                        beginRenderLogCount,
                        String.format("%.3f", viewCopy.m02()), String.format("%.3f", viewCopy.m12()), String.format("%.3f", viewCopy.m22()),
                        String.format("%.3f", viewCopy.m01()), String.format("%.3f", viewCopy.m11()), String.format("%.3f", viewCopy.m21()));
            }

            Object visMgr = getVisualizationManager(level);
            if (visMgr != null) {
                if (shouldLogDebugFrame(backendLogCount, DEBUG_FRAME_LOG_LIMIT) && mVMGetEngineImpl != null) {
                    try {
                        Object engineImpl = mVMGetEngineImpl.invoke(visMgr);
                        String drawManagerClass = "null";
                        if (engineImpl != null) {
                            Field drawManagerField = engineImpl.getClass().getDeclaredField("drawManager");
                            drawManagerField.setAccessible(true);
                            Object drawManager = drawManagerField.get(engineImpl);
                            if (drawManager != null) {
                                drawManagerClass = drawManager.getClass().getName();
                            }
                        }
                        LOGGER.info("[ReForged] FlywheelRenderBridge: engineImpl={} visMgr={}",
                                engineImpl == null ? "null" : engineImpl.getClass().getName(),
                                visMgr.getClass().getName());
                        LOGGER.info("[ReForged] FlywheelRenderBridge: drawManager={}", drawManagerClass);
                        backendLogCount++;
                    } catch (Throwable ignored) {
                    }
                }
                net.minecraft.core.Vec3i oldOrigin = (net.minecraft.core.Vec3i) mVMRenderOrigin.invoke(visMgr);
                Object dispatcher = mVMRenderDispatcher.invoke(visMgr);
                mOnStartLevelRender.invoke(dispatcher, renderCtx);
                net.minecraft.core.Vec3i newOrigin = (net.minecraft.core.Vec3i) mVMRenderOrigin.invoke(visMgr);
                if (oldOrigin != null && newOrigin != null && !oldOrigin.equals(newOrigin)) {
                    if (lastRenderOrigin == null || !lastRenderOrigin.equals(newOrigin)) {
                        resetFrameUniformsHistory();
                        scheduleVisualRefresh(level, 0, "render origin changed");
                        debugInfo("[ReForged] FlywheelRenderBridge: render origin changed {} -> {}, reset frame history",
                                oldOrigin, newOrigin);
                    }
                }
                lastRenderOrigin = newOrigin;
            } else if (shouldLogDebugFrame(beginRenderLogCount, DEBUG_FRAME_LOG_LIMIT)) {
                LOGGER.warn("[ReForged] FlywheelRenderBridge: beginRender — VisualizationManager.get(level) returned null");
            }
            beginRenderLogCount++;
        } catch (Throwable t) {
            if (beginRenderFailureCount++ < FAILURE_LOG_LIMIT) {
                LOGGER.warn("[ReForged] FlywheelRenderBridge: beginRender failed: {}", t.getMessage(), t);
            }
            currentRenderContext = null;
        }
    }

    /**
     * Called before block entity rendering in LevelRenderer.renderLevel().
     */
    public static void beforeBlockEntities(ClientLevel level) {
        ensureInit();
        if (!available || currentRenderContext == null || level == null) return;
        try {
            // NOTE: Do NOT force-normalize GL capability state here via RenderSystem.*() —
            // that bypasses Flywheel's internal GlStateTracker and leaves its cached state
            // out of sync with the real driver state, which can cause subsequent Flywheel
            // draws to skip uniform/state rebinds and produce garbled output. Just run the
            // normal tracker sync which captures program/VAO/buffer bindings.
            syncGlState(); // Re-sync tracker before Flywheel's main draw pass
            syncFogUniforms();
            syncLightDirections(); // Ensure light directions are set

            // Diagnostic: first 6 frames per camera mode — capture the real GL state,
            // render origin, and Flywheel engine state as Flywheel sees it.
            if (shouldLogDebugFrame(beforeBELogCount, DEBUG_FRAME_LOG_LIMIT)) {
                Object cam = lastCameraType;
                int prog = org.lwjgl.opengl.GL20.glGetInteger(org.lwjgl.opengl.GL20.GL_CURRENT_PROGRAM);
                int vao = org.lwjgl.opengl.GL30.glGetInteger(org.lwjgl.opengl.GL30.GL_VERTEX_ARRAY_BINDING);
                boolean depthTest = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
                boolean blend = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_BLEND);
                boolean cull = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_CULL_FACE);
                int depthFunc = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_DEPTH_FUNC);
                LOGGER.info("[ReForged] beforeBE gl#{} cam={}: prog={}, vao={}, depthTest={}, blend={}, cull={}, depthFunc=0x{}",
                        beforeBELogCount, cam, prog, vao, depthTest, blend, cull, Integer.toHexString(depthFunc));
                try {
                    Object visMgr = getVisualizationManager(level);
                    if (visMgr != null) {
                        net.minecraft.core.Vec3i ro = (net.minecraft.core.Vec3i) mVMRenderOrigin.invoke(visMgr);
                        LOGGER.info("[ReForged] beforeBE engine#{} cam={}: renderOrigin={}", beforeBELogCount, cam, ro);
                    }
                } catch (Throwable ignored) {}
            }
            beforeBELogCount++;

            Object visMgr = getVisualizationManager(level);
            if (visMgr != null) {
                Object dispatcher = mVMRenderDispatcher.invoke(visMgr);
                mAfterEntities.invoke(dispatcher, currentRenderContext);
            }
        } catch (Throwable t) {
            if (beforeBlockEntitiesFailureCount++ < FAILURE_LOG_LIMIT) {
                LOGGER.warn("[ReForged] FlywheelRenderBridge: beforeBlockEntities failed: {}", t.getMessage(), t);
            }
        }
    }

    /**
     * Called before render crumbling in LevelRenderer.renderLevel().
     */
    @SuppressWarnings("rawtypes")
    public static void beforeRenderCrumbling(ClientLevel level, Long2ObjectMap destructionProgress) {
        ensureInit();
        if (!available || currentRenderContext == null || level == null) return;
        try {
            Object visMgr = getVisualizationManager(level);
            if (visMgr != null) {
                Object dispatcher = mVMRenderDispatcher.invoke(visMgr);
                mBeforeCrumbling.invoke(dispatcher, currentRenderContext, destructionProgress);
            }
        } catch (Throwable t) {
            LOGGER.debug("[ReForged] FlywheelRenderBridge: beforeRenderCrumbling failed: {}", t.getMessage());
        }
    }

    /**
     * Called at the end of LevelRenderer.renderLevel().
     */
    public static void endRender() {
        currentRenderContext = null;
    }

    // =============================================================
    // Entity rendering skip
    // =============================================================

    /**
     * Check if vanilla rendering should be skipped for this entity (Flywheel handles it).
     */
    public static boolean shouldSkipVanillaRender(Entity entity) {
        ensureInit();
        if (!available) return false;
        try {
            if (!supportsVisualization(entity.level())) return false;
            return (boolean) mSkipVanillaRender.invoke(null, entity);
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Check if vanilla rendering should be skipped for this block entity (Flywheel handles it).
     * This replaces the check that SectionCompilerMixin would normally perform during chunk
     * section compilation to prevent double rendering.
     */
    public static boolean shouldSkipBlockEntityVanillaRender(BlockEntity blockEntity) {
        ensureInit();
        if (!available || mSkipVanillaRenderBE == null) return false;
        try {
            if (requiresVanillaBlockEntityOverlay(blockEntity)) return false;
            Level level = blockEntity.getLevel();
            if (level == null) return false;
            if (!supportsVisualization(level)) return false;
            return (boolean) mSkipVanillaRenderBE.invoke(null, blockEntity);
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean requiresVanillaBlockEntityOverlay(BlockEntity blockEntity) {
        String className = blockEntity.getClass().getName();
        // Flywheel draws the block body, but Create's vanilla BER supplies panel text/item overlays.
        return className.equals("com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity")
                || className.endsWith(".FactoryPanelBlockEntity");
    }

    // =============================================================
    // Visual tracking (block entity add/remove)
    // =============================================================

    /**
     * Notify Flywheel that a block entity was added (e.g., from chunk loading).
     */
    public static void onBlockEntityAdded(Level level, BlockEntity blockEntity) {
        ensureInit();
        if (!available || level == null) return;
        try {
            Object beStorage = getBlockEntityStorage(level);
            if (beStorage == null) return;
            mQueueAdd.invoke(beStorage, blockEntity);
            if (shouldLogDebugFrame(addLogCount, DEBUG_BE_LOG_LIMIT)) {
                LOGGER.info("[ReForged] onBlockEntityAdded: type={}, pos={}, level={}",
                        blockEntity.getType(), blockEntity.getBlockPos(), level.getClass().getSimpleName());
            }
            addLogCount++;
        } catch (Throwable t) {
            if (blockEntityLifecycleFailureCount++ < FAILURE_LOG_LIMIT) {
                LOGGER.warn("[ReForged] onBlockEntityAdded FAILED: type={}, pos={}, error={}",
                        blockEntity.getType(), blockEntity.getBlockPos(), t.getMessage());
            }
        }
    }

    /**
     * Notify Flywheel that a block entity was removed.
     */
    public static void onBlockEntityRemoved(Level level, BlockEntity blockEntity) {
        ensureInit();
        if (!available || level == null) return;
        try {
            Object beStorage = getBlockEntityStorage(level);
            if (beStorage == null) return;
            mQueueRemove.invoke(beStorage, blockEntity);
        } catch (Throwable t) {
            if (blockEntityLifecycleFailureCount++ < FAILURE_LOG_LIMIT) {
                LOGGER.warn("[ReForged] onBlockEntityRemoved FAILED: type={}, pos={}, error={}",
                        blockEntity.getType(), blockEntity.getBlockPos(), t.getMessage());
            }
        }
    }

    /**
     * Mirror Flywheel's visualmanage.LevelRendererMixin#setBlockDirty hook so visuals are
     * refreshed when vanilla marks a block entity section dirty.
     */
    public static void onBlockEntityChanged(ClientLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        ensureInit();
        if (!available || level == null) return;
        try {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) return;

            Object beStorage = getBlockEntityStorage(level);
            if (beStorage == null) return;

            if (!oldState.equals(newState)) {
                mQueueRemove.invoke(beStorage, blockEntity);
                mQueueAdd.invoke(beStorage, blockEntity);
            } else {
                mQueueUpdate.invoke(beStorage, blockEntity);
            }
        } catch (Throwable t) {
            if (blockEntityLifecycleFailureCount++ < FAILURE_LOG_LIMIT) {
                LOGGER.warn("[ReForged] onBlockEntityChanged FAILED: pos={}, error={}", pos, t.getMessage());
            }
        }
    }

    /**
     * Mirror Flywheel's ClientChunkCacheMixin so shader lighting stays in sync for visuals.
     */
    public static void onLightUpdate(ClientLevel level, SectionPos sectionPos, LightLayer lightLayer) {
        ensureInit();
        if (!available || level == null || sectionPos == null || lightLayer == null) return;
        try {
            Object visMgr = getVisualizationManager(level);
            if (visMgr != null) {
                mVMOnLightUpdate.invoke(visMgr, sectionPos, lightLayer);
            }
        } catch (Throwable t) {
            // Silent
        }
    }
}
