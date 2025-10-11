package org.gene.world.chunks.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.gene.world.chunks.enums.TileType.*;
import static org.gene.world.chunks.enums.Direction.*;

/**
 * Testes unitários para as regras de adjacência do enum TileType.
 * Garante que a lógica de vizinhança permaneça consistente após futuras alterações.
 */
@DisplayName("Testes de Regras de Adjacência para TileType")
class TileTypeTest {

    @Test
    @DisplayName("FULL: Todas as 8 direções de vizinhos devem estar corretas")
    void testFullAdjacencyRules() {
        // Definição de todos os resultados esperados
        Set<TileType> expectedNorth = Set.of(FULL, EDGE_S, CORNER_NW, CORNER_NE);
        Set<TileType> expectedEast = Set.of(FULL, EDGE_W, CORNER_NE, CORNER_SE);
        Set<TileType> expectedSouth = Set.of(FULL, EDGE_N, CORNER_SE, CORNER_SW);
        Set<TileType> expectedWest = Set.of(FULL, EDGE_E, CORNER_NW, CORNER_SW);

        Set<TileType> expectedNorthEast = Set.of(FULL, EDGE_S, EDGE_W, INV_CORNER_SW, CORNER_NW, CORNER_NE, CORNER_SE);
        Set<TileType> expectedSouthEast = Set.of(FULL, EDGE_N, EDGE_W, INV_CORNER_NW, CORNER_NE, CORNER_SE, CORNER_SW);
        Set<TileType> expectedSouthWest = Set.of(FULL, EDGE_N, EDGE_E, INV_CORNER_NE, CORNER_NW, CORNER_SE, CORNER_SW);
        Set<TileType> expectedNorthWest = Set.of(FULL, EDGE_S, EDGE_E, INV_CORNER_SE, CORNER_NW, CORNER_NE, CORNER_SW);

        // assertAll executa todas as verificações e reporta todas as falhas juntas
        assertAll("Verificando todas as 8 direções para TileType.FULL",
                () -> assertEquals(expectedNorth, FULL.getValidNeighbors(N), "Vizinhos ao Norte (N) estão incorretos."),
                () -> assertEquals(expectedEast, FULL.getValidNeighbors(E), "Vizinhos a Leste (E) estão incorretos."),
                () -> assertEquals(expectedSouth, FULL.getValidNeighbors(S), "Vizinhos ao Sul (S) estão incorretos."),
                () -> assertEquals(expectedWest, FULL.getValidNeighbors(W), "Vizinhos a Oeste (W) estão incorretos."),
                () -> assertEquals(expectedNorthEast, FULL.getValidNeighbors(NE), "Vizinhos a Nordeste (NE) estão incorretos."),
                () -> assertEquals(expectedSouthEast, FULL.getValidNeighbors(SE), "Vizinhos a Sudeste (SE) estão incorretos."),
                () -> assertEquals(expectedSouthWest, FULL.getValidNeighbors(SW), "Vizinhos a Sudoeste (SW) estão incorretos."),
                () -> assertEquals(expectedNorthWest, FULL.getValidNeighbors(NW), "Vizinhos a Noroeste (NW) estão incorretos.")
        );
    }

