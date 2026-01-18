package com.gogame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy dla klasy BoardHelper.
 */
class BoardHelperTest {

    private Board board;

    /**
     * Przygotowanie planszy przed każdym testem.
     */
    @BeforeEach
    void setUp() {
        board = new Board();
    }

    /**
     * Test wykonania ruchu czarnym.
     */
    @Test
    void testMakeMoveBlack() {
        BoardHelper.makeMove(board, 5, 5, true);

        assertEquals(2, board.positions[5][5].color, "Powinien być czarny kamień");
        assertTrue(board.positions[5][5].breaths > 0, "Kamień powinien mieć oddechy");
    }

    /**
     * Test wykonania ruchu białym.
     */
    @Test
    void testMakeMoveWhite() {
        BoardHelper.makeMove(board, 10, 10, false);

        assertEquals(1, board.positions[10][10].color, "Powinien być biały kamień");
        assertTrue(board.positions[10][10].breaths > 0, "Kamień powinien mieć oddechy");
    }

    /**
     * Test kopiowania stanu planszy.
     */
    @Test
    void testCopyBoardState() {
        Board source = new Board();
        source.positions[5][5].color = 2;
        source.positions[10][10].color = 1;
        source.positions[5][5].breaths = 3;
        source.lastPositions[3][3] = 1;

        Board dest = new Board();
        BoardHelper.copyBoardState(dest, source);

        assertEquals(2, dest.positions[5][5].color, "Kolor powinien być skopiowany");
        assertEquals(1, dest.positions[10][10].color, "Kolor powinien być skopiowany");
        assertEquals(3, dest.positions[5][5].breaths, "Oddechy powinny być skopiowane");
        assertEquals(1, dest.lastPositions[3][3], "lastPositions powinny być skopiowane");
    }

    /**
     * Test pobierania koloru.
     */
    @Test
    void testGetColor() {
        board.positions[5][5].color = 2;
        board.positions[10][10].color = 1;

        assertEquals(2, BoardHelper.getColor(board, 5, 5), "Powinien zwrócić czarny");
        assertEquals(1, BoardHelper.getColor(board, 10, 10), "Powinien zwrócić biały");
        assertEquals(0, BoardHelper.getColor(board, 0, 0), "Powinien zwrócić puste");
    }

    /**
     * Test pobierania koloru poza planszą.
     */
    @Test
    void testGetColorOutOfBounds() {
        assertEquals(-1, BoardHelper.getColor(board, -1, 5), "Powinien zwrócić -1 dla ujemnego wiersza");
        assertEquals(-1, BoardHelper.getColor(board, 5, -1), "Powinien zwrócić -1 dla ujemnej kolumny");
        assertEquals(-1, BoardHelper.getColor(board, 19, 5), "Powinien zwrócić -1 dla wiersza >= 19");
        assertEquals(-1, BoardHelper.getColor(board, 5, 19), "Powinien zwrócić -1 dla kolumny >= 19");
    }

    /**
     * Test pobierania właściciela terytorium.
     */
    @Test
    void testGetTerritoryOwner() {
        board.territoryCache[5][5] = 1;
        board.territoryCache[10][10] = 2;
        board.territoryCache[15][15] = 3;

        assertEquals(1, BoardHelper.getTerritoryOwner(board, 5, 5), "Powinien zwrócić białego");
        assertEquals(2, BoardHelper.getTerritoryOwner(board, 10, 10), "Powinien zwrócić czarnego");
        assertEquals(3, BoardHelper.getTerritoryOwner(board, 15, 15), "Powinien zwrócić neutralne");
        assertEquals(0, BoardHelper.getTerritoryOwner(board, 0, 0), "Powinien zwrócić nieobliczone");
    }

    /**
     * Test pobierania łańcucha dla samotnego kamienia.
     */
    @Test
    void testGetChainSingleStone() {
        board.positions[5][5].color = 1;

        List<int[]> chain = BoardHelper.getChain(board, 5, 5);

        assertEquals(1, chain.size(), "Łańcuch powinien mieć 1 kamień");
        assertEquals(5, chain.get(0)[0], "Wiersz powinien być 5");
        assertEquals(5, chain.get(0)[1], "Kolumna powinna być 5");
    }

