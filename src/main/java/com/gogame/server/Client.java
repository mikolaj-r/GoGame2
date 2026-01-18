package com.gogame.server;

import com.gogame.client.GameView;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;

/**
 * Klient gry Go łączący się z serwerem.
 * Obsługuje komunikację sieciową i aktualizuje widok gry.
 */
public class Client extends Thread {
    private final String host;
    private final int port;
    private final GameView view;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Tworzy nowego klienta gry.
     *
     * @param host adres hosta serwera
     * @param port port serwera
     * @param view interfejs widoku gry
     */
    public Client(String host, int port, GameView view) {
        this.host = host;
        this.port = port;
        this.view = view;
    }

    /**
     * Główna pętla klienta obsługująca komunikację z serwerem.
     */
    @Override
    public void run() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Platform.runLater(() -> view.showMessage("Connected to server"));

            String line;
            while ((line = in.readLine()) != null) {
                processServerMessage(line);
            }
        } catch (IOException e) {
            Platform.runLater(() -> view.showMessage("Disconnected: " + e.getMessage()));
        } finally {
            close();
        }
    }

    private boolean isNegotiation = false;

    /**
     * Przetwarza wiadomość otrzymaną z serwera.
     *
     * @param message wiadomość od serwera
     */
    private void processServerMessage(String message) {
        Platform.runLater(() -> {
            System.out.println("CLIENT IN: " + message);

            try {
                if (message.startsWith("INIT")) {
                    boolean isBlack = message.contains("BLACK");
                    view.setPlayerColor(isBlack);
                }
                else if (message.equals("YOUR_TURN")) {
                    view.setMyTurn(true);
                    if (isNegotiation) {
                        view.showMessage("NEGOTATION: Mark dead stones.");
                    } else {
                        view.showMessage("Your move");
                    }
                }
                else if (message.startsWith("MESSAGE")) {
                    String content = message.substring(8);
                    view.showMessage(content);
                    if (content.contains("thinking") || content.contains("Opponent") || content.contains("Wait")) {
                        view.setMyTurn(false);
                    }
                }
                else if (message.startsWith("MOVE_OK")) {
                    String[] parts = message.split(" ");
                    int row = Integer.parseInt(parts[1]);
                    int col = Integer.parseInt(parts[2]);
                    boolean isBlackMove = Boolean.parseBoolean(parts[3]);
                    view.updateBoard(row, col, isBlackMove ? 1 : 2);
                }
                else if (message.startsWith("UPDATE")) {
                    try {
                        String[] parts = message.split(" ");
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);
                        int color = Integer.parseInt(parts[3]); // Powinno być 0
                        view.updateBoard(row, col, color);
                    } catch (Exception e) {
                        System.err.println("Error UPDATE: " + message);
                    }
                }
                else if (message.startsWith("PHASE_NEGOTIATION")) {
                    isNegotiation = true;
                    view.setNegotiationPhase(true);
                    view.showMessage("NEGOTIATION PHASE");
                }

                else if (message.startsWith("MARK_CLEAR")) {
                    view.clearAllHighlights();
                }

                else if (message.startsWith("MARK")) {
                    try {
                        String[] parts = message.split(" ");
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);
                        view.highlightStone(row, col, true);
                    } catch (Exception e) {
                        System.err.println("Parsing error MARK: " + message);
                    }
                }
                else if (message.startsWith("UNMARK")) {
                    try {
                        String[] parts = message.split(" ");
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);
                        view.highlightStone(row, col, false);
                    } catch (Exception e) {}
                }
                // -----------------------

                else if (message.startsWith("CONFIRM_REQ")) {
                    String text = message.substring(message.indexOf(" ", 12) + 1);
                    view.showConfirmationDialog(text);
                }
                else if (message.startsWith("DEAD_REMOVED")) {
                    String[] parts = message.split(" ");
                    int row = Integer.parseInt(parts[1]);
                    int col = Integer.parseInt(parts[2]);

                    view.highlightStone(row, col, false);
                    view.removeStone(row, col);
                }
                else if (message.startsWith("GAME_OVER")) {
                    view.setMyTurn(false);
                    view.endGame(message.substring(10));
                }
                else if (message.startsWith("ERROR")) {
                    view.showMessage(message.substring(6));
                    view.setMyTurn(true);
                }
            } catch (Exception e) {
                System.err.println("Message fail: " + message);
                e.printStackTrace();
            }
        });
    }

    public void sendMove(int row, int col) { if(out!=null) out.println("MOVE " + row + " " + col); }
    public void sendPass() { if(out!=null) out.println("PASS"); }
    public void sendQuit() { if(out!=null) out.println("QUIT"); }
    public void sendSurrender() { if(out!=null) out.println("SURRENDER"); }
    public void sendDeadMark(int row, int col) { if(out!=null) out.println("DEAD " + row + " " + col); }
    public void sendConfirmation(boolean agree) { if(out!=null) out.println(agree ? "Y" : "N"); }
    public void sendDone() { if(out!=null) out.println("DONE"); }
    public void sendPlayOn() {
        if(out!=null) out.println("PLAYON");
        Platform.runLater(() -> {
            isNegotiation = false;
            view.setNegotiationPhase(false);
            view.clearAllHighlights();
        });
    }

    public void close() { try { if(socket!=null) socket.close(); } catch(Exception e){} }
}