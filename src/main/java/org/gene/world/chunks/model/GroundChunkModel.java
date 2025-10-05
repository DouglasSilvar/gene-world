package org.gene.world.chunks.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;

import java.util.Random;

/**
 * Chunk 100% terra: preenche o Pixmap inteiro com a mesma cor.
 * (Depois podemos adicionar pequenas variações procedurais, ruído, grãos etc.)
 */
public class GroundChunkModel implements ChunkModel {

    private final Color baseColor;

    public GroundChunkModel(Color baseColor) {
        this.baseColor = baseColor;
    }

    @Override
    public void fill(Pixmap pixmap, int worldX0, int worldY0, long seed) {
        int w = pixmap.getWidth();
        int h = pixmap.getHeight();

        // Escalas do ruído (em "pixels de mundo")
        float baseFreq = 1f / 28f;  // frequência principal
        float lacunarity = 2.1f;    // multiplica a freq a cada oitava
        float gain = 0.55f;         // reduz amplitude a cada oitava
        int octaves = 3;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // Coordenadas de MUNDO (impede costuras entre chunks)
                int X = worldX0 + x;
                int Y = worldY0 + y;

                // fBm: soma várias oitavas de value noise interpolado
                float amp = 1f, freq = baseFreq, fbm = 0f, norm = 0f;
                for (int o = 0; o < octaves; o++) {
                    fbm += amp * valueNoise(X * freq, Y * freq, seed + o * 101);
                    norm += amp;
                    amp *= gain;
                    freq *= lacunarity;
                }
                fbm /= norm; // [0..1]

                // Curvar levemente o contraste
                float v = fbm;
                v = v * v * (3f - 2f * v); // smoothstep extra

                // Mapear para variação de cor “terra”
                // leve tendência a escurecer/verder g para quebrar brilho
                float delta = (v - 0.5f) * 0.35f; // ~[-0.175 .. +0.175]

                float r = clamp(baseColor.r + delta);
                float g = clamp(baseColor.g + delta * 0.85f);
                float b = clamp(baseColor.b + delta * 0.55f);

                pixmap.setColor(r, g, b, 1f);
                pixmap.drawPixel(x, y);
            }
        }
    }

    // ------------------- util -------------------

    private static float clamp(float v) { return Math.min(1f, Math.max(0f, v)); }

    // Value noise interpolado (tile-free) em float coords
    private static float valueNoise(float xf, float yf, long seed) {
        int x0 = fastFloor(xf);
        int y0 = fastFloor(yf);
        int x1 = x0 + 1;
        int y1 = y0 + 1;

        float tx = xf - x0;
        float ty = yf - y0;

        float sx = smoothstep(tx);
        float sy = smoothstep(ty);

        float v00 = hash01(x0, y0, seed);
        float v10 = hash01(x1, y0, seed);
        float v01 = hash01(x0, y1, seed);
        float v11 = hash01(x1, y1, seed);

        float ix0 = lerp(v00, v10, sx);
        float ix1 = lerp(v01, v11, sx);
        return lerp(ix0, ix1, sy); // [0..1]
    }

    private static int fastFloor(float v) { int i = (int)v; return v < i ? i - 1 : i; }
    private static float smoothstep(float t){ return t*t*(3f - 2f*t); }
    private static float lerp(float a, float b, float t){ return a + (b - a) * t; }

    // Hash determinístico → [0..1]
    private static float hash01(int x, int y, long seed) {
        long h = seed;
        h ^= (x * 0x9E3779B97F4A7C15L);
        h ^= (y * 0xC2B2AE3D27D4EB4FL);
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        h ^= (h >>> 31);
        // converte para float [0..1]
        return ((h >>> 40) & 0xFFFFFF) / (float)0xFFFFFF;
    }
}