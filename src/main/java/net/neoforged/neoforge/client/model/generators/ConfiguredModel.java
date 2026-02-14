package net.neoforged.neoforge.client.model.generators;

/**
 * Stub: NeoForge ConfiguredModel — represents a model with rotation/UV lock configuration.
 */
public class ConfiguredModel {
    public final ModelFile model;
    public final int rotationX;
    public final int rotationY;
    public final boolean uvLock;
    public final int weight;

    public ConfiguredModel(ModelFile model) {
        this(model, 0, 0, false, 1);
    }

    public ConfiguredModel(ModelFile model, int rotationX, int rotationY, boolean uvLock, int weight) {
        this.model = model;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.uvLock = uvLock;
        this.weight = weight;
    }

    public static ConfiguredModel[] allRotations(ModelFile model, boolean uvlock) {
        return new ConfiguredModel[]{new ConfiguredModel(model)};
    }

    public static ConfiguredModel[] allYRotations(ModelFile model, int x, boolean uvlock) {
        return new ConfiguredModel[]{new ConfiguredModel(model, x, 0, uvlock, 1)};
    }

    public static Builder<?> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private ModelFile model;
        private int rotationX;
        private int rotationY;
        private boolean uvLock;
        private int weight = 1;

        public Builder<T> modelFile(ModelFile model) { this.model = model; return this; }
        public Builder<T> rotationX(int x) { this.rotationX = x; return this; }
        public Builder<T> rotationY(int y) { this.rotationY = y; return this; }
        public Builder<T> uvLock(boolean uvLock) { this.uvLock = uvLock; return this; }
        public Builder<T> weight(int weight) { this.weight = weight; return this; }

        public ConfiguredModel[] buildLast() {
            return new ConfiguredModel[]{new ConfiguredModel(model, rotationX, rotationY, uvLock, weight)};
        }

        @SuppressWarnings("unchecked")
        public T addModel() { return (T) this; }
    }
}
