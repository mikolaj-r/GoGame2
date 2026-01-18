package com.gogame.client;

/**
 * Interfejs widoku gry dla klienta.
 * Definiuje metody komunikacji między logiką klienta a interfejsem użytkownika.
 */
public interface GameView {
    /**
     * Wyświetla wiadomość użytkownikowi.
     *
     * @param message treść wiadomości
     */
    void showMessage(String message);

    /**
     * Aktualizuje stan planszy po ruchu.
     *
     * @param row wiersz
     * @param col kolumna
     * @param playerId ID gracza (1-biały, 2-czarny)
     */
    void updateBoard(int row, int col, int playerId);

    /**
     * Usuwa kamień z planszy.
     *
     * @param row wiersz
     * @param col kolumna
     */
    void removeStone(int row, int col);

    /**
     * Podświetla lub usuwa podświetlenie kamienia.
     *
     * @param row wiersz
     * @param col kolumna
     * @param active true aby podświetlić, false aby usunąć
     */
    void highlightStone(int row, int col, boolean active);

    /**
     * Usuwa wszystkie podświetlenia z planszy.
     */
    void clearAllHighlights();

    /**
     * Ustawia czy jest tura gracza.
     *
     * @param myTurn true jeśli tura gracza
     */
    void setMyTurn(boolean myTurn);

    /**
     * Ustawia kolor gracza.
     *
     * @param isBlack true jeśli czarny, false jeśli biały
     */
    void setPlayerColor(boolean isBlack);

    /**
     * Kończy grę i wyświetla wynik.
     *
     * @param result wynik gry
     */
    void endGame(String result);

    /**
     * Ustawia fazę negocjacji martwych kamieni.
     *
     * @param active true aby aktywować fazę negocjacji
     */
    void setNegotiationPhase(boolean active);

    /**
     * Wyświetla dialog potwierdzenia dla gracza.
     *
     * @param text treść pytania
     */
    void showConfirmationDialog(String text);
}