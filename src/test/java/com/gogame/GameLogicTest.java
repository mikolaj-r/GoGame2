package com.gogame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameLogicTest {
    @Test
    public void testBoardCreationAndBreaths() {
        Board board = new Board();
        assertNotNull(board.positions);

        // Symulacja ruchu
        board.move(0, 0, true); // Czarne na A1
        board.updateBreaths();

        // A1 ma 2 oddechy (róg)
        assertEquals(2, board.positions[0][0].breaths);
        assertEquals(2, board.positions[0][0].color); // 2 = czarne
    }

    @Test
    public void testCaptureLogic() {
        // Test przejmowania kamieni
        Board board = new Board();

        // Czarny na środku
        board.move(5, 5, true);

        // Otaczamy Białymi
        board.move(4, 5, false);
        board.move(6, 5, false);
        board.move(5, 4, false);
        board.move(5, 6, false);

        board.updateBreaths();

        // Przed checkBoard oddechy powinny być 0
        assertEquals(0, board.positions[5][5].breaths);

        board.checkBoard();

        // Kamień powinien zniknąć (color = 0)
        assertEquals(0, board.positions[5][5].color);
    }

    @Test
    public void testMoveValidation() {
        Board board = new Board();
        // Ruch na puste pole -> OK
        assertTrue(board.checkMove(10, 10));

        board.move(10, 10, true);

        // Ruch na zajęte pole -> False
        assertFalse(board.checkMove(10, 10));
    }
}
