package org.gene.world.world;

import org.gene.world.chunks.enums.Biome;
import org.gene.world.chunks.enums.Direction;
import org.gene.world.chunks.enums.TileType;

import java.util.*;

/**
 * Gera o mapa de forma iterativa usando as regras de adjacência do TileType.
 * Baseado no algoritmo Wave Function Collapse (WFC).
 */
public class MapGenerator {
    public record GeneratedTile(TileType type, Biome primary, Biome secondary) {}

    private final long seed;
    private final Random random;
    private final Biome primaryBiome = Biome.GROUND;
    private final Biome secondaryBiome = Biome.WATER;

    public MapGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    public GeneratedTile[][] generateMap(int width, int height) {
        // --- INICIALIZAÇÃO ---
        // 1. Grid de possibilidades: cada célula começa com TODOS os TileTypes possíveis.
        List<TileType>[][] possibilities = new ArrayList[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                possibilities[x][y] = new ArrayList<>(EnumSet.allOf(TileType.class));
            }
        }

        // 2. Grid final, inicialmente vazio.
        TileType[][] collapsedGrid = new TileType[width][height];
        int collapsedCount = 0;

        // --- PONTO DE PARTIDA ---
        // Força o centro a ser um tile FULL para iniciar o processo.
        int startX = width / 2;
        int startY = height / 2;
        collapsedGrid[startX][startY] = TileType.FULL;
        possibilities[startX][startY].clear();
        propagate(startX, startY, collapsedGrid, possibilities);
        collapsedCount++;

        // --- LOOP PRINCIPAL DE GERAÇÃO ---
        while (collapsedCount < width * height) {
            // 3. Encontrar a célula com menor "entropia" (menos possibilidades) para colapsar.
            int minEntropy = Integer.MAX_VALUE;
            List<int[]> minEntropyCells = new ArrayList<>();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (collapsedGrid[x][y] == null) { // Se ainda não foi escolhido
                        int entropy = possibilities[x][y].size();
                        if (entropy > 0 && entropy < minEntropy) {
                            minEntropy = entropy;
                            minEntropyCells.clear();
                            minEntropyCells.add(new int[]{x, y});
                        } else if (entropy == minEntropy) {
                            minEntropyCells.add(new int[]{x, y});
                        }
                    }
                }
            }

            if (minEntropyCells.isEmpty()) break; // Terminou ou deu erro

            // 4. Colapsar a célula: escolher uma das possibilidades para ela.
            int[] cellToCollapse = minEntropyCells.get(random.nextInt(minEntropyCells.size()));
            int x = cellToCollapse[0];
            int y = cellToCollapse[1];

            List<TileType> possibleTypes = possibilities[x][y];

            // Lógica de escolha com pesos
            double totalWeight = 0;
            for (TileType type : possibleTypes) {
                totalWeight += type.weight;
            }

            double randomChoice = random.nextDouble() * totalWeight;
            TileType chosenType = null;
            for (TileType type : possibleTypes) {
                randomChoice -= type.weight;
                if (randomChoice <= 0) {
                    chosenType = type;
                    break;
                }
            }
            // Se por algum motivo não escolher (erros de ponto flutuante), pega o último
            if (chosenType == null) {
                chosenType = possibleTypes.get(possibleTypes.size() - 1);
            }
            // --- FIM DA MUDANÇA ---


            collapsedGrid[x][y] = chosenType;
            possibilities[x][y].clear();
            collapsedCount++;

            // 5. Propagar as novas restrições para os vizinhos.
            propagate(x, y, collapsedGrid, possibilities);
        }

// --- MONTAGEM FINAL ---
        GeneratedTile[][] finalMap = new GeneratedTile[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TileType type = collapsedGrid[x][y];
                if (type == null) continue; // Pode acontecer se o WFC falhar

                // Agora, a lógica é simples e direta:
                // Se o gerador escolheu um FULL_SECONDARY, nós o criamos com os biomas invertidos.
                if (type == TileType.FULL_SECONDARY) {
                    finalMap[x][y] = new GeneratedTile(TileType.FULL, secondaryBiome, primaryBiome);
                } else {
                    finalMap[x][y] = new GeneratedTile(type, primaryBiome, secondaryBiome);
                }
            }
        }
        return finalMap;
    }

    /**
     * Atualiza as possibilidades dos vizinhos com base em um tile recém-colocado.
     */
    private void propagate(int x, int y, TileType[][] collapsedGrid, List<TileType>[][] possibilities) {
        TileType sourceTile = collapsedGrid[x][y];
        if (sourceTile == null) return;

        for (Direction dir : Direction.values()) {
            int nx = x + dir.getDx();
            int ny = y + dir.getDy();

            if (nx >= 0 && nx < collapsedGrid.length && ny >= 0 && ny < collapsedGrid[0].length && collapsedGrid[nx][ny] == null) {
                // Pega o conjunto de vizinhos válidos do tile que acabamos de colocar.
                Set<TileType> validNeighbors = sourceTile.getValidNeighbors(dir);
                // Remove das possibilidades do vizinho qualquer tile que NÃO seja válido.
                possibilities[nx][ny].retainAll(validNeighbors);
            }
        }
    }
}