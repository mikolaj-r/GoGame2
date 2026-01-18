package com.gogame;

import com.gogame.Board;
import com.gogame.controller.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testy dla klasy Game.
 */
class GameTest {

    private Socket mockBlack;
    private Socket mockWhite;
    private ByteArrayOutputStream outBlack;
    private ByteArrayOutputStream outWhite;
    private ByteArrayInputStream inBlack;
    private ByteArrayInputStream inWhite;

    /**
     * Przygotowanie mocków przed każdym testem.
     */
    @BeforeEach
    void setUp() throws IOException {
        mockBlack = mock(Socket.class);
        mockWhite = mock(Socket.class);

        outBlack = new ByteArrayOutputStream();
        outWhite = new ByteArrayOutputStream();

        when(mockBlack.getOutputStream()).thenReturn(outBlack);
        when(mockWhite.getOutputStream()).thenReturn(outWhite);
    }

    /**
     * Test tworzenia gry.
     */
    @Test
    void testGameCreation() {
        Game game = new Game(mockBlack, mockWhite);
        assertNotNull(game, "Gra powinna być utworzona");
    }

    /**
     * Test inicjalizacji graczy.
     */
    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testGameInitialization() throws Exception {
        inBlack = new ByteArrayInputStream("QUIT\n".getBytes());
        inWhite = new ByteArrayInputStream("QUIT\n".getBytes());

        when(mockBlack.getInputStream()).thenReturn(inBlack);
        when(mockWhite.getInputStream()).thenReturn(inWhite);

        Game game = new Game(mockBlack, mockWhite);
        game.start();
        game.join(1000);

        String blackOutput = outBlack.toString();
        String whiteOutput = outWhite.toString();

        assertTrue(blackOutput.contains("INIT BLACK"), "Czarny powinien dostać INIT BLACK");
        assertTrue(whiteOutput.contains("INIT WHITE"), "Biały powinien dostać INIT WHITE");
    }

    /**
     * Test komendy PASS.
     */
    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testPassCommand() throws Exception {
        inBlack = new ByteArrayInputStream("PASS\nQUIT\n".getBytes());
        inWhite = new ByteArrayInputStream("QUIT\n".getBytes());

        when(mockBlack.getInputStream()).thenReturn(inBlack);
        when(mockWhite.getInputStream()).thenReturn(inWhite);

        Game game = new Game(mockBlack, mockWhite);
        game.start();
        game.join(1000);

        String blackOutput = outBlack.toString();
        assertTrue(blackOutput.contains("You passed"), "Czarny powinien dostać potwierdzenie passa");
    }

    /**
     * Test komendy SURRENDER.
     */
    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testSurrenderCommand() throws Exception {
        inBlack = new ByteArrayInputStream("SURRENDER\n".getBytes());
        inWhite = new ByteArrayInputStream("".getBytes());

        when(mockBlack.getInputStream()).thenReturn(inBlack);
        when(mockWhite.getInputStream()).thenReturn(inWhite);

        Game game = new Game(mockBlack, mockWhite);
        game.start();
        game.join(1000);

        String blackOutput = outBlack.toString();
        String whiteOutput = outWhite.toString();

        assertTrue(blackOutput.contains("GAME_OVER") || whiteOutput.contains("GAME_OVER"),
                "Powinna być komenda GAME_OVER");
    }

    /**
     * Test poprawnego ruchu.
     */
    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testValidMove() throws Exception {
        inBlack = new ByteArrayInputStream("MOVE 9 9\nQUIT\n".getBytes());
        inWhite = new ByteArrayInputStream("QUIT\n".getBytes());

        when(mockBlack.getInputStream()).thenReturn(inBlack);
        when(mockWhite.getInputStream()).thenReturn(inWhite);

        Game game = new Game(mockBlack, mockWhite);
        game.start();
        game.join(1000);

        String blackOutput = outBlack.toString();
        assertTrue(blackOutput.contains("MOVE_OK"), "Powinno być MOVE_OK dla poprawnego ruchu");
    }

    /**
     * Test dwóch passów rozpoczynających negocjację.
     */
    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testTwoPassesStartNegotiation() throws Exception {
        inBlack = new ByteArrayInputStream("PASS\nDONE\n".getBytes());
        inWhite = new ByteArrayInputStream("PASS\nDONE\n".getBytes());

        when(mockBlack.getInputStream()).thenReturn(inBlack);
        when(mockWhite.getInputStream()).thenReturn(inWhite);

        Game game = new Game(mockBlack, mockWhite);
        game.start();
        game.join(2000);

        String blackOutput = outBlack.toString();
        String whiteOutput = outWhite.toString();

        assertTrue(blackOutput.contains("PHASE_NEGOTIATION") || whiteOutput.contains("PHASE_NEGOTIATION"),
                "Dwa passy powinny rozpocząć fazę negocjacji");
    }

    /**
     * Test komendy PLAYON.
     */
    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testPlayOnCommand() throws Exception {
        inBlack = new ByteArrayInputStream("PASS\nPLAYON\nQUIT\n".getBytes());
        inWhite = new ByteArrayInputStream("PASS\nQUIT\n".getBytes());

        when(mockBlack.getInputStream()).thenReturn(inBlack);
        when(mockWhite.getInputStream()).thenReturn(inWhite);

        Game game = new Game(mockBlack, mockWhite);
        game.start();
        game.join(2000);

        String output = outBlack.toString() + outWhite.toString();
        assertTrue(output.contains("PLAYON") || output.contains("resumed"),
                "PLAYON powinien wznowić grę");
    }

    /**
     * Test zamykania socketów.
     */
    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testSocketsClosed() throws Exception {
        inBlack = new ByteArrayInputStream("".getBytes());
        inWhite = new ByteArrayInputStream("".getBytes());

        when(mockBlack.getInputStream()).thenReturn(inBlack);
        when(mockWhite.getInputStream()).thenReturn(inWhite);

        Game game = new Game(mockBlack, mockWhite);
        game.start();
        game.join(1000);

        verify(mockBlack, atLeastOnce()).close();
        verify(mockWhite, atLeastOnce()).close();
    }

    /**
     * Test alternacji tur.
     */
    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testTurnAlternation() throws Exception {
        inBlack = new ByteArrayInputStream("MOVE 5 5\nQUIT\n".getBytes());
        inWhite = new ByteArrayInputStream("QUIT\n".getBytes());

        when(mockBlack.getInputStream()).thenReturn(inBlack);
        when(mockWhite.getInputStream()).thenReturn(inWhite);

        Game game = new Game(mockBlack, mockWhite);
        game.start();
        game.join(1000);

        String whiteOutput = outWhite.toString();
        assertTrue(whiteOutput.contains("YOUR_TURN"),
                "Po ruchu czarnego biały powinien dostać YOUR_TURN");
    }

    /**
     * Test komunikatu dla gracza oczekującego.
     */
    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testWaitingPlayerMessage() throws Exception {
        inBlack = new ByteArrayInputStream("MOVE 5 5\nQUIT\n".getBytes());
        inWhite = new ByteArrayInputStream("QUIT\n".getBytes());

        when(mockBlack.getInputStream()).thenReturn(inBlack);
        when(mockWhite.getInputStream()).thenReturn(inWhite);

        Game game = new Game(mockBlack, mockWhite);
        game.start();
        game.join(1000);

        String blackOutput = outBlack.toString();
        assertTrue(blackOutput.contains("Opponent") || blackOutput.contains("White"),
                "Czarny powinien dostać info o turze przeciwnika");
    }
}