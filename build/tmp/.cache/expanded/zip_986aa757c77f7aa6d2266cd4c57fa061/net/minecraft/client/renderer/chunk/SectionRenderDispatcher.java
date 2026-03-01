package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.SectionBufferBuilderPool;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SectionRenderDispatcher {
    private static final int MAX_HIGH_PRIORITY_QUOTA = 2;
    private final PriorityBlockingQueue<SectionRenderDispatcher.RenderSection.CompileTask> toBatchHighPriority = Queues.newPriorityBlockingQueue();
    private final Queue<SectionRenderDispatcher.RenderSection.CompileTask> toBatchLowPriority = Queues.newLinkedBlockingDeque();
    private int highPriorityQuota = 2;
    private final Queue<Runnable> toUpload = Queues.newConcurrentLinkedQueue();
    final SectionBufferBuilderPack fixedBuffers;
    private final SectionBufferBuilderPool bufferPool;
    private volatile int toBatchCount;
    private volatile boolean closed;
    private final ProcessorMailbox<Runnable> mailbox;
    private final Executor executor;
    ClientLevel level;
    final LevelRenderer renderer;
    private Vec3 camera = Vec3.ZERO;
    final SectionCompiler sectionCompiler;

    public SectionRenderDispatcher(
        ClientLevel p_299878_,
        LevelRenderer p_299032_,
        Executor p_298480_,
        RenderBuffers p_310401_,
        BlockRenderDispatcher p_343142_,
        BlockEntityRenderDispatcher p_344654_
    ) {
        this.level = p_299878_;
        this.renderer = p_299032_;
        this.fixedBuffers = p_310401_.fixedBufferPack();
        this.bufferPool = p_310401_.sectionBufferPool();
        this.executor = p_298480_;
        this.mailbox = ProcessorMailbox.create(p_298480_, "Section Renderer");
        this.mailbox.tell(this::runTask);
        this.sectionCompiler = new SectionCompiler(p_343142_, p_344654_);
    }

    public void setLevel(ClientLevel p_298968_) {
        this.level = p_298968_;
    }

    private void runTask() {
        if (!this.closed && !this.bufferPool.isEmpty()) {
            SectionRenderDispatcher.RenderSection.CompileTask sectionrenderdispatcher$rendersection$compiletask = this.pollTask();
            if (sectionrenderdispatcher$rendersection$compiletask != null) {
                SectionBufferBuilderPack sectionbufferbuilderpack = Objects.requireNonNull(this.bufferPool.acquire());
                this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
                CompletableFuture.supplyAsync(
                        Util.wrapThreadWithTaskName(
                            sectionrenderdispatcher$rendersection$compiletask.name(),
                            () -> sectionrenderdispatcher$rendersection$compiletask.doTask(sectionbufferbuilderpack)
                        ),
                        this.executor
                    )
                    .thenCompose(p_298155_ -> (CompletionStage<SectionRenderDispatcher.SectionTaskResult>)p_298155_)
                    .whenComplete((p_299295_, p_297995_) -> {
                        if (p_297995_ != null) {
                            Minecraft.getInstance().delayCrash(CrashReport.forThrowable(p_297995_, "Batching sections"));
                        } else {
                            this.mailbox.tell(() -> {
                                if (p_299295_ == SectionRenderDispatcher.SectionTaskResult.SUCCESSFUL) {
                                    sectionbufferbuilderpack.clearAll();
                                } else {
                                    sectionbufferbuilderpack.discardAll();
                                }

                                this.bufferPool.release(sectionbufferbuilderpack);
                                this.runTask();
                            });
                        }
                    });
            }
        }
    }

    @Nullable
    private SectionRenderDispatcher.RenderSection.CompileTask pollTask() {
        if (this.highPriorityQuota <= 0) {
            SectionRenderDispatcher.RenderSection.CompileTask sectionrenderdispatcher$rendersection$compiletask = this.toBatchLowPriority.poll();
            if (sectionrenderdispatcher$rendersection$compiletask != null) {
                this.highPriorityQuota = 2;
                return sectionrenderdispatcher$rendersection$compiletask;
            }
        }

        SectionRenderDispatcher.RenderSection.CompileTask sectionrenderdispatcher$rendersection$compiletask1 = this.toBatchHighPriority.poll();
        if (sectionrenderdispatcher$rendersection$compiletask1 != null) {
            this.highPriorityQuota--;
            return sectionrenderdispatcher$rendersection$compiletask1;
        } else {
            this.highPriorityQuota = 2;
            return this.toBatchLowPriority.poll();
        }
    }

    public String getStats() {
        return String.format(Locale.ROOT, "pC: %03d, pU: %02d, aB: %02d", this.toBatchCount, this.toUpload.size(), this.bufferPool.getFreeBufferCount());
    }

    public int getToBatchCount() {
        return this.toBatchCount;
    }

    public int getToUpload() {
        return this.toUpload.size();
    }

    public int getFreeBufferCount() {
        return this.bufferPool.getFreeBufferCount();
    }

    public void setCamera(Vec3 p_297762_) {
        this.camera = p_297762_;
    }

    public Vec3 getCameraPosition() {
        return this.camera;
    }

    public void uploadAllPendingUploads() {
        Runnable runnable;
        while ((runnable = this.toUpload.poll()) != null) {
            runnable.run();
        }
    }

    public void rebuildSectionSync(SectionRenderDispatcher.RenderSection p_299640_, RenderRegionCache p_297835_) {
        p_299640_.compileSync(p_297835_);
    }

    public void blockUntilClear() {
        this.clearBatchQueue();
    }

    public void schedule(SectionRenderDispatcher.RenderSection.CompileTask p_297747_) {
        if (!this.closed) {
            this.mailbox.tell(() -> {
                if (!this.closed) {
                    if (p_297747_.isHighPriority) {
                        this.toBatchHighPriority.offer(p_297747_);
                    } else {
                        this.toBatchLowPriority.offer(p_297747_);
                    }

                    this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
                    this.runTask();
                }
            });
        }
    }

    public CompletableFuture<Void> uploadSectionLayer(MeshData p_344050_, VertexBuffer p_298938_) {
        return this.closed ? CompletableFuture.completedFuture(null) : CompletableFuture.runAsync(() -> {
            if (p_298938_.isInvalid()) {
                p_344050_.close();
            } else {
                p_298938_.bind();
                p_298938_.upload(p_344050_);
                VertexBuffer.unbind();
            }
        }, this.toUpload::add);
    }

    public CompletableFuture<Void> uploadSectionIndexBuffer(ByteBufferBuilder.Result p_343213_, VertexBuffer p_344049_) {
        return this.closed ? CompletableFuture.completedFuture(null) : CompletableFuture.runAsync(() -> {
            if (p_344049_.isInvalid()) {
                p_343213_.close();
            } else {
                p_344049_.bind();
                p_344049_.uploadIndexBuffer(p_343213_);
                VertexBuffer.unbind();
            }
        }, this.toUpload::add);
    }

    private void clearBatchQueue() {
        while (!this.toBatchHighPriority.isEmpty()) {
            SectionRenderDispatcher.RenderSection.CompileTask sectionrenderdispatcher$rendersection$compiletask = this.toBatchHighPriority.poll();
            if (sectionrenderdispatcher$rendersection$compiletask != null) {
                sectionrenderdispatcher$rendersection$compiletask.cancel();
            }
        }

        while (!this.toBatchLowPriority.isEmpty()) {
            SectionRenderDispatcher.RenderSection.CompileTask sectionrenderdispatcher$rendersection$compiletask1 = this.toBatchLowPriority.poll();
            if (sectionrenderdispatcher$rendersection$compiletask1 != null) {
                sectionrenderdispatcher$rendersection$compiletask1.cancel();
            }
        }

        this.toBatchCount = 0;
    }

    public boolean isQueueEmpty() {
        return this.toBatchCount == 0 && this.toUpload.isEmpty();
    }

    public void dispose() {
        this.closed = true;
        this.clearBatchQueue();
        this.uploadAllPendingUploads();
    }

    @OnlyIn(Dist.CLIENT)
    public static class CompiledSection {
        public static final SectionRenderDispatcher.CompiledSection UNCOMPILED = new SectionRenderDispatcher.CompiledSection() {
            @Override
            public boolean facesCanSeeEachother(Direction p_301280_, Direction p_299155_) {
                return false;
            }
        };
        public static final SectionRenderDispatcher.CompiledSection EMPTY = new SectionRenderDispatcher.CompiledSection() {
            @Override
            public boolean facesCanSeeEachother(Direction p_343413_, Direction p_342431_) {
                return true;
            }
        };
        final Set<RenderType> hasBlocks = new ObjectArraySet<>(RenderType.chunkBufferLayers().size());
        final List<BlockEntity> renderableBlockEntities = Lists.newArrayList();
        VisibilitySet visibilitySet = new VisibilitySet();
        @Nullable
        MeshData.SortState transparencyState;

        public boolean hasNoRenderableLayers() {
            return this.hasBlocks.isEmpty();
        }

        public boolean isEmpty(RenderType p_300861_) {
            return !this.hasBlocks.contains(p_300861_);
        }

        public List<BlockEntity> getRenderableBlockEntities() {
            return this.renderableBlockEntities;
        }

        public boolean facesCanSeeEachother(Direction p_301006_, Direction p_300193_) {
            return this.visibilitySet.visibilityBetween(p_301006_, p_300193_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class RenderSection {
        public static final int SIZE = 16;
        public final int index;
        public final AtomicReference<SectionRenderDispatcher.CompiledSection> compiled = new AtomicReference<>(
            SectionRenderDispatcher.CompiledSection.UNCOMPILED
        );
        private final AtomicInteger initialCompilationCancelCount = new AtomicInteger(0);
        @Nullable
        private SectionRenderDispatcher.RenderSection.RebuildTask lastRebuildTask;
        @Nullable
        private SectionRenderDispatcher.RenderSection.ResortTransparencyTask lastResortTransparencyTask;
        private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
        private final Map<RenderType, VertexBuffer> buffers = RenderType.chunkBufferLayers()
            .stream()
            .collect(Collectors.toMap(p_298649_ -> (RenderType)p_298649_, p_299941_ -> new VertexBuffer(VertexBuffer.Usage.STATIC)));
        private AABB bb;
        private boolean dirty = true;
        final BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos(-1, -1, -1);
        private final BlockPos.MutableBlockPos[] relativeOrigins = Util.make(new BlockPos.MutableBlockPos[6], p_300613_ -> {
            for (int i = 0; i < p_300613_.length; i++) {
                p_300613_[i] = new BlockPos.MutableBlockPos();
            }
        });
        private boolean playerChanged;

        public RenderSection(final int p_299358_, final int p_299044_, final int p_300810_, final int p_299840_) {
            this.index = p_299358_;
            this.setOrigin(p_299044_, p_300810_, p_299840_);
        }

        private boolean doesChunkExistAt(BlockPos p_297611_) {
            return SectionRenderDispatcher.this.level
                    .getChunk(SectionPos.blockToSectionCoord(p_297611_.getX()), SectionPos.blockToSectionCoord(p_297611_.getZ()), ChunkStatus.FULL, false)
                != null;
        }

        public boolean hasAllNeighbors() {
            int i = 24;
            return !(this.getDistToPlayerSqr() > 576.0)
                ? true
                : this.doesChunkExistAt(this.relativeOrigins[Direction.WEST.ordinal()])
                    && this.doesChunkExistAt(this.relativeOrigins[Direction.NORTH.ordinal()])
                    && this.doesChunkExistAt(this.relativeOrigins[Direction.EAST.ordinal()])
                    && this.doesChunkExistAt(this.relativeOrigins[Direction.SOUTH.ordinal()]);
        }

        public AABB getBoundingBox() {
            return this.bb;
        }

        public VertexBuffer getBuffer(RenderType p_298748_) {
            return this.buffers.get(p_298748_);
        }

        public void setOrigin(int p_298099_, int p_299019_, int p_299020_) {
            this.reset();
            this.origin.set(p_298099_, p_299019_, p_299020_);
            this.bb = new AABB(
                (double)p_298099_, (double)p_299019_, (double)p_299020_, (double)(p_298099_ + 16), (double)(p_299019_ + 16), (double)(p_299020_ + 16)
            );

            for (Direction direction : Direction.values()) {
                this.relativeOrigins[direction.ordinal()].set(this.origin).move(direction, 16);
            }
        }

        protected double getDistToPlayerSqr() {
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            double d0 = this.bb.minX + 8.0 - camera.getPosition().x;
            double d1 = this.bb.minY + 8.0 - camera.getPosition().y;
            double d2 = this.bb.minZ + 8.0 - camera.getPosition().z;
            return d0 * d0 + d1 * d1 + d2 * d2;
        }

        public SectionRenderDispatcher.CompiledSection getCompiled() {
            return this.compiled.get();
        }

        private void reset() {
            this.cancelTasks();
            this.compiled.set(SectionRenderDispatcher.CompiledSection.UNCOMPILED);
            this.dirty = true;
        }

        public void releaseBuffers() {
            this.reset();
            this.buffers.values().forEach(VertexBuffer::close);
        }

        public BlockPos getOrigin() {
            return this.origin;
        }

        public void setDirty(boolean p_298731_) {
            boolean flag = this.dirty;
            this.dirty = true;
            this.playerChanged = p_298731_ | (flag && this.playerChanged);
        }

        public void setNotDirty() {
            this.dirty = false;
            this.playerChanged = false;
        }

        public boolean isDirty() {
            return this.dirty;
        }

        public boolean isDirtyFromPlayer() {
            return this.dirty && this.playerChanged;
        }

        public BlockPos getRelativeOrigin(Direction p_299060_) {
            return this.relativeOrigins[p_299060_.ordinal()];
        }

        public boolean resortTransparency(RenderType p_301074_, SectionRenderDispatcher p_298196_) {
            SectionRenderDispatcher.CompiledSection sectionrenderdispatcher$compiledsection = this.getCompiled();
            if (this.lastResortTransparencyTask != null) {
                this.lastResortTransparencyTask.cancel();
            }

            if (!sectionrenderdispatcher$compiledsection.hasBlocks.contains(p_301074_)) {
                return false;
            } else {
                this.lastResortTransparencyTask = new SectionRenderDispatcher.RenderSection.ResortTransparencyTask(this.getDistToPlayerSqr(), sectionrenderdispatcher$compiledsection);
                p_298196_.schedule(this.lastResortTransparencyTask);
                return true;
            }
        }

        protected boolean cancelTasks() {
            boolean flag = false;
            if (this.lastRebuildTask != null) {
                this.lastRebuildTask.cancel();
                this.lastRebuildTask = null;
                flag = true;
            }

            if (this.lastResortTransparencyTask != null) {
                this.lastResortTransparencyTask.cancel();
                this.lastResortTransparencyTask = null;
            }

            return flag;
        }

        public SectionRenderDispatcher.RenderSection.CompileTask createCompileTask(RenderRegionCache p_300037_) {
            boolean flag = this.cancelTasks();
            RenderChunkRegion renderchunkregion = p_300037_.createRegion(SectionRenderDispatcher.this.level, SectionPos.of(this.origin));
            boolean flag1 = this.compiled.get() == SectionRenderDispatcher.CompiledSection.UNCOMPILED;
            if (flag1 && flag) {
                this.initialCompilationCancelCount.incrementAndGet();
            }

            this.lastRebuildTask = new SectionRenderDispatcher.RenderSection.RebuildTask(this.getDistToPlayerSqr(), renderchunkregion, !flag1 || this.initialCompilationCancelCount.get() > 2);
            return this.lastRebuildTask;
        }

        public void rebuildSectionAsync(SectionRenderDispatcher p_299090_, RenderRegionCache p_297331_) {
            SectionRenderDispatcher.RenderSection.CompileTask sectionrenderdispatcher$rendersection$compiletask = this.createCompileTask(p_297331_);
            p_299090_.schedule(sectionrenderdispatcher$rendersection$compiletask);
        }

        void updateGlobalBlockEntities(Collection<BlockEntity> p_300373_) {
            Set<BlockEntity> set = Sets.newHashSet(p_300373_);
            Set<BlockEntity> set1;
            synchronized (this.globalBlockEntities) {
                set1 = Sets.newHashSet(this.globalBlockEntities);
                set.removeAll(this.globalBlockEntities);
                set1.removeAll(p_300373_);
                this.globalBlockEntities.clear();
                this.globalBlockEntities.addAll(p_300373_);
            }

            SectionRenderDispatcher.this.renderer.updateGlobalBlockEntities(set1, set);
        }

        public void compileSync(RenderRegionCache p_298605_) {
            SectionRenderDispatcher.RenderSection.CompileTask sectionrenderdispatcher$rendersection$compiletask = this.createCompileTask(p_298605_);
            sectionrenderdispatcher$rendersection$compiletask.doTask(SectionRenderDispatcher.this.fixedBuffers);
        }

        public boolean isAxisAlignedWith(int p_297900_, int p_299871_, int p_299328_) {
            BlockPos blockpos = this.getOrigin();
            return p_297900_ == SectionPos.blockToSectionCoord(blockpos.getX())
                || p_299328_ == SectionPos.blockToSectionCoord(blockpos.getZ())
                || p_299871_ == SectionPos.blockToSectionCoord(blockpos.getY());
        }

        void setCompiled(SectionRenderDispatcher.CompiledSection p_343239_) {
            this.compiled.set(p_343239_);
            this.initialCompilationCancelCount.set(0);
            SectionRenderDispatcher.this.renderer.addRecentlyCompiledSection(this);
        }

        VertexSorting createVertexSorting() {
            Vec3 vec3 = SectionRenderDispatcher.this.getCameraPosition();
            return VertexSorting.byDistance(
                (float)(vec3.x - (double)this.origin.getX()),
                (float)(vec3.y - (double)this.origin.getY()),
                (float)(vec3.z - (double)this.origin.getZ())
            );
        }

        @OnlyIn(Dist.CLIENT)
        abstract class CompileTask implements Comparable<SectionRenderDispatcher.RenderSection.CompileTask> {
            protected final double distAtCreation;
            protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
            protected final boolean isHighPriority;

            public CompileTask(final double p_300617_, final boolean p_299251_) {
                this.distAtCreation = p_300617_;
                this.isHighPriority = p_299251_;
            }

            public abstract CompletableFuture<SectionRenderDispatcher.SectionTaskResult> doTask(SectionBufferBuilderPack p_300298_);

            public abstract void cancel();

            protected abstract String name();

            public int compareTo(SectionRenderDispatcher.RenderSection.CompileTask p_298947_) {
                return Doubles.compare(this.distAtCreation, p_298947_.distAtCreation);
            }
        }

        @OnlyIn(Dist.CLIENT)
        class RebuildTask extends SectionRenderDispatcher.RenderSection.CompileTask {
            @Nullable
            protected RenderChunkRegion region;

            public RebuildTask(final double p_301300_, @Nullable final RenderChunkRegion p_300496_, final boolean p_299891_) {
                super(p_301300_, p_299891_);
                this.region = p_300496_;
            }

            @Override
            protected String name() {
                return "rend_chk_rebuild";
            }

            @Override
            public CompletableFuture<SectionRenderDispatcher.SectionTaskResult> doTask(SectionBufferBuilderPack p_299595_) {
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                } else if (!RenderSection.this.hasAllNeighbors()) {
                    this.cancel();
                    return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                } else if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                } else {
                    RenderChunkRegion renderchunkregion = this.region;
                    this.region = null;
                    if (renderchunkregion == null) {
                        RenderSection.this.setCompiled(SectionRenderDispatcher.CompiledSection.EMPTY);
                        return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.SUCCESSFUL);
                    } else {
                        SectionPos sectionpos = SectionPos.of(RenderSection.this.origin);
                        SectionCompiler.Results sectioncompiler$results = SectionRenderDispatcher.this.sectionCompiler
                            .compile(sectionpos, renderchunkregion, RenderSection.this.createVertexSorting(), p_299595_);
                        RenderSection.this.updateGlobalBlockEntities(sectioncompiler$results.globalBlockEntities);
                        if (this.isCancelled.get()) {
                            sectioncompiler$results.release();
                            return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                        } else {
                            SectionRenderDispatcher.CompiledSection sectionrenderdispatcher$compiledsection = new SectionRenderDispatcher.CompiledSection();
                            sectionrenderdispatcher$compiledsection.visibilitySet = sectioncompiler$results.visibilitySet;
                            sectionrenderdispatcher$compiledsection.renderableBlockEntities.addAll(sectioncompiler$results.blockEntities);
                            sectionrenderdispatcher$compiledsection.transparencyState = sectioncompiler$results.transparencyState;
                            List<CompletableFuture<Void>> list = new ArrayList<>(sectioncompiler$results.renderedLayers.size());
                            sectioncompiler$results.renderedLayers.forEach((p_340913_, p_340914_) -> {
                                list.add(SectionRenderDispatcher.this.uploadSectionLayer(p_340914_, RenderSection.this.getBuffer(p_340913_)));
                                sectionrenderdispatcher$compiledsection.hasBlocks.add(p_340913_);
                            });
                            return Util.sequenceFailFast(list).handle((p_340916_, p_340917_) -> {
                                if (p_340917_ != null && !(p_340917_ instanceof CancellationException) && !(p_340917_ instanceof InterruptedException)) {
                                    Minecraft.getInstance().delayCrash(CrashReport.forThrowable(p_340917_, "Rendering section"));
                                }

                                if (this.isCancelled.get()) {
                                    return SectionRenderDispatcher.SectionTaskResult.CANCELLED;
                                } else {
                                    RenderSection.this.setCompiled(sectionrenderdispatcher$compiledsection);
                                    return SectionRenderDispatcher.SectionTaskResult.SUCCESSFUL;
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void cancel() {
                this.region = null;
                if (this.isCancelled.compareAndSet(false, true)) {
                    RenderSection.this.setDirty(false);
                }
            }
        }

        @OnlyIn(Dist.CLIENT)
        class ResortTransparencyTask extends SectionRenderDispatcher.RenderSection.CompileTask {
            private final SectionRenderDispatcher.CompiledSection compiledSection;

            public ResortTransparencyTask(final double p_300619_, final SectionRenderDispatcher.CompiledSection p_297742_) {
                super(p_300619_, true);
                this.compiledSection = p_297742_;
            }

            @Override
            protected String name() {
                return "rend_chk_sort";
            }

            @Override
            public CompletableFuture<SectionRenderDispatcher.SectionTaskResult> doTask(SectionBufferBuilderPack p_297366_) {
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                } else if (!RenderSection.this.hasAllNeighbors()) {
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                } else if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                } else {
                    MeshData.SortState meshdata$sortstate = this.compiledSection.transparencyState;
                    if (meshdata$sortstate != null && !this.compiledSection.isEmpty(RenderType.translucent())) {
                        VertexSorting vertexsorting = RenderSection.this.createVertexSorting();
                        ByteBufferBuilder.Result bytebufferbuilder$result = meshdata$sortstate.buildSortedIndexBuffer(
                            p_297366_.buffer(RenderType.translucent()), vertexsorting
                        );
                        if (bytebufferbuilder$result == null) {
                            return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                        } else if (this.isCancelled.get()) {
                            bytebufferbuilder$result.close();
                            return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                        } else {
                            CompletableFuture<SectionRenderDispatcher.SectionTaskResult> completablefuture = SectionRenderDispatcher.this.uploadSectionIndexBuffer(
                                    bytebufferbuilder$result, RenderSection.this.getBuffer(RenderType.translucent())
                                )
                                .thenApply(p_297230_ -> SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                            return completablefuture.handle(
                                (p_301037_, p_300486_) -> {
                                    if (p_300486_ != null && !(p_300486_ instanceof CancellationException) && !(p_300486_ instanceof InterruptedException)) {
                                        Minecraft.getInstance().delayCrash(CrashReport.forThrowable(p_300486_, "Rendering section"));
                                    }

                                    return this.isCancelled.get()
                                        ? SectionRenderDispatcher.SectionTaskResult.CANCELLED
                                        : SectionRenderDispatcher.SectionTaskResult.SUCCESSFUL;
                                }
                            );
                        }
                    } else {
                        return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                    }
                }
            }

            @Override
            public void cancel() {
                this.isCancelled.set(true);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static enum SectionTaskResult {
        SUCCESSFUL,
        CANCELLED;
    }
}