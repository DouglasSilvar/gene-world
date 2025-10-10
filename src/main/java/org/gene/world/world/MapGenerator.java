package org.gene.world.world;

import org.gene.world.chunks.enums.Biome;
import org.gene.world.chunks.enums.TileType;

public class MapGenerator {
    // Estrutura para retornar toda a informação de um tile gerado
    public record GeneratedTile(TileType type, Biome primary, Biome secondary) {}

    private final long seed;
    private final Biome primaryBiome = Biome.GROUND;
    private final Biome secondaryBiome = Biome.WATER;

    public MapGenerator(long seed) {
        this.seed = seed;
    }

    public GeneratedTile[][] generateMap(int width, int height) {
        // --- FASE 1: Gerar mapa de BIOMAS base (só Ground ou Water) usando ruído ---
        Biome[][] biomeMap = new Biome[width][height];
        float noiseScale = 0.15f;
        float threshold = 0.10f;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float noiseValue = valueNoise(x * noiseScale, y * noiseScale, this.seed);
                biomeMap[x][y] = (noiseValue < threshold) ? secondaryBiome : primaryBiome;
            }
        }

        // --- FASE 2: Determinar a FORMA (TileType) baseada nos vizinhos ---
        GeneratedTile[][] finalMap = new GeneratedTile[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (biomeMap[x][y] == secondaryBiome) {
                    // Se o bioma do tile é o secundário (água), ele é sempre 100% preenchido
                    finalMap[x][y] = new GeneratedTile(TileType.FULL, secondaryBiome, primaryBiome);
                } else {
                    // Se for o primário (terra), calculamos a forma da borda
                    TileType shape = determineShape(x, y, biomeMap);
                    finalMap[x][y] = new GeneratedTile(shape, primaryBiome, secondaryBiome);
                }
            }
        }

        return finalMap;
    }

    /**
     * Determina a FORMA (TileType) para uma coordenada baseada nos vizinhos do bioma primário.
     */
    private TileType determineShape(int x, int y, Biome[][] biomeMap) {
        int width = biomeMap.length;
        int height = biomeMap[0].length;

        // A máscara agora checa por vizinhos que são do BIOMA PRIMÁRIO.
        int mask = 0;
        // Norte
        if (y > 0 && biomeMap[x][y - 1] == primaryBiome) mask += 1;
        // Leste
        if (x < width - 1 && biomeMap[x + 1][y] == primaryBiome) mask += 2;
        // Sul
        if (y < height - 1 && biomeMap[x][y + 1] == primaryBiome) mask += 4;
        // Oeste
        if (x > 0 && biomeMap[x - 1][y] == primaryBiome) mask += 8;

        return switch (mask) {
            case 0 -> TileType.INV_CORNER_SE; // Rodeado por água
            case 1 -> TileType.EDGE_S;        // Vizinho primário só ao Norte
            case 2 -> TileType.EDGE_W;        // ... a Leste
            case 3 -> TileType.CORNER_SW;
            case 4 -> TileType.EDGE_N;        // ... a Sul
            case 5 -> TileType.FULL;          // Conexão vertical (usamos FULL por simplicidade)
            case 6 -> TileType.CORNER_NW;
            case 7 -> TileType.INV_CORNER_NW;
            case 8 -> TileType.EDGE_E;        // ... a Oeste
            case 9 -> TileType.CORNER_SE;
            case 10 -> TileType.FULL;         // Conexão horizontal (usamos FULL por simplicidade)
            case 11 -> TileType.INV_CORNER_NE;
            case 12 -> TileType.CORNER_NE;
            case 13 -> TileType.INV_CORNER_SE;
            case 14 -> TileType.INV_CORNER_SW;
            case 15 -> TileType.FULL;         // Rodeado por terra
            default -> TileType.FULL;
        };
    }


    // --- Funções de Ruído (adaptadas do seu GroundChunkModel) ---
    private static float valueNoise(float xf, float yf, long seed) {
        int x0 = fastFloor(xf); int y0 = fastFloor(yf);
        int x1 = x0 + 1; int y1 = y0 + 1;
        float tx = xf - x0; float ty = yf - y0;
        float sx = smoothstep(tx); float sy = smoothstep(ty);
        float v00 = hash01(x0, y0, seed); float v10 = hash01(x1, y0, seed);
        float v01 = hash01(x0, y1, seed); float v11 = hash01(x1, y1, seed);
        float ix0 = lerp(v00, v10, sx); float ix1 = lerp(v01, v11, sx);
        return lerp(ix0, ix1, sy);
    }
    private static int fastFloor(float v) { int i = (int)v; return v < i ? i - 1 : i; }
    private static float smoothstep(float t){ return t*t*(3f - 2f*t); }
    private static float lerp(float a, float b, float t){ return a + (b - a) * t; }
    private static float hash01(int x, int y, long seed) {
        long h = seed; h ^= (x * 0x9E3779B97F4A7C15L); h ^= (y * 0xC2B2AE3D27D4EB4FL);
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL; h ^= (h >>> 31);
        return ((h >>> 40) & 0xFFFFFF) / (float)0xFFFFFF;
    }
}