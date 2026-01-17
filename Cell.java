package com.gogame;

public class Cell {
    public int color;      // 0 = puste, 1 = bialy, 2 = czarny
    public int breaths;

    public Cell() {
        this.breaths = 0;
        this.color = 0;
    }
}