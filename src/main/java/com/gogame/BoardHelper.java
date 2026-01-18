package com.gogame;

import java.util.ArrayList;
import java.util.List;

//Metody pomocnicze oraz te skopiowane z klasy App aby można je było zaimplementowac
//w architekturze klient-serwer

public class BoardHelper {

    public static void makeMove(Board board, int row, int col, boolean isBlack) {
        int color = isBlack ? 2 : 1;
        board.positions[row][col].color = color;
        board.updateBreaths();
    }

    public static void copyBoardState(Board destination, Board source) {
        int stonesCount = 0; // Debug
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                destination.positions[i][j].color = source.positions[i][j].color;
                destination.positions[i][j].breaths = source.positions[i][j].breaths;
                destination.lastPositions[i][j] = source.lastPositions[i][j];

                if (source.positions[i][j].color != 0) stonesCount++;
            }
        }
        System.out.println("DEBUG: Board copied. Stones number: " + stonesCount);
    }

    public static int getColor(Board board, int row, int col) {
        if (row < 0 || row >= 19 || col < 0 || col >= 19) {
            return -1;
        }
        return board.positions[row][col].color;
    }

    public static int getTerritoryOwner(Board board, int row, int col) {
        return board.territoryCache[row][col];
    }

    public static List<int[]> getChain(Board board, int row, int col) {
        List<int[]> chain = new ArrayList<>();
        int targetColor = board.positions[row][col].color;
        if (targetColor == 0) return chain;

        boolean[][] visited = new boolean[19][19];
        findChainRecursive(board, row, col, targetColor, visited, chain);
        return chain;
    }

    private static void findChainRecursive(Board board, int r, int c, int targetColor, boolean[][] visited, List<int[]> chain) {
        if (r < 0 || r >= 19 || c < 0 || c >= 19) return;
        if (visited[r][c]) return;
        if (board.positions[r][c].color != targetColor) return;

        visited[r][c] = true;
        chain.add(new int[]{r, c});

        findChainRecursive(board, r + 1, c, targetColor, visited, chain);
        findChainRecursive(board, r - 1, c, targetColor, visited, chain);
        findChainRecursive(board, r, c + 1, targetColor, visited, chain);
        findChainRecursive(board, r, c - 1, targetColor, visited, chain);
    }
}