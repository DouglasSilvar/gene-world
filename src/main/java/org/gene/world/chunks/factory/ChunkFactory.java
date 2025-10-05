package org.gene.world.chunks.factory;

import org.gene.world.chunks.enums.ChunkType;
import org.gene.world.chunks.model.ChunkModel;
import org.gene.world.chunks.model.GroundImageChunkModel;

public class ChunkFactory {
    public static ChunkModel modelFor(ChunkType type) {
        return switch (type) {
            case GROUND -> new GroundImageChunkModel();
        };
    }
}
