package net.neoforged.neoforge.client.event;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;
import java.util.function.Supplier;

/**
 * NeoForge wrapper events for entity/block-entity renderer registration.
 * Each inner class wraps the corresponding Forge event via a constructor.
 */
public class EntityRenderersEvent {

    /** Wraps {@code net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers}. */
    public static class RegisterRenderers {
        private final net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers delegate;

        public RegisterRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers delegate) {
            this.delegate = delegate;
        }

        public <T extends Entity> void registerEntityRenderer(EntityType<T> entityType,
                                                               EntityRendererProvider<T> provider) {
            delegate.registerEntityRenderer(entityType, provider);
        }

        public <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<T> type,
                                                                         BlockEntityRendererProvider<T> provider) {
            delegate.registerBlockEntityRenderer(type, provider);
        }
    }

    /** Wraps {@code net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static class RegisterLayerDefinitions {
        private final net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions delegate;

        public RegisterLayerDefinitions(net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions delegate) {
            this.delegate = delegate;
        }

        public void registerLayerDefinition(ModelLayerLocation layerLocation, Supplier<LayerDefinition> supplier) {
            delegate.registerLayerDefinition(layerLocation, supplier);
        }
    }

    /** Wraps {@code net.minecraftforge.client.event.EntityRenderersEvent.AddLayers}. */
    public static class AddLayers extends EntityRenderersEvent {
        private final net.minecraftforge.client.event.EntityRenderersEvent.AddLayers delegate;

        public AddLayers(net.minecraftforge.client.event.EntityRenderersEvent.AddLayers delegate) {
            this.delegate = delegate;
        }

        public Set<PlayerSkin.Model> getSkins() {
            return delegate.getSkins();
        }

        @SuppressWarnings("unchecked")
        public <R extends EntityRenderer<? extends Player>> R getPlayerSkin(PlayerSkin.Model skinName) {
            return delegate.getPlayerSkin(skinName);
        }

        @SuppressWarnings("unchecked")
        public <T extends LivingEntity, R extends EntityRenderer<T>> R getEntityRenderer(EntityType<? extends T> entityType) {
            return delegate.getEntityRenderer(entityType);
        }

        public EntityModelSet getEntityModels() {
            return delegate.getEntityModels();
        }

        public EntityRendererProvider.Context getContext() {
            return delegate.getContext();
        }
    }

    public static class CreateSkullModels extends EntityRenderersEvent {}
}
