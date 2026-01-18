package com.gogame.client;

import com.gogame.server.Client;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Optional;

public class GoGameFX extends Application implements GameView {

    private static final int BOARD_SIZE = 19;
    private static final int TILE_SIZE = 30;
    private static final int PADDING = 20;
    private static final int CANVAS_SIZE = TILE_SIZE * (BOARD_SIZE - 1) + PADDING * 2;

    private Client connection;
    private GraphicsContext gc;
    private Label statusLabel;

    private Button passButton;
    private Button surrenderButton;
    private Button doneButton;
    private Button playOnButton;

    private int[][] boardState = new int[BOARD_SIZE][BOARD_SIZE];
    private boolean myTurn = false;
    private boolean negotiationPhase = false;

    private boolean[][] markedStones = new boolean[BOARD_SIZE][BOARD_SIZE];

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        connection = new Client("localhost", 8001, this);

        BorderPane root = new BorderPane();
        Canvas canvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        gc = canvas.getGraphicsContext2D();
        drawBoard();

        canvas.setOnMouseClicked(event -> {
            if (!myTurn) return;

            int col = (int) Math.round((event.getX() - PADDING) / TILE_SIZE);
            int row = (int) Math.round((event.getY() - PADDING) / TILE_SIZE);

            if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                if (negotiationPhase) {
                    if (boardState[row][col] != 0) {
                        System.out.println("Sending DEAD: " + row + ", " + col); // DEBUG
                        connection.sendDeadMark(row, col);
                        statusLabel.setText("Sending marking...");
                        myTurn = false;
                    } else {
                        statusLabel.setText("Choose occupied place!");
                    }
                } else {
                    connection.sendMove(row, col);
                    statusLabel.setText("Sending...");
                    myTurn = false;
                }
            }
        });

        VBox controls = new VBox(10);
        controls.setStyle("-fx-padding: 15; -fx-alignment: top-center; -fx-background-color: #eee;");

        statusLabel = new Label("Connecting...");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(180);

        passButton = new Button("PASS");
        passButton.setMaxWidth(Double.MAX_VALUE);
        passButton.setOnAction(e -> {
            if(myTurn && !negotiationPhase) {
                connection.sendPass();
                myTurn = false;
            }
        });

        surrenderButton = new Button("SURRENDER");
        surrenderButton.setMaxWidth(Double.MAX_VALUE);
        surrenderButton.setOnAction(e -> connection.sendSurrender());

        doneButton = new Button("DONE");
        doneButton.setMaxWidth(Double.MAX_VALUE);
        doneButton.setDisable(true);
        doneButton.setOnAction(e -> {
            if(myTurn && negotiationPhase) connection.sendDone();
        });

        playOnButton = new Button("CONTINUE GAME");
        playOnButton.setMaxWidth(Double.MAX_VALUE);
        playOnButton.setDisable(true);
        playOnButton.setOnAction(e -> connection.sendPlayOn());

        controls.getChildren().addAll(statusLabel, passButton, surrenderButton, new Separator(), doneButton, playOnButton);

        root.setCenter(canvas);
        root.setRight(controls);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Go Game");
        primaryStage.setOnCloseRequest(e -> {
            connection.sendQuit();
            connection.close();
            System.exit(0);
        });
        primaryStage.setScene(scene);
        primaryStage.show();

        connection.start();
    }

    @Override
    public void updateBoard(int row, int col, int playerId) {
        boardState[row][col] = playerId;
        drawBoard();
    }

    @Override
    public void removeStone(int row, int col) {
        boardState[row][col] = 0;
        drawBoard();
    }

    @Override
    public void highlightStone(int row, int col, boolean active) {
        if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
            System.out.println("Highlighting stone at " + row + "," + col + " = " + active); // DEBUG
            markedStones[row][col] = active;
            drawBoard();
        }
    }

    @Override
    public void clearAllHighlights() {
        for(int i=0; i<BOARD_SIZE; i++) {
            for(int j=0; j<BOARD_SIZE; j++) {
                markedStones[i][j] = false;
            }
        }
        drawBoard();
    }

    @Override
    public void showMessage(String message) {
        statusLabel.setText(message);
    }

    @Override
    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    @Override
    public void setPlayerColor(boolean isBlack) {
    }

    @Override
    public void setNegotiationPhase(boolean active) {
        System.out.println("Setting negotiation phase: " + active); // DEBUG
        this.negotiationPhase = active;
        doneButton.setDisable(!active);
        playOnButton.setDisable(!active);
        passButton.setDisable(active);

        if (active) {
            statusLabel.setText("NEGOTIATION PHASE. Mark stones.");
        } else {
            statusLabel.setText("Game resumed.");
        }
    }

    @Override
    public void showConfirmationDialog(String text) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Opponent's confirmation");
        alert.setHeaderText("Do you agree?");
        alert.setContentText(text);

        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeYes) {
            connection.sendConfirmation(true);
        } else {
            connection.sendConfirmation(false);
        }
    }

    @Override
    public void endGame(String result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game over");
        alert.setHeaderText(null);
        alert.setContentText(result);
        alert.show();
        passButton.setDisable(true);
        surrenderButton.setDisable(true);
        doneButton.setDisable(true);
        playOnButton.setDisable(true);
    }

    private void drawBoard() {
        gc.setFill(Color.web("#663c09"));
        gc.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);

        for (int i = 0; i < BOARD_SIZE; i++) {
            double pos = PADDING + i * TILE_SIZE;
            gc.strokeLine(pos, PADDING, pos, PADDING + (BOARD_SIZE - 1) * TILE_SIZE);
            gc.strokeLine(PADDING, pos, PADDING + (BOARD_SIZE - 1) * TILE_SIZE, pos);
        }

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (boardState[r][c] != 0) {
                    drawStone(r, c, boardState[r][c] == 1 ? Color.BLACK : Color.WHITE);
                }

                if (markedStones[r][c]) {
                    drawMark(r, c);
                }
            }
        }
    }

    private void drawStone(int row, int col, Color color) {
        double x = PADDING + col * TILE_SIZE;
        double y = PADDING + row * TILE_SIZE;
        double r = TILE_SIZE * 0.45;
        gc.setFill(color);
        gc.fillOval(x - r, y - r, 2 * r, 2 * r);
        if (color == Color.WHITE) {
            gc.setStroke(Color.GRAY);
            gc.strokeOval(x - r, y - r, 2 * r, 2 * r);
        }
    }

    private void drawMark(int row, int col) {
        double x = PADDING + col * TILE_SIZE;
        double y = PADDING + row * TILE_SIZE;
        double r = TILE_SIZE * 0.3;

        gc.setStroke(Color.RED);
        gc.setLineWidth(3.0);
        gc.strokeLine(x - r, y - r, x + r, y + r);
        gc.strokeLine(x + r, y - r, x - r, y + r);
    }
}