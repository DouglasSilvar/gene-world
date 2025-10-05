package org.gene.world.chunks.enums;

import com.badlogic.gdx.graphics.Color;

public enum ChunkType {
    GROUND(new Color(0.43f, 0.29f, 0.17f, 1f)); // marrom terra - ajuste como preferir

    public final Color baseColor;

    ChunkType(Color baseColor) {
        this.baseColor = baseColor;
    }
}