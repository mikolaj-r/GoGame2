package com.gogame.client;

public interface GameView {
    void showMessage(String message);
    void updateBoard(int row, int col, int playerId);

    void removeStone(int row, int col);

    void highlightStone(int row, int col, boolean active);
    void clearAllHighlights();

    void setMyTurn(boolean myTurn);
    void setPlayerColor(boolean isBlack);
    void endGame(String result);

    void setNegotiationPhase(boolean active);
    void showConfirmationDialog(String text);
}