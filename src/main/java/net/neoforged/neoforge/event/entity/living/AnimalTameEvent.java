package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

/**
 * Stub: Fired when an animal is tamed.
 */
public class AnimalTameEvent extends LivingEvent {
    private final Animal animal;
    private final Player tamer;

    public AnimalTameEvent(Animal animal, Player tamer) {
        super(animal);
        this.animal = animal;
        this.tamer = tamer;
    }

    public Animal getAnimal() { return animal; }
    public Player getTamer() { return tamer; }
}
