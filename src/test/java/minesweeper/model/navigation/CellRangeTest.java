package minesweeper.model.navigation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CellRangeTest {

    @Test
    @DisplayName("Конструктор с валидными min и max")
    void constructor_validMinMax_createsRange() {
        CellRange range = new CellRange(1, 5);
        assertEquals(1, range.min());
        assertEquals(5, range.max());
    }

    @Test
    @DisplayName("Конструктор с min < 0, min должен стать 0")
    void constructor_minLessThanZero_setsMinToZero() {
        CellRange range = new CellRange(-5, 5);
        assertEquals(0, range.min(), "Min должен быть скорректирован до 0");
        assertEquals(5, range.max());
    }

    @Test
    @DisplayName("Конструктор с max < min, max должен стать равным min")
    void constructor_maxLessThanMin_setsMaxToMin() {
        CellRange range = new CellRange(5, 1);
        assertEquals(5, range.min());
        assertEquals(5, range.max(), "Max должен быть скорректирован до значения Min");
    }

    @Test
    @DisplayName("Конструктор с min < 0 и max < скорректированного min, max должен стать равным скорректированному min (0)")
    void constructor_minNegativeAndMaxLessThanCorrectedMin_setsMaxToCorrectedMin() {
        CellRange range = new CellRange(-5, -10);
        assertEquals(0, range.min(), "Min должен быть скорректирован до 0");
        assertEquals(0, range.max(), "Max должен быть скорректирован до скорректированного Min (0)");
    }

    @Test
    @DisplayName("Конструктор с min = 0 и max < min, max должен стать 0")
    void constructor_minZeroAndMaxLessThanMin_setsMaxToZero() {
        CellRange range = new CellRange(0, -2);
        assertEquals(0, range.min());
        assertEquals(0, range.max(), "Max должен быть скорректирован до 0");
    }

    @Test
    @DisplayName("min() возвращает правильное минимальное значение")
    void min_returnsCorrectValue() {
        CellRange range = new CellRange(2, 8);
        assertEquals(2, range.min());
    }

    @Test
    @DisplayName("max() возвращает правильное максимальное значение")
    void max_returnsCorrectValue() {
        CellRange range = new CellRange(2, 8);
        assertEquals(8, range.max());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0, 1",   // [0..0]
            "0, 2, 3",   // [0..2] -> 0, 1, 2
            "5, 5, 1",   // [5..5]
            "3, 7, 5"    // [3..7] -> 3, 4, 5, 6, 7
    })
    @DisplayName("length() возвращает правильную длину диапазона")
    void length_returnsCorrectLength(int min, int max, int expectedLength) {
        CellRange range = new CellRange(min, max);
        assertEquals(expectedLength, range.length());
    }

    @Test
    @DisplayName("length() для диапазона, скорректированного конструктором")
    void length_afterConstructorCorrection() {
        CellRange range1 = new CellRange(-5, -2); // Станет [0..0]
        assertEquals(1, range1.length());

        CellRange range2 = new CellRange(5, 2); // Станет [5..5]
        assertEquals(1, range2.length());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 5, true",    // Валидный
            "0, 0, true",    // Валидный, min = max
            "5, 10, true",   // Валидный
            "-1, 5, false",  // Невалидный, min < 0
            "5, 1, false",   // Невалидный, max < min
            "-5, -2, false"  // Невалидный, min < 0 и max < min
    })
    @DisplayName("isValidRange (статический) проверяет валидность диапазона")
    void staticIsValidRange_checksRangeCorrectly(int min, int max, boolean expected) {
        assertEquals(expected, CellRange.isValidRange(min, max));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 5, 0, true",   // val = min
            "0, 5, 5, true",   // val = max
            "0, 5, 3, true",   // val внутри диапазона
            "0, 5, -1, false", // val < min
            "0, 5, 6, false",  // val > max
            "3, 3, 3, true",   // Диапазон из одного числа, val = min = max
            "3, 3, 2, false"
    })
    @DisplayName("contains() проверяет, содержится ли значение в диапазоне")
    void contains_checksValueCorrectly(int rMin, int rMax, int val, boolean expected) {
        CellRange range = new CellRange(rMin, rMax);
        assertEquals(expected, range.contains(val));
    }

    @Test
    @DisplayName("toString() возвращает корректное строковое представление")
    void toString_returnsCorrectFormat() {
        CellRange range1 = new CellRange(0, 9);
        assertEquals("CellRange[0..9]", range1.toString());

        CellRange range2 = new CellRange(5, 5);
        assertEquals("CellRange[5..5]", range2.toString());

        CellRange range3 = new CellRange(-2, 7); // Станет [0..7]
        assertEquals("CellRange[0..7]", range3.toString());
    }
}