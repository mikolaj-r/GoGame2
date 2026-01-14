package com.gogame.controller;

import com.gogame.Board;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Controller
 * Pełni rolę Kontrolera. Steruje przepływem zdarzeń w grze, waliduje ruchy wykorzystując model Board
 *
 * High Cohesion
 * Skupia się wyłącznie na logice przebiegu gry, nie zajmuje się łączeniem w sieci ani wyswietlaniem planszy
 *
 * Wzorzec facade
 * Ukrywa złożoność logiki gry, obsługi strumieni wejścia/wyjścia
 * oraz synchronizacji graczy, udostępniając prosty interfejs uruchomieniowy (metoda run)
 */

public class Game extends Thread {
    private Socket playerBlack;
    private Socket playerWhite;
    private Board masterBoard;


    public Game(Socket black, Socket white) {
        this.playerBlack = black;
        this.playerWhite = white;
        this.masterBoard = new Board();
        this.masterBoard.updateBreaths();
    }
    @Override
    public void run() {
        try {
            BufferedReader inBlack = new BufferedReader(new InputStreamReader(playerBlack.getInputStream()));
            PrintWriter outBlack = new PrintWriter(playerBlack.getOutputStream(), true);
            BufferedReader inWhite = new BufferedReader(new InputStreamReader(playerWhite.getInputStream()));
            PrintWriter outWhite = new PrintWriter(playerWhite.getOutputStream(), true);

            outBlack.println("INIT BLACK");
            outWhite.println("INIT WHITE");

            boolean blackTurn = true;
            boolean keepPlaying = true;
            int passCount = 0;

            while (keepPlaying) {
                // Ustalenie aktywnego gracza
                PrintWriter currentOut = blackTurn ? outBlack : outWhite;
                BufferedReader currentIn = blackTurn ? inBlack : inWhite;
                PrintWriter opponentOut = blackTurn ? outWhite : outBlack;

                currentOut.println("YOUR_TURN");
                opponentOut.println("MESSAGE Opponent is thinking...");

                String input = currentIn.readLine();
                if (input == null) break;

                if (input.equals("PASS")) {
                    passCount++;
                    currentOut.println("MESSAGE You passed.");
                    opponentOut.println("MESSAGE Opponent passed.");
                    if (passCount >= 2) {
                        outBlack.println("GAME_OVER");
                        outWhite.println("GAME_OVER");
                        keepPlaying = false;
                        break;
                    }
                    blackTurn = !blackTurn;

                //Wzorzec command
                //Komunikacja odbywa się poprzez przesyłanie poleceń w formie tekstowej
                //i zamienia polecenia na konkretne instrukcje
                } else if (input.startsWith("MOVE")) {
                    // Oczekiwany format od klienta: "MOVE <row> <col>"
                    // Klient już przeliczył A1 na liczby, serwer tylko waliduje.
                    String[] parts = input.split(" ");
                    int row = Integer.parseInt(parts[1]);
                    int col = Integer.parseInt(parts[2]);

                    // Information Expert - Board decyduje czy ruch jest legalny
                    if (masterBoard.checkMove(row, col)) {
                        passCount = 0;

                        // Wykonaj ruch na serwerze
                        masterBoard.move(row, col, blackTurn);
                        masterBoard.updateBreaths();
                        masterBoard.checkBoard();

                        // Wyślij potwierdzenie ruchu do obu klientów, aby zaktualizowali swoje plansze
                        String moveCmd = "MOVE_OK " + row + " " + col + " " + (blackTurn ? "true" : "false");
                        outBlack.println(moveCmd);
                        outWhite.println(moveCmd);

                        blackTurn = !blackTurn;
                    } else {
                        currentOut.println("ERROR Invalid move. Try again");
                    }
                } else if (input.equals("QUIT")) {
                    keepPlaying = false;
                }
            }
        } catch (IOException e) {
            System.out.println("Disconnection: " + e.getMessage());
        } finally {
            try {
                playerBlack.close();
                playerWhite.close();
            } catch (IOException e) {
                System.out.println("Session ended");
            }
        }
    }
}