    @Test
    @DisplayName("EDGE_N: Todas as 8 direções de vizinhos devem estar corretas")
    void testEdgeNAdjacencyRules() {
        // Resultados esperados para EDGE_N
        Set<TileType> expectedNorth = Set.of(FULL, EDGE_S, CORNER_NW, CORNER_NE);
        Set<TileType> expectedEast = Set.of(EDGE_N, CORNER_SW, INV_CORNER_NW);
        Set<TileType> expectedSouth = Set.of(EDGE_S,INV_CORNER_SE, INV_CORNER_SW);
        Set<TileType> expectedWest = Set.of(EDGE_N, CORNER_SE, INV_CORNER_NE);

        Set<TileType> expectedNorthEast = Set.of(FULL, EDGE_S, EDGE_W, INV_CORNER_SW, CORNER_NW, CORNER_NE, CORNER_SE);
        Set<TileType> expectedSouthEast = Set.of(EDGE_S, EDGE_E, INV_CORNER_NE, INV_CORNER_SE, INV_CORNER_SW, CORNER_NW );
        Set<TileType> expectedSouthWest = Set.of(EDGE_W, EDGE_S, INV_CORNER_NW, INV_CORNER_SE, INV_CORNER_SW, CORNER_NE);
        Set<TileType> expectedNorthWest = Set.of(FULL, EDGE_S, EDGE_E, CORNER_NW, CORNER_NE, CORNER_SW,INV_CORNER_SE);

        // Verificações
        assertAll("Verificando todas as 8 direções para TileType.EDGE_N",
                () -> assertEquals(expectedNorth, EDGE_N.getValidNeighbors(N), "Vizinhos ao Norte (N) de EDGE_N estão incorretos."),
                () -> assertEquals(expectedEast, EDGE_N.getValidNeighbors(E), "Vizinhos a Leste (E) de EDGE_N estão incorretos."),
                () -> assertEquals(expectedSouth, EDGE_N.getValidNeighbors(S), "Vizinhos ao Sul (S) de EDGE_N estão incorretos."),
                () -> assertEquals(expectedWest, EDGE_N.getValidNeighbors(W), "Vizinhos a Oeste (W) de EDGE_N estão incorretos."),
                () -> assertEquals(expectedNorthEast, EDGE_N.getValidNeighbors(NE), "Vizinhos a Nordeste (NE) de EDGE_N estão incorretos."),
                () -> assertEquals(expectedSouthEast, EDGE_N.getValidNeighbors(SE), "Vizinhos a Sudeste (SE) de EDGE_N estão incorretos."),
                () -> assertEquals(expectedSouthWest, EDGE_N.getValidNeighbors(SW), "Vizinhos a Sudoeste (SW) de EDGE_N estão incorretos."),
                () -> assertEquals(expectedNorthWest, EDGE_N.getValidNeighbors(NW), "Vizinhos a Noroeste (NW) de EDGE_N estão incorretos.")
        );
    }

    @Test
    @DisplayName("EDGE_S: Todas as 8 direções de vizinhos devem estar corretas")
    void testEdgeSAdjacencyRules() {
        // Resultados esperados para EDGE_S com base na sua lógica de quadrantes
        Set<TileType> expectedNorth = Set.of(EDGE_N, INV_CORNER_NW, INV_CORNER_NE);
        Set<TileType> expectedEast = Set.of(EDGE_S, INV_CORNER_SW, CORNER_NW);
        Set<TileType> expectedSouth = Set.of(FULL, EDGE_N, CORNER_SW, CORNER_SE);
        Set<TileType> expectedWest = Set.of(EDGE_S,  INV_CORNER_SE, CORNER_NE);

        Set<TileType> expectedNorthEast = Set.of(EDGE_N, EDGE_E, INV_CORNER_NW, INV_CORNER_NE, INV_CORNER_SE, CORNER_SW);
        Set<TileType> expectedSouthEast = Set.of(FULL, EDGE_N, EDGE_W, INV_CORNER_NW, CORNER_NE, CORNER_SE, CORNER_SW);
        Set<TileType> expectedSouthWest = Set.of(FULL, EDGE_N, EDGE_E, INV_CORNER_NE,CORNER_NW, CORNER_SE, CORNER_SW );
        Set<TileType> expectedNorthWest = Set.of(EDGE_N, EDGE_W, INV_CORNER_NW, INV_CORNER_NE, INV_CORNER_SW, CORNER_SE);

        // Verificações
        assertAll("Verificando todas as 8 direções para TileType.EDGE_S",
                () -> assertEquals(expectedNorth, EDGE_S.getValidNeighbors(N), "Vizinhos ao Norte (N) de EDGE_S estão incorretos."),
                () -> assertEquals(expectedEast, EDGE_S.getValidNeighbors(E), "Vizinhos a Leste (E) de EDGE_S estão incorretos."),
                () -> assertEquals(expectedSouth, EDGE_S.getValidNeighbors(S), "Vizinhos ao Sul (S) de EDGE_S estão incorretos."),
                () -> assertEquals(expectedWest, EDGE_S.getValidNeighbors(W), "Vizinhos a Oeste (W) de EDGE_S estão incorretos."),
                () -> assertEquals(expectedNorthEast, EDGE_S.getValidNeighbors(NE), "Vizinhos a Nordeste (NE) de EDGE_S estão incorretos."),
                () -> assertEquals(expectedSouthEast, EDGE_S.getValidNeighbors(SE), "Vizinhos a Sudeste (SE) de EDGE_S estão incorretos."),
                () -> assertEquals(expectedSouthWest, EDGE_S.getValidNeighbors(SW), "Vizinhos a Sudoeste (SW) de EDGE_S estão incorretos."),
                () -> assertEquals(expectedNorthWest, EDGE_S.getValidNeighbors(NW), "Vizinhos a Noroeste (NW) de EDGE_S estão incorretos.")
        );
    }

