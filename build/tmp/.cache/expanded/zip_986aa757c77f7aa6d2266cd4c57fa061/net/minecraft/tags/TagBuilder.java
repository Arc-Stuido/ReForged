package net.minecraft.tags;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class TagBuilder implements net.minecraftforge.common.extensions.IForgeRawTagBuilder {
    private final List<TagEntry> entries = new ArrayList<>();

    public static TagBuilder create() {
        return new TagBuilder();
    }

    public List<TagEntry> build() {
        return List.copyOf(this.entries);
    }

    public TagBuilder add(TagEntry p_215903_) {
        this.entries.add(p_215903_);
        return this;
    }

    public TagBuilder addElement(ResourceLocation p_215901_) {
        return this.add(TagEntry.element(p_215901_));
    }

    public TagBuilder addOptionalElement(ResourceLocation p_215906_) {
        return this.add(TagEntry.optionalElement(p_215906_));
    }

    public TagBuilder addTag(ResourceLocation p_215908_) {
        return this.add(TagEntry.tag(p_215908_));
    }

    public TagBuilder addOptionalTag(ResourceLocation p_215910_) {
        return this.add(TagEntry.optionalTag(p_215910_));
    }

    /** Forge: Used for datagen */
    private final List<TagEntry> removeEntries = new ArrayList<>();
    private boolean replace = false;

    public java.util.stream.Stream<TagEntry> getRemoveEntries() { // should this return the List instead? Might end up with mem leaks from unterminated streams otherwise -Paint_Ninja
        return this.removeEntries.stream();
    }

    /** Forge: Add an entry to be removed from this tag in datagen */
    public TagBuilder remove(TagEntry entry) {
        this.removeEntries.add(entry);
        return this;
    }

    /** Forge: Set the replace property of this tag */
    public TagBuilder replace(boolean value) {
        this.replace = value;
        return this;
    }

    /** Forge: Is this tag set to replace or not? */
    public boolean isReplace() {
        return this.replace;
    }
}
