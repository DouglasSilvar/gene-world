package org.gene.world.chunks.enums;

/**
 * Representa as 8 direções em uma grade 2D.
 * Inclui métodos utilitários para manipulação de coordenadas e lógica de vizinhança.
 */
public enum Direction {
    // Direções Cardinais
    N(0, 1),
    E(1, 0),
    S(0, -1),
    W(-1, 0),

    // Direções Diagonais
    NE(1, 1),
    SE(1, -1),
    SW(-1, -1),
    NW(-1, 1);

    /**
     * O deslocamento no eixo X que esta direção representa.
     */
    private final int dx;

    /**
     * O deslocamento no eixo Y que esta direção representa.
     * (Assumindo que Y cresce para cima)
     */
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Retorna o deslocamento no eixo X.
     * @return -1 para Oeste, 1 para Leste, 0 para os demais.
     */
    public int getDx() {
        return dx;
    }

    /**
     * Retorna o deslocamento no eixo Y.
     * @return 1 para Norte, -1 para Sul, 0 para os demais.
     */
    public int getDy() {
        return dy;
    }

    /**
     * Verifica se a direção é Cardinal (Norte, Sul, Leste, Oeste).
     * @return true se for uma direção cardinal, false caso contrário.
     */
    public boolean isCardinal() {
        return switch (this) {
            case N, E, S, W -> true;
            default -> false;
        };
    }

    /**
     * Verifica se a direção é Diagonal (NE, SE, SW, NW).
     * @return true se for uma direção diagonal, false caso contrário.
     */
    public boolean isDiagonal() {
        // Simplesmente o oposto de ser cardinal.
        return !isCardinal();
    }

    /**
     * Retorna a direção oposta a esta.
     * Exemplo: N.getOpposite() retorna S.
     * @return A direção oposta.
     */
    public Direction getOpposite() {
        return switch (this) {
            case N -> S;
            case E -> W;
            case S -> N;
            case W -> E;
            case NE -> SW;
            case SE -> NW;
            case SW -> NE;
            case NW -> SE;
        };
    }

    /**
     * Retorna uma direção a partir de um vetor de deslocamento (dx, dy).
     * Os valores de dx e dy são normalizados para -1, 0, ou 1.
     * Retorna null se o vetor for (0,0).
     * @param dx Deslocamento em X.
     * @param dy Deslocamento em Y.
     * @return A Direção correspondente, ou null.
     */
    public static Direction fromVector(int dx, int dy) {
        // Normaliza o vetor para garantir que os componentes sejam -1, 0, ou 1.
        int normDx = Integer.signum(dx);
        int normDy = Integer.signum(dy);

        if (normDx == 0 && normDy == 0) {
            return null; // Não há direção para um vetor nulo.
        }

        for (Direction dir : values()) {
            if (dir.dx == normDx && dir.dy == normDy) {
                return dir;
            }
        }
        return null; // Não deveria acontecer com entradas válidas.
    }
}