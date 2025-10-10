package org.gene.world.chunks.enums;

import java.util.Set;
import java.util.Map;

public enum TileType {
    // Forma 100% preenchida
    FULL,

    // Formas de borda reta
    EDGE_N, // Bioma primário ao Norte, secundário ao Sul
    EDGE_E, // Bioma primário a Leste, secundário a Oeste
    EDGE_S, // Bioma primário ao Sul, secundário ao Norte
    EDGE_W, // Bioma primário a Oeste, secundário a Leste

    // Formas de canto (Primário ocupa 1/4 do tile)
    CORNER_NW,
    CORNER_NE,
    CORNER_SE,
    CORNER_SW,

    // Formas de canto invertido (Primário ocupa 3/4 do tile)
    INV_CORNER_NW,
    INV_CORNER_NE,
    INV_CORNER_SE,
    INV_CORNER_SW;

    // --- Espaço para as regras de vizinhança que você mencionou ---
    // Esta estrutura está aqui para quando você quiser um gerador mais complexo.
    // Por enquanto, não precisamos preenchê-la.
    private Map<Direction, Set<TileType>> adjacencyRules;

    /**
     * Monta o nome do arquivo de forma dinâmica baseado nos biomas.
     * @param primary O bioma principal (ex: GROUND)
     * @param secondary O bioma secundário (ex: WATER)
     * @return O caminho completo para o arquivo de asset. ex: "assets/top-ground-bottom-water.png"
     */
    public String getFilename(Biome primary, Biome secondary) {
        String p = primary.toString();
        String s = secondary.toString();

        String fileName = switch (this) {
            case FULL -> String.format("%s.png", p);

            // Nomes baseados na imagem que você forneceu
            case EDGE_N -> String.format("top-%s-bottom-%s.png", p, s);
            case EDGE_S -> String.format("top-%s-bottom-%s.png", s, p);
            case EDGE_E -> String.format("left-%s-right-%s.png", s, p);
            case EDGE_W -> String.format("left-%s-right-%s.png", p, s);

            case CORNER_NW -> String.format("threeparts-%s-topleft-%s.png", p, s);
            case CORNER_NE-> String.format("threeparts-%s-topright-%s.png", p, s);
            case CORNER_SE -> String.format("threeparts-%s-bottomright-%s.png", p, s);
            case CORNER_SW -> String.format("threeparts-%s-bottomleft-%s.png", p, s);

            case INV_CORNER_NW -> String.format("threeparts-%s-topleft-%s.png", s, p);
            case INV_CORNER_NE-> String.format("threeparts-%s-topright-%s.png", s, p);
            case INV_CORNER_SE -> String.format("threeparts-%s-bottomright-%s.png", s, p);
            case INV_CORNER_SW -> String.format("threeparts-%s-bottomleft-%s.png", s, p);


            // Caso o bioma secundário seja igual ao primário, ele é sempre FULL
            default -> String.format("%s.png", p);
        };
        return "assets/" + fileName;
    }
}