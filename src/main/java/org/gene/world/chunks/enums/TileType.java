package org.gene.world.chunks.enums;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum TileType {
    // --- MUDANÇA 1: As definições agora representam os 4 quadrantes (NW, NE, SW, SE) ---
    // P = PRIMARY, S = SECONDARY.
    FULL(         QuadrantType.PRIMARY,   QuadrantType.PRIMARY,
            QuadrantType.PRIMARY,   QuadrantType.PRIMARY,10.0),

    FULL_SECONDARY( QuadrantType.SECONDARY, QuadrantType.SECONDARY,
            QuadrantType.SECONDARY, QuadrantType.SECONDARY,   10.0),

    EDGE_N(       QuadrantType.PRIMARY,   QuadrantType.PRIMARY,
            QuadrantType.SECONDARY, QuadrantType.SECONDARY,   1.5),
    EDGE_S(       QuadrantType.SECONDARY, QuadrantType.SECONDARY,
            QuadrantType.PRIMARY,   QuadrantType.PRIMARY,   1.5),
    EDGE_E(       QuadrantType.SECONDARY, QuadrantType.PRIMARY,
            QuadrantType.SECONDARY, QuadrantType.PRIMARY,   1.5),
    EDGE_W(       QuadrantType.PRIMARY,   QuadrantType.SECONDARY,
            QuadrantType.PRIMARY,   QuadrantType.SECONDARY,   1.5),

    INV_CORNER_NW(    QuadrantType.PRIMARY,   QuadrantType.SECONDARY,
            QuadrantType.SECONDARY, QuadrantType.SECONDARY,   1.0),
    INV_CORNER_NE(    QuadrantType.SECONDARY, QuadrantType.PRIMARY,
            QuadrantType.SECONDARY, QuadrantType.SECONDARY,   1.0),
    INV_CORNER_SE(    QuadrantType.SECONDARY, QuadrantType.SECONDARY,
            QuadrantType.SECONDARY, QuadrantType.PRIMARY,   1.0),
    INV_CORNER_SW(    QuadrantType.SECONDARY, QuadrantType.SECONDARY,
            QuadrantType.PRIMARY,   QuadrantType.SECONDARY,   1.0),

    CORNER_NW(QuadrantType.SECONDARY, QuadrantType.PRIMARY,
            QuadrantType.PRIMARY,   QuadrantType.PRIMARY,   1.0),
    CORNER_NE(QuadrantType.PRIMARY,   QuadrantType.SECONDARY,
            QuadrantType.PRIMARY,   QuadrantType.PRIMARY,   1.0),
    CORNER_SE(QuadrantType.PRIMARY,   QuadrantType.PRIMARY,
            QuadrantType.PRIMARY,   QuadrantType.SECONDARY,   1.0),
    CORNER_SW(QuadrantType.PRIMARY,   QuadrantType.PRIMARY,
            QuadrantType.SECONDARY, QuadrantType.PRIMARY,   1.0);

    private enum QuadrantType { PRIMARY, SECONDARY }
    final QuadrantType nwQuadrant, neQuadrant, swQuadrant, seQuadrant;
    public final double weight;
    private Map<Direction, Set<TileType>> adjacencyRules;

    TileType(QuadrantType nw, QuadrantType ne, QuadrantType sw, QuadrantType se, double weight) {
        this.nwQuadrant = nw;
        this.neQuadrant = ne;
        this.swQuadrant = sw;
        this.seQuadrant = se;
        this.weight = weight;
    }

    /**
     * Bloco estático que gera as regras. Nenhuma alteração aqui,
     * pois ele apenas chama o método canBeNeighbor, que será modificado.
     */
    static {
        for (TileType currentTile : TileType.values()) {
            currentTile.adjacencyRules = new EnumMap<>(Direction.class);
            // --- MUDANÇA 1: O loop agora itera sobre TODAS as direções ---
            for (Direction direction : Direction.values()) {
                EnumSet<TileType> validNeighbors = EnumSet.noneOf(TileType.class);
                for (TileType candidateTile : TileType.values()) {
                    if (currentTile.canBeNeighbor(candidateTile, direction)) {
                        validNeighbors.add(candidateTile);
                    }
                }
                currentTile.adjacencyRules.put(direction, validNeighbors);
            }
        }
    }

    /**
     * Lógica de verificação de vizinhos com as regras para as diagonais.
     */
    private boolean canBeNeighbor(TileType candidate, Direction direction) {
        return switch (direction) {
            // Regras Cardinais
            case N -> this.nwQuadrant == candidate.swQuadrant && this.neQuadrant == candidate.seQuadrant;
            case E -> this.neQuadrant == candidate.nwQuadrant && this.seQuadrant == candidate.swQuadrant;
            case S -> this.swQuadrant == candidate.nwQuadrant && this.seQuadrant == candidate.neQuadrant;
            case W -> this.nwQuadrant == candidate.neQuadrant && this.swQuadrant == candidate.seQuadrant;

            // --- MUDANÇA 2: Adicionadas as regras para as Diagonais ---
            // A verificação é simples: um único quadrante precisa casar.
            case NE -> this.neQuadrant == candidate.swQuadrant; // Meu NE casa com o SW do vizinho
            case SE -> this.seQuadrant == candidate.nwQuadrant; // Meu SE casa com o NW do vizinho
            case SW -> this.swQuadrant == candidate.neQuadrant; // Meu SW casa com o NE do vizinho
            case NW -> this.nwQuadrant == candidate.seQuadrant; // Meu NW casa com o SE do vizinho
        };
    }

    public Set<TileType> getValidNeighbors(Direction direction) {
        return adjacencyRules.get(direction);
    }

    public String getFilename(Biome primary, Biome secondary) {
        String p = primary.toString();
        String s = secondary.toString();
        String fileName = switch (this) {
            case FULL -> String.format("%s.png", p);
            case FULL_SECONDARY -> String.format("%s.png", s);
            case EDGE_N -> String.format("top-%s-bottom-%s.png", p, s);
            case EDGE_S -> String.format("top-%s-bottom-%s.png", s, p);
            case EDGE_E -> String.format("left-%s-right-%s.png", s, p);
            case EDGE_W -> String.format("left-%s-right-%s.png", p, s);
            case CORNER_NW -> String.format("threeparts-%s-topleft-%s.png", p, s);
            case CORNER_NE -> String.format("threeparts-%s-topright-%s.png", p, s);
            case CORNER_SE -> String.format("threeparts-%s-bottomright-%s.png", p, s);
            case CORNER_SW -> String.format("threeparts-%s-bottomleft-%s.png", p, s);
            case INV_CORNER_NW -> String.format("threeparts-%s-topleft-%s.png", s, p);
            case INV_CORNER_NE -> String.format("threeparts-%s-topright-%s.png", s, p);
            case INV_CORNER_SE -> String.format("threeparts-%s-bottomright-%s.png", s, p);
            case INV_CORNER_SW -> String.format("threeparts-%s-bottomleft-%s.png", s, p);
            default -> String.format("%s.png", p);
        };
        return "assets/" + fileName;
    }

}