package org.gene.world.world;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import org.gene.world.chunks.enums.ChunkType;

public class GeneWorldGame extends ApplicationAdapter {

    public static final int SCREEN_SIZE = 1000;   // px
    public static final int CHUNK_SIZE  = 100;    // px por chunk (100x100)
    public static final int CHUNKS_PER_AXIS = SCREEN_SIZE / CHUNK_SIZE; // 10

    private SpriteBatch batch;
    private World world;

    @Override
    public void create() {
        batch = new SpriteBatch();
        // 🔑 garante espaço de coordenadas exatamente 0..1000 x 0..1000 (pixel-perfect)
        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, SCREEN_SIZE, SCREEN_SIZE));

        world = new World(CHUNKS_PER_AXIS, CHUNKS_PER_AXIS, CHUNK_SIZE, ChunkType.GROUND);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.disableBlending();  // 🔑 evita bleed de alpha entre texturas adjacentes
        world.render(batch);
        batch.enableBlending();
        batch.end();
    }

    @Override
    public void dispose() {
        if (world != null) world.dispose();
        if (batch != null) batch.dispose();
    }
}
