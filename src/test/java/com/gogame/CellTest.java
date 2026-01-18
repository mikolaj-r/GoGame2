package com.gogame;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy dla klasy Cell.
 */
class CellTest {

    /**
     * Test tworzenia pustej komórki.
     */
    @Test
    void testCellCreation() {
        Cell cell = new Cell();
        assertEquals(0, cell.color, "Nowa komórka powinna być pusta");
        assertEquals(0, cell.breaths, "Nowa komórka powinna mieć 0 oddechów");
    }

    /**
     * Test ustawiania koloru.
     */
    @Test
    void testSetColor() {
        Cell cell = new Cell();
        cell.color = 1;
        assertEquals(1, cell.color, "Kolor powinien być 1 (biały)");

        cell.color = 2;
        assertEquals(2, cell.color, "Kolor powinien być 2 (czarny)");
    }

    /**
     * Test ustawiania oddechów.
     */
    @Test
    void testSetBreaths() {
        Cell cell = new Cell();
        cell.breaths = 3;
        assertEquals(3, cell.breaths, "Oddechy powinny wynosić 3");
    }
}