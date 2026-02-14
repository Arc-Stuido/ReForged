package org.xiyu.reforged.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.util.DependencySorter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.*;

/**
 * Fix StackOverflowError in {@link DependencySorter#isCyclic}.
 *
 * <p>The vanilla implementation uses recursive DFS without a visited set,
 * causing infinite recursion when the dependency graph contains non-target
 * cycles. This replaces it with iterative BFS + visited tracking.</p>
 */
@Mixin(DependencySorter.class)
public abstract class DependencySorterMixin<K, V extends DependencySorter.Entry<K>> {

    /**
     * @reason Fix infinite recursion by using iterative BFS with visited set
     * @author ReForged
     */
    @Overwrite(remap = false)
    private static <K> boolean isCyclic(Multimap<K, K> edges, K target, K current) {
        Set<K> visited = new HashSet<>();
        Deque<K> queue = new ArrayDeque<>();
        queue.add(current);
        while (!queue.isEmpty()) {
            K node = queue.poll();
            if (!visited.add(node)) continue;
            Collection<K> deps = edges.get(node);
            if (deps.contains(target)) return true;
            queue.addAll(deps);
        }
        return false;
    }
}
