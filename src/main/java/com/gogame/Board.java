package com.gogame;

/**
 * Reprezentuje planszę do gry Go 19x19.
 * Przechowuje stan gry i zarządza logiką ruchów.
 */

public class Board {
    public Cell[][] positions; //0 puste 1 biale 2 czarne
    public boolean[][] visited = new boolean[19][19];
    public int[][] lastPositions = new int[19][19];
    public int[][] whiteVisited = new int[19][19];
    public int[][] blackVisited = new int[19][19];
    public int[][] territoryCache = new int[19][19]; //0 - nie obliczone, 1 - bialego, 2 - czarnego, 3 - niczyje

    public Board() {
        this.positions = new Cell[19][19];
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                // GRASP: Creator
                // Board tworzy obiekty Cell, bo je przechowuje i nimi zarządza
                positions[i][j] = new Cell();
            }
        }
    }

    /**
     * Kopiuje aktualny stan planszy do lastPositions.
     */
    public void copyToLast() {
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                lastPositions[i][j] = positions[i][j].color;
            }
        }
    }

    /**
     * Usuwa łańcuch kamieni i dodaje punkty podczas liczenia końcowego.
     *
     * @param row wiersz początkowy
     * @param column kolumna początkowa
     */
    public void finalRemoveChain(int row, int column) {
        int color = positions[row][column].color;
        if (color == 0 || visited[row][column])
            return;

        visited[row][column] = true;

        if (color == 1)
            App.finalPointsBlack++;
        if (color == 2)
            App.finalPointsWhite++;

        positions[row][column].color = 0;

        int nextRow;
        int nextColumn;

        nextRow = row + 1;
        if (nextRow < 19) {
            if (!visited[nextRow][column] && positions[nextRow][column].color == color)
                removeChain(nextRow, column);
        }

        nextRow = row - 1;
        if (nextRow >= 0) {
            if (!visited[nextRow][column] && positions[nextRow][column].color == color)
                removeChain(nextRow, column);
        }

        nextColumn = column + 1;
        if (nextColumn < 19) {
            if (!visited[row][nextColumn] && positions[row][nextColumn].color == color)
                removeChain(row, nextColumn);
        }

        nextColumn = column - 1;
        if (nextColumn >= 0) {
            if (!visited[row][nextColumn] && positions[row][nextColumn].color == color)
                removeChain(row, nextColumn);
        }
    }

    /**
     * Aktualizuje liczbę oddechów dla wszystkich pól na planszy.
     */
    public void updateBreaths() {
        // GRASP: Information Expert
        // Board zna układ planszy i sąsiedztwa pól,
        // więc to on liczy oddechy

        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                int breath = 0;

                if (j > 0) {
                    if (positions[i][j - 1].color == 0)  //lewo
                    {
                        breath++;
                    }
                }
                if (j < 18) {
                    if (positions[i][j + 1].color == 0)  //prawo
                    {
                        breath++;
                    }
                }
                if (i > 0) {
                    if (positions[i - 1][j].color == 0)  //gora
                    {
                        breath++;
                    }
                }
                if (i < 18) {
                    if (positions[i + 1][j].color == 0)  // dol
                    {
                        breath++;
                    }
                }
                // GRASP: Information Expert
                // Board aktualizuje stan Cell, bo jest właścicielem danych
                positions[i][j].breaths = breath;

            }
        }
    }

    /**
     * Sprawdza czy łańcuch jest żywy i usuwa go jeśli martwy.
     *
     * @param row wiersz
     * @param column kolumna
     */
    public void checkChains(int row, int column)  //color true - black, false = white. jak chainalive to git, jak 0 to clearuje
    {
        int color = positions[row][column].color;
        if (isChainAlive(row, column, color) == 0) {
            visited = new boolean[19][19];
            removeChain(row, column);
        }
    }

    /**
     * Usuwa łańcuch kamieni i dodaje punkty.
     *
     * @param row wiersz
     * @param column kolumna
     */
    public void removeChain(int row, int column) {
        int color = positions[row][column].color;
        if (color == 0 || visited[row][column])
            return;

        visited[row][column] = true;

        if (color == 1)
            App.pointsBlack++;
        if (color == 2)
            App.pointsWhite++;

        positions[row][column].color = 0;

        int nextRow;
        int nextColumn;

        nextRow = row + 1;
        if (nextRow < 19) {
            if (!visited[nextRow][column] && positions[nextRow][column].color == color)
                removeChain(nextRow, column);
        }

        nextRow = row - 1;
        if (nextRow >= 0) {
            if (!visited[nextRow][column] && positions[nextRow][column].color == color)
                removeChain(nextRow, column);
        }

        nextColumn = column + 1;
        if (nextColumn < 19) {
            if (!visited[row][nextColumn] && positions[row][nextColumn].color == color)
                removeChain(row, nextColumn);
        }

        nextColumn = column - 1;
        if (nextColumn >= 0) {
            if (!visited[row][nextColumn] && positions[row][nextColumn].color == color)
                removeChain(row, nextColumn);
        }
    }

    /**
     * Sprawdza czy łańcuch kamieni ma oddechy.
     *
     * @param row wiersz
     * @param column kolumna
     * @param color kolor kamienia
     * @return 1 jeśli żywy, 0 jeśli martwy
     */
    public int isChainAlive(int row, int column, int color) {
        visited[row][column] = true;
        if (positions[row][column].breaths > 0) //najwazniejszy fragment! jesli jakikolwiek w lancuchu zywy to caly lancuch zywy
            return 1;
        int nextRow;
        int nextColumn;
        nextRow = row + 1;
        if (nextRow < 19) {
            if (!visited[nextRow][column] && positions[nextRow][column].color == color) {
                if (isChainAlive(nextRow, column, color) == 1)
                    return 1;
            }
        }
        nextRow = row - 1;
        if (nextRow >= 0) {
            if (!visited[nextRow][column] && positions[nextRow][column].color == color) {
                if (isChainAlive(nextRow, column, color) == 1)
                    return 1;
            }
        }
        nextColumn = column + 1;
        if (nextColumn < 19) {
            if (!visited[row][nextColumn] && positions[row][nextColumn].color == color) {
                if (isChainAlive(row, nextColumn, color) == 1)
                    return 1;
            }
        }
        nextColumn = column - 1;
        if (nextColumn >= 0) {
            if (!visited[row][nextColumn] && positions[row][nextColumn].color == color) {
                if (isChainAlive(row, nextColumn, color) == 1)
                    return 1;
            }
        }

        return 0;
    }

    /**
     * Sprawdza całą planszę i usuwa martwe łańcuchy.
     */
    public void checkBoard() {
        // GRASP: Information Expert
        // Board sprawdza stan całej planszy i decyduje,
        // które pionki są martwe (breaths == 0)czytl
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                if (positions[i][j].breaths == 0) {
                    checkChains(i, j);
                }

            }
        }


    }

    /**
     * Sprawdza czy nastąpiła sytuacja Ko.
     *
     * @return true jeśli Ko, false w przeciwnym razie
     */
    public boolean isKo() {
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                if (positions[i][j].color != lastPositions[i][j])
                    return false;
            }

        }
        return true;
    }

    /**
     * Sprawdza czy ruch jest legalny.
     *
     * @param row wiersz
     * @param column kolumna
     * @param turn true dla czarnego, false dla białego
     * @return true jeśli ruch legalny
     */
    public boolean checkMove(int row, int column, boolean turn) //true = black
    {
        //w tej funkcji zrobie troche dirty ruch, jak wiem ze miejsce jest jak do samobojstwa ale wiem ze zabije inny lancuch to dam sobie oddechy :)
        if (positions[row][column].color != 0)
            return false;


        //sprawdze czy jak postawie to najpierw czy zabijam siebie, potem czy pionek obok,, ismovecorrect moze robic move od teraz
        int color;
        if (turn) {
            positions[row][column].color = 2;
            color = 2;
        } else {
            positions[row][column].color = 1;
            color = 1;
        }
        int enemyColor = 3 - color;
        updateBreaths();

        //sprawdzam ko bo latwo

        if (isKo())
            return false;


        visited = new boolean[19][19];
        if (isChainAlive(row, column, positions[row][column].color) == 1) {
            updateBreaths();
            return true;
        }//sprawdzilem czy zabijam siebie, jak nie to git i robie ruch. teraz sprawdze czy zabilem jakis obok
        else {
            if (row > 0) {
                visited = new boolean[19][19];
                if (positions[row - 1][column].color != color && isChainAlive(row - 1, column, enemyColor) == 0) {
                    updateBreaths();
                    positions[row][column].breaths = 4;
                    return true;
                }
            }
            if (row < 18) {
                visited = new boolean[19][19];
                if (positions[row + 1][column].color != color && isChainAlive(row + 1, column, enemyColor) == 0) {
                    updateBreaths();
                    positions[row][column].breaths = 4;
                    return true;
                }
            }
            if (column > 0) {
                visited = new boolean[19][19];
                if (positions[row][column - 1].color != color && isChainAlive(row, column - 1, enemyColor) == 0) {
                    updateBreaths();
                    positions[row][column].breaths = 4;
                    return true;
                }
            }
            if (column < 18) {
                visited = new boolean[19][19];
                if (positions[row][column + 1].color != color && isChainAlive(row, column + 1, enemyColor) == 0) {
                    updateBreaths();
                    positions[row][column].breaths = 4;
                    return true;
                }
            }
        }//sprawdzilem wszsytkich sasiadow pola, jak jakiegos z nich zabijam to zrobilem sobie miejsce i jest okej, a jak nie to false

        {
            positions[row][column].color = 0;
            updateBreaths();
            return false;
        }
    }

    /**
     * Wyświetla planszę w konsoli.
     */
    public void show() {
        // GRASP: Pure Fabrication
        // Metoda do prezentacji stanu planszy w konsoli,
        // nie jest częścią logiki domenowej GO

        int row = 1;
        System.out.println("   A B C D E F G H I J K L M N O P Q R S");
        for (int i = 0; i < 19; i++) {
            System.out.print(row);
            if (row < 10) {
                System.out.print(" ");
            }
            System.out.print(" ");
            row++;
            for (int j = 0; j < 19; j++) {
                if (positions[i][j].color == 0)
                    System.out.print("+");
                if (positions[i][j].color == 1)
                    System.out.print("0");
                if (positions[i][j].color == 2)
                    System.out.print("X");
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    /**
     * Sprawdza rekurencyjnie czy pole należy do terytorium białego.
     *
     * @param row wiersz
     * @param col kolumna
     * @return true jeśli białego terytorium
     */
    private boolean checkWhiteTerritoryRecursive(int row, int col) {
        // Jeśli pole jest poza planszą git
        if (row < 0 || row >= 19 || col < 0 || col >= 19) {
            return true;
        }

        // Jeśli już odwiedziliśmy to pole, zwracamy true
        if (whiteVisited[row][col] == 1) {
            return true;
        }


        int color = positions[row][col].color;

        if (color == 1) {
            return true;
        }
        if (color == 2) {
            return false;
        }
        whiteVisited[row][col] = 1;

        boolean up = checkWhiteTerritoryRecursive(row - 1, col);
        boolean down = checkWhiteTerritoryRecursive(row + 1, col);
        boolean left = checkWhiteTerritoryRecursive(row, col - 1);
        boolean right = checkWhiteTerritoryRecursive(row, col + 1);

        return up && down && left && right;
    }

    /**
     * Sprawdza rekurencyjnie czy pole należy do terytorium czarnego.
     *
     * @param row wiersz
     * @param col kolumna
     * @return true jeśli czarnego terytorium
     */
    private boolean checkBlackTerritoryRecursive(int row, int col) {
        // Jeśli pole jest poza planszą git
        if (row < 0 || row >= 19 || col < 0 || col >= 19) {
            return true;
        }

        // Jeśli już odwiedziliśmy to pole, zwracamy true
        if (blackVisited[row][col] == 1) {
            return true;
        }


        int color = positions[row][col].color;


        if (color == 2) {
            return true;
        }

        if (color == 1) {
            return false;
        }
        blackVisited[row][col] = 1;
        boolean up = checkBlackTerritoryRecursive(row - 1, col);
        boolean down = checkBlackTerritoryRecursive(row + 1, col);
        boolean left = checkBlackTerritoryRecursive(row, col - 1);
        boolean right = checkBlackTerritoryRecursive(row, col + 1);


        return up && down && left && right;
    }

    /**
     * Oblicza terytoria obu graczy na planszy.
     */
    public void calculateTerritories() {
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                if (positions[i][j].color == 0 && territoryCache[i][j] == 0) {
                    for (int x = 0; x < 19; x++) {
                        for (int y = 0; y < 19; y++) {
                            whiteVisited[x][y] = 0;
                        }
                    }


                    boolean isWhite = checkWhiteTerritoryRecursive(i, j);


                    for (int x = 0; x < 19; x++) {
                        for (int y = 0; y < 19; y++) {
                            blackVisited[x][y] = 0;
                        }
                    }


                    boolean isBlack = checkBlackTerritoryRecursive(i, j);

                    // Ustal wynik
                    int result;
                    if (isWhite && !isBlack) {
                        result = 1;
                    } else if (!isWhite && isBlack) {
                        result = 2;
                    } else {
                        result = 3;
                    }


                    for (int x = 0; x < 19; x++) {
                        for (int y = 0; y < 19; y++) {
                            if ((whiteVisited[x][y] == 1 || blackVisited[x][y] == 1) &&
                                    positions[x][y].color == 0) {
                                territoryCache[x][y] = result;
                            }
                        }
                    }
                }
            }
        }
    }














}














