    /**
     * Test pobierania łańcucha dla pustego pola.
     */
    @Test
    void testGetChainEmpty() {
        List<int[]> chain = BoardHelper.getChain(board, 5, 5);

        assertEquals(0, chain.size(), "Puste pole powinno dać pusty łańcuch");
    }

    /**
     * Test pobierania poziomego łańcucha.
     */
    @Test
    void testGetChainHorizontal() {
        board.positions[5][5].color = 2;
        board.positions[5][6].color = 2;
        board.positions[5][7].color = 2;

        List<int[]> chain = BoardHelper.getChain(board, 5, 6);

        assertEquals(3, chain.size(), "Łańcuch powinien mieć 3 kamienie");
    }

    /**
     * Test pobierania pionowego łańcucha.
     */
    @Test
    void testGetChainVertical() {
        board.positions[5][5].color = 1;
        board.positions[6][5].color = 1;
        board.positions[7][5].color = 1;
        board.positions[8][5].color = 1;

        List<int[]> chain = BoardHelper.getChain(board, 6, 5);

        assertEquals(4, chain.size(), "Łańcuch powinien mieć 4 kamienie");
    }

    /**
     * Test pobierania łańcucha w kształcie L.
     */
    @Test
    void testGetChainLShape() {
        board.positions[5][5].color = 2;
        board.positions[5][6].color = 2;
        board.positions[6][6].color = 2;
        board.positions[7][6].color = 2;

        List<int[]> chain = BoardHelper.getChain(board, 5, 5);

        assertEquals(4, chain.size(), "Łańcuch w kształcie L powinien mieć 4 kamienie");
    }

    /**
     * Test nie łączy różnych kolorów.
     */
    @Test
    void testGetChainDifferentColors() {
        board.positions[5][5].color = 1;
        board.positions[5][6].color = 2;
        board.positions[5][7].color = 1;

        List<int[]> chain = BoardHelper.getChain(board, 5, 5);

        assertEquals(1, chain.size(), "Różne kolory nie powinny być w jednym łańcuchu");
    }

    /**
     * Test dużego łańcucha.
     */
    @Test
    void testGetChainLarge() {
        // Kwadrat 3x3
        for (int i = 5; i < 8; i++) {
            for (int j = 5; j < 8; j++) {
                board.positions[i][j].color = 1;
            }
        }

        List<int[]> chain = BoardHelper.getChain(board, 6, 6);

        assertEquals(9, chain.size(), "Kwadrat 3x3 powinien mieć 9 kamieni");
    }

    /**
     * Test łańcucha w narożniku.
     */
    @Test
    void testGetChainCorner() {
        board.positions[0][0].color = 2;
        board.positions[0][1].color = 2;
        board.positions[1][0].color = 2;

        List<int[]> chain = BoardHelper.getChain(board, 0, 0);

        assertEquals(3, chain.size(), "Łańcuch w narożniku powinien mieć 3 kamienie");
    }

    /**
     * Test kopiowania pustej planszy.
     */
    @Test
    void testCopyEmptyBoard() {
        Board source = new Board();
        Board dest = new Board();

        BoardHelper.copyBoardState(dest, source);

        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                assertEquals(0, dest.positions[i][j].color, "Wszystkie pola powinny być puste");
            }
        }
    }

    /**
     * Test kopiowania pełnej planszy.
     */
    @Test
    void testCopyFullBoard() {
        Board source = new Board();
        // Wypełnij planszę na przemian
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                source.positions[i][j].color = ((i + j) % 2) + 1;
            }
        }

        Board dest = new Board();
        BoardHelper.copyBoardState(dest, source);

        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                assertEquals(source.positions[i][j].color, dest.positions[i][j].color,
                        "Kolory powinny być identyczne");
            }
        }
    }
}