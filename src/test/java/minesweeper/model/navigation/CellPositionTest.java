package minesweeper.model.navigation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CellPositionTest {

    // Сохраним начальные диапазоны, чтобы восстанавливать их после тестов,
    // так как они статические и влияют на все тесты.
    private static CellRange originalHorizontalRange;
    private static CellRange originalVerticalRange;

    @BeforeEach
    void setUpGlobalRanges() {
        // Сохраняем текущие глобальные диапазоны перед каждым тестом
        originalHorizontalRange = CellPosition.horizontalRange();
        originalVerticalRange = CellPosition.verticalRange();

        // Устанавливаем известные диапазоны для большинства тестов
        CellPosition.setHorizontalRange(0, 9); // Например, поле 10x10
        CellPosition.setVerticalRange(0, 9);
    }

    @AfterEach
    void restoreGlobalRanges() {
        // Восстанавливаем исходные глобальные диапазоны после каждого теста
        // чтобы тесты не влияли друг на друга через статические поля.
        CellPosition.setHorizontalRange(originalHorizontalRange.min(), originalHorizontalRange.max());
        CellPosition.setVerticalRange(originalVerticalRange.min(), originalVerticalRange.max());
    }

    @Test
    @DisplayName("Конструктор создает CellPosition с правильными row и column")
    void constructor_setsRowAndColumnCorrectly() {
        CellPosition pos = new CellPosition(3, 5);
        assertEquals(3, pos.getRow());
        assertEquals(5, pos.getColumn());
    }

    @Test
    @DisplayName("getRow и getColumn возвращают правильные значения")
    void getters_returnCorrectValues() {
        int r = 7, c = 2;
        CellPosition pos = new CellPosition(r, c);
        assertEquals(r, pos.getRow());
        assertEquals(c, pos.getColumn());
    }

    @Test
    @DisplayName("setHorizontalRange устанавливает горизонтальный диапазон")
    void setHorizontalRange_updatesRange() {
        CellPosition.setHorizontalRange(1, 5);
        assertEquals(1, CellPosition.horizontalRange().min());
        assertEquals(5, CellPosition.horizontalRange().max());
    }

    @Test
    @DisplayName("setHorizontalRange не меняет диапазон при невалидных min/max")
    void setHorizontalRange_invalidRange_doesNotUpdate() {
        CellPosition.setHorizontalRange(0, 9); // Начальный
        CellRange initialRange = CellPosition.horizontalRange();

        CellPosition.setHorizontalRange(5, 1); // Невалидный (max < min)
        assertEquals(initialRange.min(), CellPosition.horizontalRange().min(), "Диапазон не должен был измениться");
        assertEquals(initialRange.max(), CellPosition.horizontalRange().max(), "Диапазон не должен был измениться");

        // CellRange сам обрабатывает min < 0, устанавливая его в 0.
        // CellRange.isValidRange проверяет min >= 0 && max >= min.
        CellPosition.setHorizontalRange(-1, 5); // Невалидный по CellRange.isValidRange
        // Проверяем, что не изменился относительно состояния *после* предыдущей попытки (т.е. 0,9)
        assertEquals(initialRange.min(), CellPosition.horizontalRange().min());
        assertEquals(initialRange.max(), CellPosition.horizontalRange().max());
    }

    @Test
    @DisplayName("setVerticalRange устанавливает вертикальный диапазон")
    void setVerticalRange_updatesRange() {
        CellPosition.setVerticalRange(2, 7);
        assertEquals(2, CellPosition.verticalRange().min());
        assertEquals(7, CellPosition.verticalRange().max());
    }

    @ParameterizedTest
    @CsvSource({
            "0,0,true",   // Внутри диапазона 0-9, 0-9
            "9,9,true",   // Граница диапазона
            "5,5,true",   // Середина
            "0,10,false", // Column за пределами
            "10,0,false", // Row за пределами
            "-1,5,false", // Row < 0
            "5,-1,false"  // Column < 0
    })
    @DisplayName("isValid (статический) проверяет, находится ли позиция в установленных диапазонах")
    void staticIsValid_checksPositionAgainstRanges(int r, int c, boolean expected) {
        assertEquals(expected, CellPosition.isValid(r, c));
    }

    @Test
    @DisplayName("isValid (метод экземпляра) проверяет валидность текущей позиции")
    void instanceIsValid_checksOwnPosition() {
        CellPosition validPos = new CellPosition(1, 1);
        assertTrue(validPos.isValid());

        CellPosition.setHorizontalRange(0,0); // Сужаем диапазон
        CellPosition.setVerticalRange(0,0);

        CellPosition nowInvalidPos = new CellPosition(1,1); // Эта позиция теперь невалидна
        assertFalse(nowInvalidPos.isValid());

        CellPosition stillValidPos = new CellPosition(0,0);
        assertTrue(stillValidPos.isValid());
    }

    @Test
    @DisplayName("equals должен возвращать true для одинаковых позиций и false для разных")
    void equals_comparesPositionsCorrectly() {
        CellPosition pos1 = new CellPosition(2, 3);
        CellPosition pos2 = new CellPosition(2, 3);
        CellPosition pos3 = new CellPosition(3, 2);
        CellPosition pos4 = new CellPosition(2, 4);

        assertEquals(pos1, pos2, "Позиции с одинаковыми (r,c) должны быть равны");
        assertNotEquals(pos1, pos3, "Позиции с разными (r,c) не должны быть равны");
        assertNotEquals(pos1, pos4, "Позиции с разными (r,c) не должны быть равны");
        assertNotEquals(pos1, null, "Сравнение с null должно возвращать false");
        assertNotEquals(pos1, new Object(), "Сравнение с объектом другого типа должно возвращать false");
        assertEquals(pos1,pos1,"Объект должен быть равен самому себе");
    }

    @Test
    @DisplayName("hashCode должен быть одинаковым для равных объектов")
    void hashCode_isConsistentForEqualObjects() {
        CellPosition pos1 = new CellPosition(5, 8);
        CellPosition pos2 = new CellPosition(5, 8);
        assertEquals(pos1.hashCode(), pos2.hashCode(), "Хеш-коды равных объектов должны совпадать");
    }

    @Test
    @DisplayName("hashCode обычно различается для не равных объектов")
    void hashCode_usuallyDiffersForUnequalObjects() {
        CellPosition pos1 = new CellPosition(1, 2);
        CellPosition pos3 = new CellPosition(2, 1);
        // Это не строгое требование для hashCode, но обычно выполняется
        assertNotEquals(pos1.hashCode(), pos3.hashCode(), "Хеш-коды разных объектов обычно различаются");
    }


    @Test
    @DisplayName("toString возвращает корректное строковое представление")
    void toString_returnsCorrectFormat() {
        CellPosition pos = new CellPosition(4, 6);
        assertEquals("CellPosition(4, 6)", pos.toString());
    }
}