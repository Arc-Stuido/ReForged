package org.xiyu.reforged.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.IoSupplier;

/**
 * Adapts NeoForge pack data that Forge 1.21 can read but interprets more strictly.
 */
public final class NeoForgePackResources implements PackResources {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final byte[] FALLBACK_PACK_MCMETA = """
            {
              "pack": {
                "description": "ReForged NeoForge mod resources",
                "pack_format": 32
              }
            }
            """.getBytes(StandardCharsets.UTF_8);

    private final PackResources delegate;

    public NeoForgePackResources(PackResources delegate) {
        this.delegate = delegate;
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... path) {
        IoSupplier<InputStream> supplier = delegate.getRootResource(path);
        if (supplier == null && path.length == 1 && PackResources.PACK_META.equals(path[0])) {
            return () -> new ByteArrayInputStream(FALLBACK_PACK_MCMETA);
        }
        return supplier;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        return wrapResource(type, location, delegate.getResource(type, location));
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        delegate.listResources(type, namespace, path,
                (location, supplier) -> output.accept(location, wrapResource(type, location, supplier)));
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return delegate.getNamespaces(type);
    }

    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) throws IOException {
        return delegate.getMetadataSection(serializer);
    }

    @Override
    public PackLocationInfo location() {
        return delegate.location();
    }

    @Override
    public String packId() {
        return delegate.packId();
    }

    @Override
    public Optional<KnownPack> knownPackInfo() {
        return delegate.knownPackInfo();
    }

    @Override
    public void close() {
        delegate.close();
    }

    private static IoSupplier<InputStream> wrapResource(PackType type, ResourceLocation location,
                                                         IoSupplier<InputStream> supplier) {
        if (supplier == null || !shouldNormalize(type, location)) {
            return supplier;
        }

        return () -> {
            byte[] bytes;
            try (InputStream in = supplier.get()) {
                bytes = in.readAllBytes();
            }
            return new ByteArrayInputStream(normalizeEntityTypeTag(bytes));
        };
    }

    private static boolean shouldNormalize(PackType type, ResourceLocation location) {
        String path = location.getPath();
        return type == PackType.SERVER_DATA
                && path.startsWith("tags/entity_type/")
                && path.endsWith(".json");
    }

    private static byte[] normalizeEntityTypeTag(byte[] bytes) {
        try {
            JsonElement root = JsonParser.parseReader(new InputStreamReader(
                    new ByteArrayInputStream(bytes), StandardCharsets.UTF_8));
            if (!root.isJsonObject()) {
                return bytes;
            }

            JsonObject object = root.getAsJsonObject();
            JsonElement values = object.get("values");
            if (values == null || !values.isJsonArray()) {
                return bytes;
            }

            JsonArray normalized = new JsonArray();
            boolean changed = false;
            for (JsonElement value : values.getAsJsonArray()) {
                if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                    JsonObject optionalEntry = new JsonObject();
                    optionalEntry.addProperty("id", value.getAsString());
                    optionalEntry.addProperty("required", false);
                    normalized.add(optionalEntry);
                    changed = true;
                } else {
                    normalized.add(value);
                }
            }

            if (!changed) {
                return bytes;
            }

            object.add("values", normalized);
            return GSON.toJson(object).getBytes(StandardCharsets.UTF_8);
        } catch (Throwable ignored) {
            return bytes;
        }
    }
}
