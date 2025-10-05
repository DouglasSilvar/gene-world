package org.gene.world.chunks.model;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Chunk {
    private final int size;           // 100
    private final int screenX, screenY;

    private final Pixmap inner;       // 100x100 (gerado pelo modelo)
    private final Pixmap extruded;    // 102x102 (borda 1px replicada)
    private final Texture texture;    // da imagem 102x102
    private final TextureRegion region; // recorte 100x100 (remove a borda)

    public Chunk(int size, int screenX, int screenY,
                 int worldX0, int worldY0, long seed,
                 ChunkModel model) {
        this.size = size;
        this.screenX = screenX;
        this.screenY = screenY;

        // 1) gera o conteúdo real 100x100
        inner = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        model.fill(inner, worldX0, worldY0, seed);

        // 2) cria um pixmap 102x102 e cola o 100x100 no centro (1,1)
        extruded = new Pixmap(size + 2, size + 2, Pixmap.Format.RGBA8888);
        extruded.drawPixmap(inner,
                0, 0, size, size,   // src
                1, 1, size, size);  // dst

        // 2.a) extrusão das LINHAS (top/bottom)
        extruded.drawPixmap(inner,
                0, 0, size, 1,      // primeira linha do inner
                1, 0, size, 1);     // topo do extruded
        extruded.drawPixmap(inner,
                0, size - 1, size, 1, // última linha do inner
                1, size + 1, size, 1);// base do extruded

        // 2.b) extrusão das COLUNAS (left/right)
        extruded.drawPixmap(inner,
                0, 0, 1, size,      // primeira coluna
                0, 1, 1, size);     // esquerda do extruded
        extruded.drawPixmap(inner,
                size - 1, 0, 1, size, // última coluna
                size + 1, 1, 1, size);// direita do extruded

        // 2.c) cantos
        extruded.drawPixmap(inner, 0, 0, 1, 1, 0, 0, 1, 1);                         // TL
        extruded.drawPixmap(inner, size - 1, 0, 1, 1, size + 1, 0, 1, 1);           // TR
        extruded.drawPixmap(inner, 0, size - 1, 1, 1, 0, size + 1, 1, 1);           // BL
        extruded.drawPixmap(inner, size - 1, size - 1, 1, 1, size + 1, size + 1, 1, 1); // BR

        // 3) textura da imagem extrudada + recorte 100x100
        texture = new Texture(extruded);
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);

        // recorta o miolo sem a borda (1..size)
        region = new TextureRegion(texture, 1, 1, size, size);
    }

    public void render(SpriteBatch batch) {
        // desenha o recorte (100x100) exatamente na célula do chunk
        batch.draw(region, screenX, screenY, size, size);
    }

    public void dispose() {
        texture.dispose();
        inner.dispose();
        extruded.dispose();
    }
}
