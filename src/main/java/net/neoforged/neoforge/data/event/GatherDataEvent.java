package net.neoforged.neoforge.data.event;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.eventbus.api.Event;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Stub: NeoForge GatherDataEvent — fired during data generation.
 */
public class GatherDataEvent extends Event implements IModBusEvent {
    private final DataGenerator generator;
    private final ExistingFileHelper existingFileHelper;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public GatherDataEvent() {
        this.generator = null;
        this.existingFileHelper = new ExistingFileHelper();
        this.lookupProvider = CompletableFuture.completedFuture(null);
    }

    public GatherDataEvent(DataGenerator generator, ExistingFileHelper existingFileHelper,
                           CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.generator = generator;
        this.existingFileHelper = existingFileHelper;
        this.lookupProvider = lookupProvider;
    }

    public DataGenerator getGenerator() { return generator; }
    public ExistingFileHelper getExistingFileHelper() { return existingFileHelper; }
    public CompletableFuture<HolderLookup.Provider> getLookupProvider() { return lookupProvider; }
    public boolean includeServer() { return true; }
    public boolean includeClient() { return true; }
    public boolean includeDev() { return false; }
    public boolean includeReports() { return false; }
    public boolean validate() { return false; }
}
