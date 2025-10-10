package org.gene.world.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.gene.world.chunks.enums.TileType;
import org.gene.world.chunks.factory.ChunkFactory;
import org.gene.world.chunks.model.Chunk;
import org.gene.world.chunks.model.TileImageChunkModel;

import java.util.ArrayList;
import java.util.List;

public class World {
    private final List<Chunk> chunks = new ArrayList<>();
    private final long seed;

    public World(int chunksX, int chunksY, int chunkSize) {
        this.seed = "A".hashCode();
        MapGenerator generator = new MapGenerator(seed);
        MapGenerator.GeneratedTile[][] mapData = generator.generateMap(chunksX, chunksY);

        for (int cy = 0; cy < chunksY; cy++) {
            for (int cx = 0; cx < chunksX; cx++) {
                int screenX = cx * chunkSize;
                int screenY = cy * chunkSize;
                int worldX0 = cx * chunkSize;
                int worldY0 = cy * chunkSize;

                // Pega o tipo de tile gerado para esta posição
                MapGenerator.GeneratedTile tileInfo = mapData[cx][cy];
                var model = ChunkFactory.modelFor(tileInfo.type(), tileInfo.primary(), tileInfo.secondary());
                chunks.add(new Chunk(chunkSize, screenX, screenY, worldX0, worldY0, seed, model));
            }
        }
    }

    public void render(SpriteBatch batch) { for (var c : chunks) c.render(batch); }

    public void dispose() {
        for (var c : chunks) c.dispose();
        // Limpa o cache de imagens estático
        TileImageChunkModel.disposeCache();
    }
}