package org.gene.world.chunks.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;

/**
 * Model de chunk baseado em imagem tileable.
 * Amostra a textura em coordenadas de MUNDO (worldX/worldY) com wrap toroidal.
 * Assim, vizinhos se encaixam sem costura entre chunks.
 */
public class GroundImageChunkModel implements ChunkModel {

    // cache único da imagem
    private static Pixmap TILE;
    private static int TW, TH;

    public GroundImageChunkModel() {
        if (TILE == null) {
            FileHandle fh = Gdx.files.internal("assets/ground.png");
            TILE = new Pixmap(fh);
            TW = TILE.getWidth();
            TH = TILE.getHeight();
        }
    }

    @Override
    public void fill(Pixmap pixmap, int worldX0, int worldY0, long seed) {
        final int w = pixmap.getWidth();
        final int h = pixmap.getHeight();

        for (int y = 0; y < h; y++) {
            int wy = worldY0 + y;
            int sy = positiveMod(wy, TH);

            for (int x = 0; x < w; x++) {
                int wx = worldX0 + x;
                int sx = positiveMod(wx, TW);

                int rgba = TILE.getPixel(sx, sy);
                pixmap.drawPixel(x, y, rgba);
            }
        }
    }

    private static int positiveMod(int a, int m) {
        int r = a % m;
        return (r < 0) ? (r + m) : r;
    }
}