    @Test
    @DisplayName("EDGE_E: Todas as 8 direções de vizinhos devem estar corretas")
    void testEdgeEAdjacencyRules() {
        // Resultados esperados para EDGE_E com base na sua lógica de quadrantes
        Set<TileType> expectedNorth = Set.of(EDGE_E, INV_CORNER_SE, CORNER_SW);
        Set<TileType> expectedEast = Set.of(FULL, EDGE_W, CORNER_NE, CORNER_SE);
        Set<TileType> expectedSouth = Set.of(EDGE_E, INV_CORNER_NE, CORNER_NW);
        Set<TileType> expectedWest = Set.of(EDGE_W, INV_CORNER_NW, INV_CORNER_SW);

        Set<TileType> expectedNorthEast = Set.of(FULL, EDGE_S, EDGE_W, INV_CORNER_SW, CORNER_NW, CORNER_NE, CORNER_SE);
        Set<TileType> expectedSouthEast = Set.of(FULL, EDGE_N, EDGE_W, INV_CORNER_NW, CORNER_NE, CORNER_SE, CORNER_SW);
        Set<TileType> expectedSouthWest = Set.of(EDGE_S, EDGE_W, INV_CORNER_NW, INV_CORNER_SE, INV_CORNER_SW, CORNER_NE);
        Set<TileType> expectedNorthWest = Set.of(EDGE_N, EDGE_W, INV_CORNER_NW, INV_CORNER_NE, INV_CORNER_SW, CORNER_SE);

        // Verificações
        assertAll("Verificando todas as 8 direções para TileType.EDGE_E",
                () -> assertEquals(expectedNorth, EDGE_E.getValidNeighbors(N), "Vizinhos ao Norte (N) de EDGE_E estão incorretos."),
                () -> assertEquals(expectedEast, EDGE_E.getValidNeighbors(E), "Vizinhos a Leste (E) de EDGE_E estão incorretos."),
                () -> assertEquals(expectedSouth, EDGE_E.getValidNeighbors(S), "Vizinhos ao Sul (S) de EDGE_E estão incorretos."),
                () -> assertEquals(expectedWest, EDGE_E.getValidNeighbors(W), "Vizinhos a Oeste (W) de EDGE_E estão incorretos."),
                () -> assertEquals(expectedNorthEast, EDGE_E.getValidNeighbors(NE), "Vizinhos a Nordeste (NE) de EDGE_E estão incorretos."),
                () -> assertEquals(expectedSouthEast, EDGE_E.getValidNeighbors(SE), "Vizinhos a Sudeste (SE) de EDGE_E estão incorretos."),
                () -> assertEquals(expectedSouthWest, EDGE_E.getValidNeighbors(SW), "Vizinhos a Sudoeste (SW) de EDGE_E estão incorretos."),
                () -> assertEquals(expectedNorthWest, EDGE_E.getValidNeighbors(NW), "Vizinhos a Noroeste (NW) de EDGE_E estão incorretos.")
        );
    }

}