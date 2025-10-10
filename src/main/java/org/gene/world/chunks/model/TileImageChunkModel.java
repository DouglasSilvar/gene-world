package org.gene.world.chunks.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import org.gene.world.chunks.enums.Biome;
import org.gene.world.chunks.enums.TileType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class TileImageChunkModel implements ChunkModel {

    // Cache estático para evitar carregar as mesmas imagens repetidamente
    private static final Map<String, Pixmap> TILE_CACHE = new HashMap<>(); // Chave agora é String

    private final Pixmap tilePixmap;
    private final int tileWidth;
    private final int tileHeight;

    public TileImageChunkModel(TileType type, Biome primary, Biome secondary) {
        String filename = type.getFilename(primary, secondary); // Gera o nome do arquivo
        synchronized (TILE_CACHE) {
            if (!TILE_CACHE.containsKey(filename)) {
                FileHandle fh = Gdx.files.internal(filename);
                TILE_CACHE.put(filename, new Pixmap(fh));
            }
        }
        this.tilePixmap = TILE_CACHE.get(filename);
        this.tileWidth = tilePixmap.getWidth();
        this.tileHeight = tilePixmap.getHeight();
    }

    @Override
    public void fill(Pixmap pixmap, int worldX0, int worldY0, long seed) {
        final int w = pixmap.getWidth();
        final int h = pixmap.getHeight();

        // Esta lógica de tiling garante que a textura se repita perfeitamente
        // entre os chunks, caso a imagem base seja menor que o chunk.
        // Para tiles do mesmo tamanho do chunk, ela simplesmente copia a imagem.
        for (int y = 0; y < h; y++) {
            int wy = worldY0 + y;
            int sy = positiveMod(wy, tileHeight);

            for (int x = 0; x < w; x++) {
                int wx = worldX0 + x;
                int sx = positiveMod(wx, tileWidth);

                int rgba = tilePixmap.getPixel(sx, sy);
                pixmap.drawPixel(x, y, rgba);
            }
        }
    }

    // Método para limpar o cache quando o jogo fechar
    public static void disposeCache() {
        synchronized (TILE_CACHE) {
            for (Pixmap pixmap : TILE_CACHE.values()) {
                pixmap.dispose();
            }
            TILE_CACHE.clear();
        }
    }

    private static int positiveMod(int a, int m) {
        int r = a % m;
        return (r < 0) ? (r + m) : r;
    }
}