package org.xiyu.reforged.core;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;

/**
 * Processes NeoForge enum extension declarations ({@code META-INF/enumextensions.json})
 * and injects new enum constants into Minecraft/Forge enum types at runtime.
 *
 * <p>Uses {@code sun.misc.Unsafe} to:</p>
 * <ul>
 *   <li>Create enum instances without calling constructors</li>
 *   <li>Set private fields (name, ordinal, type-specific fields)</li>
 *   <li>Update {@code $VALUES} array</li>
 *   <li>Clear JDK enum caches so {@code valueOf()} finds new constants</li>
 * </ul>
 */
public final class EnumExtensionHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    private EnumExtensionHandler() {}

    /**
     * Process enum extensions from all NeoForge mod JARs.
     * Must be called BEFORE any mod classes are loaded.
     */
    public static void processAll(List<Path> jars, URLClassLoader neoClassLoader) {
        int totalExtensions = 0;
        for (Path jar : jars) {
            try (JarFile jf = new JarFile(jar.toFile())) {
                var entry = jf.getJarEntry("META-INF/enumextensions.json");
                if (entry == null) continue;

                try (InputStream is = jf.getInputStream(entry);
                     InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                    JsonArray entries = root.getAsJsonArray("entries");
                    if (entries == null) continue;

                    LOGGER.info("[ReForged] Processing {} enum extension(s) from {}",
                            entries.size(), jar.getFileName());

                    for (JsonElement elem : entries) {
                        try {
                            JsonObject ext = elem.getAsJsonObject();
                            String enumClassName = ext.get("enum").getAsString().replace('/', '.');
                            String constantName = ext.get("name").getAsString();

                            if (injectEnumConstant(enumClassName, constantName)) {
                                totalExtensions++;
                            }
                        } catch (Exception e) {
                            LOGGER.warn("[ReForged] Failed to process enum extension entry: {}",
                                    elem, e);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("[ReForged] Could not read enumextensions.json from {}: {}",
                        jar.getFileName(), e.getMessage());
            }
        }

        if (totalExtensions > 0) {
            LOGGER.info("[ReForged] Successfully injected {} enum constant(s)", totalExtensions);
        }
    }

    /**
     * Inject a new constant into an enum class using Unsafe.
     *
     * @param enumClassName fully-qualified class name of the enum
     * @param constantName  name of the new enum constant
     * @return true if successfully injected
     */
    @SuppressWarnings("unchecked")
    private static boolean injectEnumConstant(String enumClassName, String constantName) {
        try {
            // Load the enum class from the game classloader (TransformingClassLoader)
            Class<?> enumClass = Class.forName(enumClassName);
            if (!enumClass.isEnum()) {
                LOGGER.warn("[ReForged] {} is not an enum, skipping constant {}",
                        enumClassName, constantName);
                return false;
            }

            // Check if constant already exists
            for (Object existing : enumClass.getEnumConstants()) {
                if (((Enum<?>) existing).name().equals(constantName)) {
                    LOGGER.debug("[ReForged] Enum constant {}.{} already exists, skipping",
                            enumClass.getSimpleName(), constantName);
                    return true;
                }
            }

            sun.misc.Unsafe unsafe = getUnsafe();

            // 1. Create new enum instance without calling constructor
            Object newConstant = unsafe.allocateInstance(enumClass);

            // 2. Set Enum.name
            Field nameField = Enum.class.getDeclaredField("name");
            long nameOffset = unsafe.objectFieldOffset(nameField);
            unsafe.putObject(newConstant, nameOffset, constantName);

            // 3. Set Enum.ordinal
            Field ordinalField = Enum.class.getDeclaredField("ordinal");
            long ordinalOffset = unsafe.objectFieldOffset(ordinalField);
            Object[] currentValues = enumClass.getEnumConstants();
            int newOrdinal = currentValues.length;
            unsafe.putInt(newConstant, ordinalOffset, newOrdinal);

            // 4. Set type-specific fields with reasonable defaults
            setTypeSpecificFields(unsafe, enumClass, newConstant, constantName, newOrdinal);

            // 5. Update $VALUES array
            Field valuesField = enumClass.getDeclaredField("$VALUES");
            long valuesOffset = unsafe.staticFieldOffset(valuesField);
            Object oldValues = unsafe.getObject(enumClass, valuesOffset);
            int oldLen = Array.getLength(oldValues);
            Object newValues = Array.newInstance(enumClass, oldLen + 1);
            System.arraycopy(oldValues, 0, newValues, 0, oldLen);
            Array.set(newValues, oldLen, newConstant);
            unsafe.putObject(enumClass, valuesOffset, newValues);

            // 6. Clear JDK's cached enum constant arrays so valueOf() picks up the new constant
            clearEnumCache(unsafe, enumClass);

            LOGGER.info("[ReForged] Injected enum constant {}.{} (ordinal={})",
                    enumClass.getSimpleName(), constantName, newOrdinal);
            return true;

        } catch (ClassNotFoundException e) {
            LOGGER.warn("[ReForged] Enum class {} not found, skipping constant {}",
                    enumClassName, constantName);
            return false;
        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to inject enum constant {}.{}",
                    enumClassName, constantName, e);
            return false;
        }
    }

    /**
     * Set type-specific fields for known enum types.
     * For unknown types, fields remain at Java defaults (0/null/false).
     */
    private static void setTypeSpecificFields(sun.misc.Unsafe unsafe, Class<?> enumClass,
                                               Object constant, String name, int ordinal) {
        String className = enumClass.getName();
        try {
            switch (className) {
                case "net.minecraft.world.item.Rarity" -> {
                    // Forge Rarity: int id, String name, ChatFormatting color
                    setField(unsafe, enumClass, constant, "id", ordinal);
                    setField(unsafe, enumClass, constant, "name", name.toLowerCase(Locale.ROOT));
                    // Use LIGHT_PURPLE for twilight themed rarity
                    setField(unsafe, enumClass, constant, "color",
                            net.minecraft.ChatFormatting.LIGHT_PURPLE);
                }
                case "net.minecraft.world.damagesource.DamageEffects" -> {
                    // DamageEffects: Supplier<SoundEvent> sound
                    // Leave at null — TF will set it via the parameter provider
                    // The TFEnumExtensions method returns the sound supplier
                }
                case "net.minecraft.world.item.ItemDisplayContext" -> {
                    // ItemDisplayContext: byte id, String name, @Nullable String fallback
                    // Use a unique negative ID to avoid conflicts
                    setFieldByte(unsafe, enumClass, constant, "id", (byte)(-100 - ordinal));
                    String displayName = name.toLowerCase(Locale.ROOT);
                    if (displayName.startsWith("twilightforest_")) {
                        displayName = displayName.substring("twilightforest_".length());
                    }
                    setField(unsafe, enumClass, constant, "name", displayName);
                }
                case "net.minecraft.world.entity.vehicle.Boat$Type" -> {
                    // Boat$Type: Block planks, String name
                    // The name field is critical for model path generation (e.g. "boat/twilight_oak")
                    // Extract name from constant: TWILIGHTFOREST_TWILIGHT_OAK → twilight_oak
                    // Must avoid collision with vanilla names (e.g. MANGROVE already exists)
                    String boatName = name.toLowerCase(Locale.ROOT);
                    if (boatName.startsWith("twilightforest_")) {
                        boatName = boatName.substring("twilightforest_".length());
                    }
                    // Check for collision with existing Boat.Type names
                    boolean collision = false;
                    for (Object existing : enumClass.getEnumConstants()) {
                        try {
                            Field nameF = findField(enumClass, "name");
                            if (nameF != null) {
                                nameF.setAccessible(true);
                                String existingName = (String) nameF.get(existing);
                                if (boatName.equals(existingName)) {
                                    collision = true;
                                    break;
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                    if (collision) {
                        boatName = "tf_" + boatName;
                    }
                    setField(unsafe, enumClass, constant, "name", boatName);
                    setField(unsafe, enumClass, constant, "planks",
                            net.minecraft.world.level.block.Blocks.OAK_PLANKS); // placeholder
                }
                default -> {
                    if (className.contains("GrassColorModifier")) {
                        // BiomeSpecialEffects$GrassColorModifier has a ColorModifier field
                        // Leave at null — rendering may not work perfectly but won't crash
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("[ReForged] Could not set type-specific fields for {}.{}: {}",
                    enumClass.getSimpleName(), name, e.getMessage());
        }
    }

    /**
     * Set a declared field value using Unsafe.
     */
    private static void setField(sun.misc.Unsafe unsafe, Class<?> clazz, Object instance,
                                  String fieldName, Object value) {
        try {
            Field f = findField(clazz, fieldName);
            if (f == null) return;
            long offset = unsafe.objectFieldOffset(f);
            if (value instanceof Integer i) {
                unsafe.putInt(instance, offset, i);
            } else if (value instanceof Long l) {
                unsafe.putLong(instance, offset, l);
            } else if (value instanceof Boolean b) {
                unsafe.putBoolean(instance, offset, b);
            } else if (value instanceof Byte bt) {
                unsafe.putByte(instance, offset, bt);
            } else {
                unsafe.putObject(instance, offset, value);
            }
        } catch (Exception e) {
            LOGGER.debug("[ReForged] Could not set field {}.{}: {}", clazz.getSimpleName(), fieldName, e.getMessage());
        }
    }

    /**
     * Set a byte field using Unsafe.
     */
    private static void setFieldByte(sun.misc.Unsafe unsafe, Class<?> clazz, Object instance,
                                      String fieldName, byte value) {
        try {
            Field f = findField(clazz, fieldName);
            if (f == null) return;
            long offset = unsafe.objectFieldOffset(f);
            unsafe.putByte(instance, offset, value);
        } catch (Exception e) {
            LOGGER.debug("[ReForged] Could not set byte field {}.{}: {}", clazz.getSimpleName(), fieldName, e.getMessage());
        }
    }

    /**
     * Find a field in a class or its superclasses.
     */
    private static Field findField(Class<?> clazz, String name) {
        Class<?> c = clazz;
        while (c != null) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Clear JDK internal enum caches so {@code Enum.valueOf()} and
     * {@code Class.getEnumConstants()} return updated values.
     */
    private static void clearEnumCache(sun.misc.Unsafe unsafe, Class<?> enumClass) {
        try {
            // Clear Class.enumConstants
            Field enumConstants = Class.class.getDeclaredField("enumConstants");
            long offset1 = unsafe.objectFieldOffset(enumConstants);
            unsafe.putObject(enumClass, offset1, null);
        } catch (NoSuchFieldException e) {
            LOGGER.debug("[ReForged] Could not find enumConstants field");
        }

        try {
            // Clear Class.enumConstantDirectory (used by valueOf())
            Field enumConstantDirectory = Class.class.getDeclaredField("enumConstantDirectory");
            long offset2 = unsafe.objectFieldOffset(enumConstantDirectory);
            unsafe.putObject(enumClass, offset2, null);
        } catch (NoSuchFieldException e) {
            LOGGER.debug("[ReForged] Could not find enumConstantDirectory field");
        }
    }

    private static sun.misc.Unsafe getUnsafe() {
        try {
            Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (sun.misc.Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Cannot get Unsafe instance", e);
        }
    }
}
