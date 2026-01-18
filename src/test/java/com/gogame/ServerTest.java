package com.gogame;

import com.gogame.controller.Game;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerTest {
    static class MockSocket extends Socket {
        private ByteArrayInputStream input;
        private ByteArrayOutputStream output;

        public MockSocket(String inputData) {
            this.input = new ByteArrayInputStream(inputData.getBytes());
            this.output = new ByteArrayOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return input;
        }

        @Override
        public OutputStream getOutputStream() {
            return output;
        }

        // Metoda pomocnicza do odczytania tego, co serwer wysłał do tego klienta
        public String getOutput() {
            return output.toString();
        }
    }

    @Test
    public void testInitialization() {
        // Symulujemy, że gracze wchodzą i od razu wychodzą (QUIT), żeby zakończyć wątek
        MockSocket p1 = new MockSocket("QUIT\n");
        MockSocket p2 = new MockSocket("QUIT\n");

        Game session = new Game(p1, p2);
        session.run();

        // Sprawdzamy czy serwer wysłał powitanie i przydzielił kolory
        String out1 = p1.getOutput();
        String out2 = p2.getOutput();

        assertTrue(out1.contains("INIT BLACK"), "Player 1 should play as BLACK");
        assertTrue(out2.contains("INIT WHITE"), "Player 2 should play as WHITE");
    }

    @Test
    public void testValidMoveFlow() {
        // 1. P1 wykonuje ruch na A1 (MOVE 0 0).
        // 2. Potem P1 wychodzi.
        // 3. Gracz P2 czeka i wychodzi.

        String p1Input = "MOVE 0 0\nQUIT\n";
        String p2Input = "QUIT\n";

        MockSocket p1 = new MockSocket(p1Input);
        MockSocket p2 = new MockSocket(p2Input);

        Game session = new Game(p1, p2);
        session.run();

        String out1 = p1.getOutput();
        String out2 = p2.getOutput();

        assertTrue(out1.contains("MOVE_OK 0 0 true"), "Server should broadcast move to P1");
        assertTrue(out2.contains("MOVE_OK 0 0 true"), "Server should broadcast move to P2");

        assertTrue(out2.contains("YOUR_TURN"), "Player 2 receives message about turn");
    }

    @Test
    public void testInvalidMove() {
        // 1. P1 wykonuje ruch A1 (Poprawny).
        // 2. P2 próbuje wykonać ruch A1 (Zajęte -> Błąd).
        // 3. P2 wykonuje ruch A2 (Poprawny).

        String p1Input = "MOVE 0 0\nQUIT\n"; // Ruch 0,0 i wyjście

        // P2 musi poczekać na ruch P1
        // potem błędny ruch, potem poprawny
        String p2Input = "MOVE 0 0\nMOVE 0 1\nQUIT\n";

        MockSocket p1 = new MockSocket(p1Input);
        MockSocket p2 = new MockSocket(p2Input);

        Game session = new Game(p1, p2);
        session.run();

        String out2 = p2.getOutput();

        // P2 powinien dostać komunikat błędu po pierwszym ruchu
        assertTrue(out2.contains("ERROR"), "P2 should get error");
        // P2 powinien dostać potwierdzenie drugiego, poprawnego ruchu (0,1)
        assertTrue(out2.contains("MOVE_OK 0 1 false"), "P2 should get correct move notification");
    }
}
