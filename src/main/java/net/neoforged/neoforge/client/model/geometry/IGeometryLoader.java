package net.neoforged.neoforge.client.model.geometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * NeoForge's IGeometryLoader — extends Forge's so that NeoForge loaders can be
 * stored directly in Forge's {@code GeometryLoaderManager.LOADERS} map.
 *
 * <p>Since the type bound {@code T extends IUnbakedGeometry<T>} (NeoForge) implies
 * {@code T extends net.minecraftforge.*.IUnbakedGeometry<T>} (through inheritance),
 * this interface is a proper subtype of Forge's IGeometryLoader. The {@code read()}
 * method has the same erased signature, so no bridge method is needed.</p>
 */
public interface IGeometryLoader<T extends IUnbakedGeometry<T>>
        extends net.minecraftforge.client.model.geometry.IGeometryLoader<T> {

    @Override
    T read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException;
}
