package net.neoforged.neoforge.client.event;

import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired to calculate detached camera distance.
 */
public class CalculateDetachedCameraDistanceEvent extends Event {
    private double distance;

    public CalculateDetachedCameraDistanceEvent(double distance) {
        this.distance = distance;
    }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
}
