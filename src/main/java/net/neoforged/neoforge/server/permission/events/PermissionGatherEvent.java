package net.neoforged.neoforge.server.permission.events;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

/**
 * Shim: NeoForge PermissionGatherEvent with Handler and Nodes inner classes.
 */
public abstract class PermissionGatherEvent extends Event {

    public static class Handler extends PermissionGatherEvent {
        private final Map<ResourceLocation, Object> availableHandlers = new HashMap<>();

        public Handler() {}

        public Map<ResourceLocation, Object> getAvailablePermissionHandlerFactories() {
            return Collections.unmodifiableMap(availableHandlers);
        }

        public void addPermissionHandler(ResourceLocation identifier, Object handlerFactory) {
            if (identifier == null) throw new NullPointerException("Permission handler identifier cannot be null!");
            if (handlerFactory == null) throw new NullPointerException("Permission handler cannot be null!");
            if (this.availableHandlers.containsKey(identifier))
                throw new IllegalArgumentException("Attempted to overwrite permission handler " + identifier + ", this is not allowed.");
            this.availableHandlers.put(identifier, handlerFactory);
        }
    }

    public static class Nodes extends PermissionGatherEvent {
        private final Set<PermissionNode<?>> nodes = new HashSet<>();

        public Nodes() {}

        public Collection<PermissionNode<?>> getNodes() {
            return Collections.unmodifiableCollection(this.nodes);
        }

        public void addNodes(PermissionNode<?>... nodes) {
            for (PermissionNode<?> node : nodes) {
                if (!this.nodes.add(node))
                    throw new IllegalArgumentException("Tried to register duplicate PermissionNode '" + node.getNodeName() + "'");
            }
        }

        public void addNodes(Iterable<PermissionNode<?>> nodes) {
            for (PermissionNode<?> node : nodes) {
                if (!this.nodes.add(node))
                    throw new IllegalArgumentException("Tried to register duplicate PermissionNode '" + node.getNodeName() + "'");
            }
        }
    }
}
