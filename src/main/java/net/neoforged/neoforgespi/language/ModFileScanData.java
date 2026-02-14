package net.neoforged.neoforgespi.language;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.objectweb.asm.Type;

/**
 * Shim: NeoForge's ModFileScanData.
 * <p>
 * Mirrors Forge's {@code net.minecraftforge.forgespi.language.ModFileScanData}.
 * Can wrap a Forge instance or stand alone with empty data.
 */
public class ModFileScanData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Set<AnnotationData> annotations = new LinkedHashSet<>();
    private final Set<ClassData> classes = new LinkedHashSet<>();

    public Set<ClassData> getClasses() {
        return classes;
    }

    public Set<AnnotationData> getAnnotations() {
        return annotations;
    }

    /**
     * Returns a stream of AnnotationData for annotations matching the given annotation class and target element type.
     * This is a NeoForge API method used by mods like Twilight Forest for bean/annotation scanning.
     */
    public Stream<AnnotationData> getAnnotatedBy(Class<? extends Annotation> annotationClass, ElementType targetType) {
        Type annotationType = Type.getType(annotationClass);
        LOGGER.info("[ReForged DEBUG] getAnnotatedBy: annotationType={} targetType={} totalAnnotations={}",
                annotationType, targetType, annotations.size());
        long count = annotations.stream()
                .filter(ad -> annotationType.equals(ad.annotationType()))
                .filter(ad -> targetType == null || targetType.equals(ad.targetType()))
                .count();
        LOGGER.info("[ReForged DEBUG] getAnnotatedBy found {} matches for {}", count, annotationType);
        if (count == 0 && !annotations.isEmpty()) {
            // Log first few stored annotation types for debugging
            annotations.stream().limit(5).forEach(ad ->
                    LOGGER.info("[ReForged DEBUG]   stored annotation: type={} target={} class={}",
                            ad.annotationType(), ad.targetType(), ad.clazz()));
        }
        return annotations.stream()
                .filter(ad -> annotationType.equals(ad.annotationType()))
                .filter(ad -> targetType == null || targetType.equals(ad.targetType()));
    }

    /**
     * Wraps Forge's ModFileScanData by copying its annotation and class data.
     */
    public static ModFileScanData wrap(net.minecraftforge.forgespi.language.ModFileScanData forgeScanData) {
        if (forgeScanData == null) {
            LOGGER.warn("[ReForged DEBUG] wrap() called with null forgeScanData!");
            return new ModFileScanData();
        }
        LOGGER.info("[ReForged DEBUG] wrap() called: forgeAnnotations={} forgeClasses={}",
                forgeScanData.getAnnotations().size(), forgeScanData.getClasses().size());
        ModFileScanData neo = new ModFileScanData();
        // Copy annotations
        for (net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData ad : forgeScanData.getAnnotations()) {
            neo.annotations.add(new AnnotationData(
                    ad.annotationType(), ad.targetType(), ad.clazz(), ad.memberName(), ad.annotationData()));
        }
        // Copy classes
        for (net.minecraftforge.forgespi.language.ModFileScanData.ClassData cd : forgeScanData.getClasses()) {
            neo.classes.add(new ClassData(cd.clazz(), cd.parent(), cd.interfaces()));
        }
        return neo;
    }

    public record ClassData(Type clazz, Type parent, Set<Type> interfaces) {}

    public record AnnotationData(Type annotationType, ElementType targetType, Type clazz, String memberName, Map<String, Object> annotationData) {}
}
