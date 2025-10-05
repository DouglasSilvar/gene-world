package org.gene.world.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.gene.world.chunks.enums.ChunkType;
import org.gene.world.chunks.factory.ChunkFactory;
import org.gene.world.chunks.model.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Mantém uma grade 2D de Chunks e desenha todos.
 * Aqui simplificamos: 10x10 na tela, preenchidos por um único tipo de chunk.
 */
public class World {
    private final List<Chunk> chunks = new ArrayList<>();
    private final long seed;

    public World(int chunksX, int chunksY, int chunkSize, ChunkType type) {
        this.seed = new Random(12345L).nextLong(); // fixe ou passe de fora
        var model = ChunkFactory.modelFor(type);

        for (int cy = 0; cy < chunksY; cy++) {
            for (int cx = 0; cx < chunksX; cx++) {
                int screenX = cx * chunkSize;
                int screenY = cy * chunkSize;

                // Origem de MUNDO para este chunk (em pixels)
                int worldX0 = cx * chunkSize;
                int worldY0 = cy * chunkSize;

                chunks.add(new Chunk(chunkSize, screenX, screenY, worldX0, worldY0, seed, model));
            }
        }
    }

    public void render(SpriteBatch batch) { for (var c : chunks) c.render(batch); }
    public void dispose() { for (var c : chunks) c.dispose(); }
}
