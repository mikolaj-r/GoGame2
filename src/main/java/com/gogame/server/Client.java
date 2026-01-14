package com.gogame.server;

import com.gogame.Board;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

/**
 * Pure Fabrication
 * Obsługuje komunikacje sieciową i interakcje z użytkownikiem.
 * Służy jako adapter między graczem a serwerem.
 *
 * Low Coupling
 * Klient nie decyduje o poprawności ruchów (zależy to od serwera).
 */

public class Client {
    // wzorzec proxy
    // Obiekt board jest lokalnym reprezentantem stanu gry.
    // Nie wykonuje walidacji, wyswietla to co zatwierdzone przez serwer.
    private Board board;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner console;
    private boolean isMyTurn = false;

    public void start() throws Exception {
        socket = new Socket("localhost", 8001);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        console = new Scanner(System.in);

        // Creator - Klient tworzy swoją planszę do wyświetlania
        board = new Board();
        board.updateBreaths();

        System.out.println("Connected to server.");

        // Pętla nasłuchująca komunikatów od serwera
        try {
            String response;
            // Pętla czytająca komunikaty serwera
            while ((response = in.readLine()) != null) {
                if (response.startsWith("INIT")) {
                    String color = response.split(" ")[1];
                    System.out.println("You are player: " + color);
                    board.show();
                }
                else if (response.startsWith("MESSAGE")) {
                    System.out.println("Server: " + response.substring(8));
                }
                else if (response.startsWith("ERROR")) {
                    System.out.println(response.substring(6));
                    if(isMyTurn) handleInput();
                }
                else if (response.equals("YOUR_TURN")) {
                    isMyTurn = true;
                    System.out.println("Your move (e.g. A1) or PASS:");
                    handleInput();
                }
                else if (response.startsWith("MOVE_OK")) {
                    String[] parts = response.split(" ");
                    int row = Integer.parseInt(parts[1]);
                    int col = Integer.parseInt(parts[2]);
                    boolean isBlack = parts[3].equals("true");

                    board.move(row, col, isBlack);
                    board.updateBreaths();
                    board.checkBoard();

                    System.out.println("\nBoard updated:");
                    board.show();
                    isMyTurn = false;
                }
                else if (response.equals("GAME_OVER")) {
                    System.out.println("Game Over! (Score calculated on server logs)");
                    break;
                }
            }
        } catch (SocketException e) {
            System.out.println("Connection closed by server");
        } catch (IOException e) {
            System.out.println("I/O ERROR: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException e) {}
            System.out.println("Client closed.");
        }
    }

    private void handleInput() {
        while (true) {
            String input = console.nextLine().trim().toUpperCase();
            if (input.equals("PASS")) {
                out.println("PASS");
                break;
            }

            if (input.length() < 2) {
                System.out.println("Invalid format. Use ColRow (e.g. A1).");
                continue;
            }

            char colChar = input.charAt(0);
            if (colChar < 'A' || colChar > 'S') {
                System.out.println("Invalid column.");
                continue;
            }

            try {
                int col = colChar - 'A';
                String rowStr = input.substring(1);
                int row = Integer.parseInt(rowStr) - 1; // Konwersja 1-19 na 0-18

                if (row < 0 || row > 18) {
                    System.out.println("Row out of bounds.");
                    continue;
                }

                // Wysyłamy przetworzone współrzędne do serwera
                out.println("MOVE " + row + " " + col);
                break;

            } catch (NumberFormatException e) {
                System.out.println("Invalid row number.");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            new Client().start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}