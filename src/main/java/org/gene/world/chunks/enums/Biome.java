package org.gene.world.chunks.enums;

// Representa o "material" do chunk: Terra, Água, Areia, etc.
public enum Biome {
    GROUND,
    WATER;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}