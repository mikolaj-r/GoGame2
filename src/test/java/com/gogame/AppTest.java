package com.gogame;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy dla klasy App.
 */
class AppTest {

    /**
     * Test walidacji komendy PASS.
     */
    @Test
    void testValidInputPass() {
        assertTrue(App.validInput("PASS"), "PASS powinien być prawidłowy");
    }

    /**
     * Test walidacji komendy SURRENDER.
     */
    @Test
    void testValidInputSurrender() {
        assertTrue(App.validInput("SURRENDER"), "SURRENDER powinien być prawidłowy");
    }

    /**
     * Test prawidłowych współrzędnych.
     */
    @Test
    void testValidInputCorrectCoordinates() {
        assertTrue(App.validInput("A1"), "A1 powinno być prawidłowe");
        assertTrue(App.validInput("S19"), "S19 powinno być prawidłowe");
        assertTrue(App.validInput("J10"), "J10 powinno być prawidłowe");
        assertTrue(App.validInput("A19"), "A19 powinno być prawidłowe");
        assertTrue(App.validInput("S1"), "S1 powinno być prawidłowe");
    }

    /**
     * Test za krótkiego wejścia.
     */
    @Test
    void testValidInputTooShort() {
        assertFalse(App.validInput("A"), "Pojedyncza litera powinna być nieprawidłowa");
        assertFalse(App.validInput("1"), "Pojedyncza cyfra powinna być nieprawidłowa");
        assertFalse(App.validInput(""), "Pusty string powinien być nieprawidłowy");
    }

    /**
     * Test za długiego wejścia.
     */
    @Test
    void testValidInputTooLong() {
        assertFalse(App.validInput("A111"), "Za długie wejście powinno być nieprawidłowe");
        assertFalse(App.validInput("ABCD"), "Za długie wejście powinno być nieprawidłowe");
    }

    /**
     * Test nieprawidłowej kolumny.
     */
    @Test
    void testValidInputInvalidColumn() {
        assertFalse(App.validInput("T1"), "T jest poza zakresem A-S");
        assertFalse(App.validInput("Z10"), "Z jest poza zakresem A-S");
        assertFalse(App.validInput("@1"), "@ nie jest literą");
    }

    /**
     * Test nieprawidłowego wiersza.
     */
    @Test
    void testValidInputInvalidRow() {
        assertFalse(App.validInput("A0"), "Wiersz 0 jest poza zakresem");
        assertFalse(App.validInput("A20"), "Wiersz 20 jest poza zakresem");
        assertFalse(App.validInput("A99"), "Wiersz 99 jest poza zakresem");
        assertFalse(App.validInput("J-5"), "Ujemny wiersz jest nieprawidłowy");
    }

    /**
     * Test nieprawidłowego formatu wiersza.
     */
    @Test
    void testValidInputNonNumericRow() {
        assertFalse(App.validInput("AA"), "Wiersz musi być liczbą");
        assertFalse(App.validInput("AX"), "Wiersz musi być liczbą");
        assertFalse(App.validInput("J#"), "Wiersz musi być liczbą");
    }

    /**
     * Test granicznych wartości kolumn.
     */
    @Test
    void testValidInputBoundaryColumns() {
        assertTrue(App.validInput("A10"), "A jest prawidłową kolumną");
        assertTrue(App.validInput("S10"), "S jest prawidłową kolumną");
        assertFalse(App.validInput("@10"), "@ jest przed A");
        assertFalse(App.validInput("T10"), "T jest po S");
    }

    /**
     * Test granicznych wartości wierszy.
     */
    @Test
    void testValidInputBoundaryRows() {
        assertTrue(App.validInput("J1"), "1 jest prawidłowym wierszem");
        assertTrue(App.validInput("J19"), "19 jest prawidłowym wierszem");
        assertFalse(App.validInput("J0"), "0 jest poza zakresem");
        assertFalse(App.validInput("J20"), "20 jest poza zakresem");
    }

    /**
     * Test wejścia z białymi znakami.
     */
    @Test
    void testValidInputWithWhitespace() {
        // Zakładamy że trim() jest wywoływany przed validInput
        assertFalse(App.validInput(" A10"), "Spacja na początku powinna być nieprawidłowa");
        assertFalse(App.validInput("A10 "), "Spacja na końcu powinna być nieprawidłowa");
        assertFalse(App.validInput("A 10"), "Spacja w środku powinna być nieprawidłowa");
    }

    /**
     * Test specjalnych znaków.
     */
    @Test
    void testValidInputSpecialCharacters() {
        assertFalse(App.validInput("A!1"), "Znak specjalny powinien być nieprawidłowy");
        assertFalse(App.validInput("A$5"), "Znak specjalny powinien być nieprawidłowy");
        assertFalse(App.validInput("#10"), "Znak specjalny powinien być nieprawidłowy");
    }

    /**
     * Test różnych długości wejścia.
     */
    @Test
    void testValidInputLengths() {
        assertTrue(App.validInput("A1"), "Długość 2 powinna być prawidłowa");
        assertTrue(App.validInput("A10"), "Długość 3 powinna być prawidłowa");
        assertFalse(App.validInput("A"), "Długość 1 powinna być nieprawidłowa");
        assertFalse(App.validInput("A100"), "Długość 4 powinna być nieprawidłowa");
    }

    /**
     * Test wszystkich prawidłowych kolumn.
     */
    @Test
    void testValidInputAllColumns() {
        String columns = "ABCDEFGHIJKLMNOPQRS";
        for (char c : columns.toCharArray()) {
            assertTrue(App.validInput(c + "10"),
                    "Kolumna " + c + " powinna być prawidłowa");
        }
    }

    /**
     * Test wszystkich prawidłowych wierszy.
     */
    @Test
    void testValidInputAllRows() {
        for (int i = 1; i <= 19; i++) {
            assertTrue(App.validInput("J" + i),
                    "Wiersz " + i + " powinien być prawidłowy");
        }
    }

    /**
     * Test null input.
     */
    @Test
    void testValidInputNull() {
        assertThrows(NullPointerException.class, () -> {
            App.validInput(null);
        }, "Null powinien rzucić wyjątek");
    }

    /**
     * Test pojedynczych cyfr jako wierszy.
     */
    @Test
    void testValidInputSingleDigitRows() {
        for (int i = 1; i <= 9; i++) {
            assertTrue(App.validInput("A" + i),
                    "Pojedyncza cyfra " + i + " powinna być prawidłowa");
        }
    }

    /**
     * Test dwucyfrowych wierszy.
     */
    @Test
    void testValidInputDoubleDigitRows() {
        for (int i = 10; i <= 19; i++) {
            assertTrue(App.validInput("A" + i),
                    "Dwucyfrowy wiersz " + i + " powinien być prawidłowy");
        }
    }
}