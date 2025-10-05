package org.gene.world.chunks.model;

import com.badlogic.gdx.graphics.Pixmap;

/**
 * Contrato para modelos de geração "pixel-a-pixel" de um Chunk (100x100).
 * Implementações preenchem completamente o Pixmap.
 */
public interface ChunkModel {
    void fill(Pixmap pixmap, int worldX0, int worldY0, long seed);
}