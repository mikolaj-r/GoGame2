package com.gogame.controller;

import com.gogame.Board;
import com.gogame.BoardHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * Reprezentuje sesję gry między dwoma graczami.
 * Zarządza stanem gry i komunikacją z klientami.
 */
public class Game extends Thread {

    /**
     * Stany gry podczas rozgrywki i negocjacji.
     */
    private enum State {
        PLAYING,
        NEGOTIATION_BLACK,
        CONFIRM_WHITE,
        NEGOTIATION_WHITE,
        CONFIRM_BLACK,
        FINISHED
    }

    private Socket socketBlack;
    private Socket socketWhite;
    private Board board;
    private Board scoringBoard;
    private State currentState = State.PLAYING;
    private boolean blackTurn = true;
    private int passCount = 0;
    private int pendingDeadRow = -1;
    private int pendingDeadCol = -1;

    /**
     * Tworzy nową sesję gry.
     *
     * @param black socket gracza czarnego
     * @param white socket gracza białego
     */
    public Game(Socket black, Socket white) {
        this.socketBlack = black;
        this.socketWhite = white;
        this.board = new Board();
        this.board.updateBreaths();
    }

    /**
     * Główna pętla gry obsługująca komunikację z klientami.
     */
    @Override
    public void run() {
        try {
            Thread.sleep(100);
            BufferedReader inBlack = new BufferedReader(new InputStreamReader(socketBlack.getInputStream()));
            PrintWriter outBlack = new PrintWriter(socketBlack.getOutputStream(), true);
            BufferedReader inWhite = new BufferedReader(new InputStreamReader(socketWhite.getInputStream()));
            PrintWriter outWhite = new PrintWriter(socketWhite.getOutputStream(), true);

            outBlack.println("INIT BLACK");
            outWhite.println("INIT WHITE");
            sendTurnUpdate(outBlack, outWhite);

            boolean connectionActive = true;
            while (connectionActive && currentState != State.FINISHED) {
                Socket activeSocket = getActiveSocket();
                BufferedReader activeIn = (activeSocket == socketBlack) ? inBlack : inWhite;

                String input = activeIn.readLine();
                if (input == null) break;

                System.out.println("SERVER: " + input + " [State: " + currentState + "]");

                processCommand(input, outBlack, outWhite);
            }
        } catch (IOException e) {
            System.out.println("Error, disconnect: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try { socketBlack.close(); socketWhite.close(); } catch (IOException e) {}
        }
    }

    /**
     * Zwraca socket aktywnego gracza w zależności od stanu gry.
     *
     * @return socket aktywnego gracza
     */
    private Socket getActiveSocket() {
        switch (currentState) {
            case PLAYING: return blackTurn ? socketBlack : socketWhite;
            case NEGOTIATION_BLACK: return socketBlack;
            case CONFIRM_WHITE: return socketWhite;
            case NEGOTIATION_WHITE: return socketWhite;
            case CONFIRM_BLACK: return socketBlack;
            default: return socketBlack;
        }
    }

    /**
     * Przetwarza komendę od gracza.
     *
     * @param input komenda
     * @param outBlack writer gracza czarnego
     * @param outWhite writer gracza białego
     */
    private void processCommand(String input, PrintWriter outBlack, PrintWriter outWhite) {
        PrintWriter currentOut = (getActiveSocket() == socketBlack) ? outBlack : outWhite;
        PrintWriter opponentOut = (getActiveSocket() == socketBlack) ? outWhite : outBlack;

        if (input.equals("SURRENDER")) {
            finishGame(outBlack, outWhite, (getActiveSocket() == socketBlack) ? "White" : "Black", "Surrender");
            return;
        }

        switch (currentState) {
            case PLAYING:
                handlePlaying(input, outBlack, outWhite, currentOut, opponentOut);
                break;
            case NEGOTIATION_BLACK:
            case NEGOTIATION_WHITE:
                handleNegotiationState(input, outBlack, outWhite, currentOut, opponentOut);
                break;
            case CONFIRM_WHITE:
            case CONFIRM_BLACK:
                handleConfirmation(input, outBlack, outWhite, currentOut, opponentOut);
                break;
        }
    }

    /**
     * Obsługuje komendy podczas rozgrywki.
     *
     * @param input komenda
     * @param outBlack writer gracza czarnego
     * @param outWhite writer gracza białego
     * @param currentOut writer aktywnego gracza
     * @param opponentOut writer przeciwnika
     */
    private void handlePlaying(String input, PrintWriter outBlack, PrintWriter outWhite, PrintWriter currentOut, PrintWriter opponentOut) {
        if (input.equals("PASS")) {
            passCount++;
            currentOut.println("MESSAGE You passed.");
            opponentOut.println("MESSAGE Opponent passed");
            blackTurn = !blackTurn;

            if (passCount >= 2) {
                initiateNegotiation(outBlack, outWhite);
            } else {
                sendTurnUpdate(outBlack, outWhite);
            }
        }
        else if (input.startsWith("MOVE")) {
            String[] parts = input.split(" ");
            int row = Integer.parseInt(parts[1]);
            int col = Integer.parseInt(parts[2]);

            if (board.checkMove(row, col, blackTurn)) {
                passCount = 0;
                board.copyToLast();
                BoardHelper.makeMove(board, row, col, blackTurn);

                String moveMsg = "MOVE_OK " + row + " " + col + " " + (blackTurn ? "true" : "false");
                outBlack.println(moveMsg);
                outWhite.println(moveMsg);

                blackTurn = !blackTurn;
                sendTurnUpdate(outBlack, outWhite);
            } else {
                currentOut.println("ERROR Invalid move.");
            }
        }
    }

    /**
     * Rozpoczyna fazę negocjacji martwych kamieni.
     *
     * @param outBlack writer gracza czarnego
     * @param outWhite writer gracza białego
     */
    private void initiateNegotiation(PrintWriter outBlack, PrintWriter outWhite) {
        this.scoringBoard = new Board();
        BoardHelper.copyBoardState(this.scoringBoard, this.board);

        currentState = State.NEGOTIATION_BLACK;

        outBlack.println("PHASE_NEGOTIATION");
        outWhite.println("PHASE_NEGOTIATION");

        outBlack.println("MESSAGE NEGOTIATION PHASE. Mark dead stones.");
        outWhite.println("MESSAGE NEGOTIATION PHASE. Wait for Black...");

        outBlack.println("YOUR_TURN");
    }

    /**
     * Obsługuje fazę negocjacji martwych kamieni.
     *
     * @param input komenda
     * @param outBlack writer gracza czarnego
     * @param outWhite writer gracza białego
     * @param currentOut writer aktywnego gracza
     * @param opponentOut writer przeciwnika
     */
    private void handleNegotiationState(String input, PrintWriter outBlack, PrintWriter outWhite, PrintWriter currentOut, PrintWriter opponentOut) {
        if (input.equals("PLAYON")) {
            currentState = State.PLAYING;
            passCount = 0;
            String msg = "MESSAGE Game resumed (PLAYON)!";
            outBlack.println(msg);
            outWhite.println(msg);

            outBlack.println("MARK_CLEAR");
            outWhite.println("MARK_CLEAR");

            sendTurnUpdate(outBlack, outWhite);
            return;
        }

        if (input.equals("DONE")) {
            if (currentState == State.NEGOTIATION_BLACK) {
                currentState = State.NEGOTIATION_WHITE;
                outWhite.println("PHASE_NEGOTIATION");
                outWhite.println("MESSAGE Your turn to mark.");
                outBlack.println("MESSAGE Black finished. Wait for White.");
                outWhite.println("YOUR_TURN");
            } else {
                calculateScore(outBlack, outWhite);
            }
            return;
        }

        if (input.startsWith("DEAD")) {
            String[] parts = input.split(" ");
            int row = Integer.parseInt(parts[1]);
            int col = Integer.parseInt(parts[2]);

            if (BoardHelper.getColor(scoringBoard, row, col) != 0) {
                pendingDeadRow = row;
                pendingDeadCol = col;

                List<int[]> group = BoardHelper.getChain(scoringBoard, row, col);
                for(int[] stone : group) {
                    String markMsg = "MARK " + stone[0] + " " + stone[1];
                    outBlack.println(markMsg);
                    outWhite.println(markMsg);
                }

                currentState = (currentState == State.NEGOTIATION_BLACK) ? State.CONFIRM_WHITE : State.CONFIRM_BLACK;
                currentOut.println("MESSAGE Waiting for enemy's approval...");
                opponentOut.println("CONFIRM_REQ " + row + " " + col + " Oppenent marked stones. Do you agree with him?");
            } else {
                currentOut.println("ERROR Choose valid position!");
            }
        }
        else {
            currentOut.println("ERROR Invalid command.");
            currentOut.println("PHASE_NEGOTIATION");
            currentOut.println("YOUR_TURN");
        }
    }

    /**
     * Obsługuje potwierdzanie martwych kamieni przez przeciwnika.
     *
     * @param input komenda (Y/N)
     * @param outBlack writer gracza czarnego
     * @param outWhite writer gracza białego
     * @param currentOut writer aktywnego gracza
     * @param opponentOut writer przeciwnika
     */
    private void handleConfirmation(String input, PrintWriter outBlack, PrintWriter outWhite, PrintWriter currentOut, PrintWriter opponentOut) {
        List<int[]> deadGroup = BoardHelper.getChain(scoringBoard, pendingDeadRow, pendingDeadCol);

        if (input.equalsIgnoreCase("Y")) {
            scoringBoard.finalRemoveChain(pendingDeadRow, pendingDeadCol);

            for (int[] stone : deadGroup) {
                int r = stone[0];
                int c = stone[1];
                board.positions[r][c].color = 0;
                board.positions[r][c].breaths = 0;
            }

            for (int[] stone : deadGroup) {
                String rmMsg = "DEAD_REMOVED " + stone[0] + " " + stone[1];
                outBlack.println(rmMsg);
                outWhite.println(rmMsg);
            }

            currentOut.println("MESSAGE Agreement. Stones are removed.");
            opponentOut.println("MESSAGE Agreement.");
        } else {
            for (int[] stone : deadGroup) {
                String clearMsg = "UNMARK " + stone[0] + " " + stone[1];
                outBlack.println(clearMsg);
                outWhite.println(clearMsg);
            }

            currentOut.println("MESSAGE No agreement.");
            opponentOut.println("MESSAGE Opponent did not agree.");
        }

        currentState = (currentState == State.CONFIRM_WHITE) ? State.NEGOTIATION_BLACK : State.NEGOTIATION_WHITE;
        opponentOut.println("YOUR_TURN");
    }

    /**
     * Oblicza końcowy wynik gry.
     *
     * @param outBlack writer gracza czarnego
     * @param outWhite writer gracza białego
     */
    private void calculateScore(PrintWriter outBlack, PrintWriter outWhite) {
        scoringBoard.calculateTerritories();

        int pointsBlack = 0;
        int pointsWhite = 0;

        for(int i = 0; i < 19; i++) {
            for(int j = 0; j < 19; j++) {
                int territory = scoringBoard.territoryCache[i][j];
                if(territory == 1) pointsWhite++;
                if(territory == 2) pointsBlack++;
            }
        }

        String result = "Score: Black=" + pointsBlack + ", White=" + pointsWhite;
        if (pointsWhite > pointsBlack) result += " -> WHTIE WON!";
        else if (pointsBlack > pointsWhite) result += " -> BLACK WON!";
        else result += " -> DRAW!";

        finishGame(outBlack, outWhite, result, "Game over");
    }

    /**
     * Kończy grę i wysyła wynik do graczy.
     *
     * @param outBlack writer gracza czarnego
     * @param outWhite writer gracza białego
     * @param result wynik gry
     * @param reason powód zakończenia
     */
    private void finishGame(PrintWriter outBlack, PrintWriter outWhite, String result, String reason) {
        outBlack.println("GAME_OVER " + result);
        outWhite.println("GAME_OVER " + result);
        currentState = State.FINISHED;
    }

    /**
     * Wysyła informację o turze do obu graczy.
     *
     * @param outBlack writer gracza czarnego
     * @param outWhite writer gracza białego
     */
    private void sendTurnUpdate(PrintWriter outBlack, PrintWriter outWhite) {
        if (blackTurn) {
            outBlack.println("MESSAGE Your move");
            outBlack.println("YOUR_TURN");
            outWhite.println("MESSAGE Opponents move (Black)...");
        } else {
            outWhite.println("MESSAGE Your move!");
            outWhite.println("YOUR_TURN");
            outBlack.println("MESSAGE Opponents move (White)...");
        }
    }
}