package com.gogame.server;

import com.gogame.controller.Game;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Serwer gry Go obsługujący połączenia klientów.
 * Tworzy sesje gry dla par graczy.
 *
 * Creator
 * pełni rolę "twórcy" dla obiektów Game.
 */
public class Server {
    /**
     * Uruchamia serwer i nasłuchuje połączeń.
     *
     * @param args argumenty linii poleceń
     * @throws Exception w przypadku błędu I/O
     */
    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(8001)) {
            System.out.println("SERVER IS WORKING");
            while (true) {
                Socket player1 = listener.accept();
                System.out.println("Player 1 connected (Black)");
                PrintWriter out1 = new PrintWriter(player1.getOutputStream(), true);
                out1.println("MESSAGE Waiting for opponent...");

                Socket player2 = listener.accept();
                System.out.println("Player 2 connected (White)");
                PrintWriter out2 = new PrintWriter(player2.getOutputStream(), true);
                out2.println("MESSAGE Opponent connected. Game starts.");

                // Utworzenie sesji gry (Controller)
                Game game = new Game(player1, player2);
                game.start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}