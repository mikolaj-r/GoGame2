package com.gogame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy dla klasy Board.
 */
class BoardTest {

    private Board board;

    /**
     * Przygotowanie planszy przed każdym testem.
     */
    @BeforeEach
    void setUp() {
        board = new Board();
        App.pointsBlack = 0;
        App.pointsWhite = 0;
        App.finalPointsBlack = 0;
        App.finalPointsWhite = 0;
    }

    /**
     * Test tworzenia planszy.
     */
    @Test
    void testBoardCreation() {
        assertNotNull(board.positions, "Pozycje powinny być zainicjowane");
        assertEquals(19, board.positions.length, "Plansza powinna mieć 19 wierszy");
        assertEquals(19, board.positions[0].length, "Plansza powinna mieć 19 kolumn");

        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                assertNotNull(board.positions[i][j], "Każda komórka powinna być zainicjowana");
                assertEquals(0, board.positions[i][j].color, "Każda komórka powinna być pusta");
            }
        }
    }

    /**
     * Test aktualizacji oddechów.
     */
    @Test
    void testUpdateBreaths() {
        board.updateBreaths();

        // Narożnik ma 2 oddechy
        assertEquals(2, board.positions[0][0].breaths, "Narożnik powinien mieć 2 oddechy");

        // Krawędź ma 3 oddechy
        assertEquals(3, board.positions[0][9].breaths, "Krawędź powinna mieć 3 oddechy");

        // Środek ma 4 oddechy
        assertEquals(4, board.positions[9][9].breaths, "Środek powinien mieć 4 oddechy");
    }

    /**
     * Test oddechów z kamieniaami.
     */
    @Test
    void testUpdateBreathsWithStones() {
        board.positions[9][9].color = 1; // biały
        board.positions[9][10].color = 2; // czarny
        board.updateBreaths();

        assertEquals(3, board.positions[9][9].breaths, "Biały kamień powinien mieć 3 oddechy");
        assertEquals(3, board.positions[9][10].breaths, "Czarny kamień powinien mieć 3 oddechy");
    }

    /**
     * Test kopiowania stanu planszy.
     */
    @Test
    void testCopyToLast() {
        board.positions[5][5].color = 2;
        board.positions[10][10].color = 1;

        board.copyToLast();

        assertEquals(2, board.lastPositions[5][5], "Stan powinien być skopiowany");
        assertEquals(1, board.lastPositions[10][10], "Stan powinien być skopiowany");
        assertEquals(0, board.lastPositions[0][0], "Puste pola powinny być 0");
    }

    /**
     * Test wykrywania Ko.
     */
    @Test
    void testIsKo() {
        board.positions[5][5].color = 1;
        board.copyToLast();

        assertTrue(board.isKo(), "Identyczne plansze powinny być Ko");

        board.positions[5][5].color = 0;
        assertFalse(board.isKo(), "Różne plansze nie powinny być Ko");
    }

    /**
     * Test sprawdzania życia łańcucha.
     */
    @Test
    void testIsChainAlive() {
        board.positions[9][9].color = 1;
        board.updateBreaths();

        board.visited = new boolean[19][19];
        assertEquals(1, board.isChainAlive(9, 9, 1), "Samotny kamień z oddechami powinien żyć");
    }

    /**
     * Test martego łańcucha.
     */
    @Test
    void testDeadChain() {
        // Otoczony kamień
        board.positions[9][9].color = 1;
        board.positions[8][9].color = 2;
        board.positions[10][9].color = 2;
        board.positions[9][8].color = 2;
        board.positions[9][10].color = 2;
        board.updateBreaths();

        assertEquals(0, board.positions[9][9].breaths, "Otoczony kamień nie powinien mieć oddechów");
    }

    /**
     * Test usuwania łańcucha.
     */
    @Test
    void testRemoveChain() {
        board.positions[5][5].color = 1;
        board.positions[5][6].color = 1;
        int initialPoints = App.pointsBlack;

        board.visited = new boolean[19][19];
        board.removeChain(5, 5);

        assertEquals(0, board.positions[5][5].color, "Kamień powinien być usunięty");
        assertEquals(0, board.positions[5][6].color, "Kamień powinien być usunięty");
        assertEquals(initialPoints + 2, App.pointsBlack, "Punkty powinny wzrosnąć o 2");
    }

    /**
     * Test legalnego ruchu.
     */
    @Test
    void testCheckMoveLegal() {
        boolean result = board.checkMove(9, 9, true);
        assertTrue(result, "Ruch na pustą komórkę powinien być legalny");
        assertEquals(2, board.positions[9][9].color, "Powinien być czarny kamień");
    }

    /**
     * Test nielegalnego ruchu na zajętą pozycję.
     */
    @Test
    void testCheckMoveOccupied() {
        board.positions[9][9].color = 1;
        boolean result = board.checkMove(9, 9, true);
        assertFalse(result, "Ruch na zajętą pozycję powinien być nielegalny");
    }

    /**
     * Test zbicia przeciwnika.
     */
    @Test
    void testCheckMoveCapture() {
        // Otoczony biały kamień
        board.positions[9][9].color = 1;
        board.positions[8][9].color = 2;
        board.positions[10][9].color = 2;
        board.positions[9][8].color = 2;
        board.updateBreaths();

        boolean result = board.checkMove(9, 10, false);
        assertTrue(result, "Ruch zbijający powinien być legalny");
    }

    /**
     * Test obliczania terytoriów dla białego.
     */
    @Test
    void testCalculateTerritoriesWhite() {
        // Małe terytorium białego
        board.positions[0][0].color = 1;
        board.positions[0][1].color = 1;
        board.positions[1][0].color = 1;

        board.calculateTerritories();

        // Sprawdź czy puste pola w narożniku należą do białego
        assertTrue(board.territoryCache[0][2] == 1 || board.territoryCache[0][2] == 3,
                "Terytorium powinno należeć do białego lub być neutralne");
    }

    /**
     * Test obliczania terytoriów dla czarnego.
     */
    @Test
    void testCalculateTerritoriesBlack() {
        // Małe terytorium czarnego
        board.positions[18][18].color = 2;
        board.positions[18][17].color = 2;
        board.positions[17][18].color = 2;

        board.calculateTerritories();

        // Sprawdź czy puste pola w narożniku należą do czarnego
        assertTrue(board.territoryCache[18][16] == 2 || board.territoryCache[18][16] == 3,
                "Terytorium powinno należeć do czarnego lub być neutralne");
    }

    /**
     * Test checkBoard usuwa martwe kamienie.
     */
    @Test
    void testCheckBoard() {
        // Otoczony kamień
        board.positions[9][9].color = 1;
        board.positions[8][9].color = 2;
        board.positions[10][9].color = 2;
        board.positions[9][8].color = 2;
        board.positions[9][10].color = 2;
        board.updateBreaths();

        board.checkBoard();

        assertEquals(0, board.positions[9][9].color, "Martwy kamień powinien być usunięty");
    }

    /**
     * Test długiego łańcucha.
     */
    @Test
    void testLongChain() {
        // Łańcuch poziomy
        for (int i = 5; i < 10; i++) {
            board.positions[9][i].color = 1;
        }
        board.updateBreaths();

        board.visited = new boolean[19][19];
        int alive = board.isChainAlive(9, 5, 1);
        assertEquals(1, alive, "Łańcuch z oddechami powinien żyć");
    }

    /**
     * Test zawijania łańcucha.
     */
    @Test
    void testWrappedChain() {
        // Łańcuch w kształcie L
        board.positions[9][9].color = 1;
        board.positions[9][10].color = 1;
        board.positions[10][9].color = 1;

        board.visited = new boolean[19][19];
        board.removeChain(9, 9);

        assertEquals(0, board.positions[9][9].color, "Cały łańcuch powinien być usunięty");
        assertEquals(0, board.positions[9][10].color, "Cały łańcuch powinien być usunięty");
        assertEquals(0, board.positions[10][9].color, "Cały łańcuch powinien być usunięty");
    }

    /**
     * Test granicznych pozycji.
     */
    @Test
    void testBoundaryPositions() {
        assertTrue(board.checkMove(0, 0, true), "Ruch w narożniku powinien być legalny");
        assertTrue(board.checkMove(18, 18, false), "Ruch w narożniku powinien być legalny");
        assertTrue(board.checkMove(0, 18, true), "Ruch w narożniku powinien być legalny");
        assertTrue(board.checkMove(18, 0, false), "Ruch w narożniku powinien być legalny");
    }
}