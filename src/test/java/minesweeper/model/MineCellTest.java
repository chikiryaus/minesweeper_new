package minesweeper.model;

import minesweeper.model.navigation.CellPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName; // Для более читаемых названий тестов

import static org.junit.jupiter.api.Assertions.*; // Статический импорт для ассертов

/**
 * Тесты для класса {@link MineCell}.
 */
class MineCellTest {

    private MineCell cell;
    private CellPosition testPosition;

    @BeforeEach
    void setUp() {
        // Этот метод будет вызываться перед каждым тестовым методом.
        // Создаем свежую ячейку для каждого теста.
        testPosition = new CellPosition(1, 1); // Пример позиции
        cell = new MineCell(testPosition);
    }

    @Test
    @DisplayName("Новая ячейка должна быть закрыта, не миной, не флагом и с 0 соседей")
    void newCellDefaultState() {
        assertNotNull(cell.getPosition(), "Позиция не должна быть null");
        assertEquals(testPosition, cell.getPosition(), "Позиция должна совпадать с заданной");
        assertFalse(cell.isMine(), "Новая ячейка не должна быть миной");
        assertFalse(cell.isOpen(), "Новая ячейка должна быть закрыта");
        assertFalse(cell.isFlagged(), "Новая ячейка не должна быть помечена флагом");
        assertEquals(0, cell.getAdjacentMinesCount(), "Количество соседей должно быть 0");
    }

    @Test
    @DisplayName("Установка и проверка мины")
    void setAndCheckMine() {
        cell.setMine(true);
        assertTrue(cell.isMine(), "Ячейка должна стать миной");
        cell.setMine(false);
        assertFalse(cell.isMine(), "Мина должна быть убрана");
    }

    @Test
    @DisplayName("Открытие ячейки")
    void setOpen() {
        cell.setOpen(true);
        assertTrue(cell.isOpen(), "Ячейка должна стать открытой");
    }

    @Test
    @DisplayName("Открытие ячейки снимает флаг")
    void setOpenRemovesFlag() {
        cell.setFlagged(true); // Устанавливаем флаг на закрытую ячейку
        assertTrue(cell.isFlagged(), "Флаг должен быть установлен перед открытием");

        cell.setOpen(true);
        assertTrue(cell.isOpen(), "Ячейка должна стать открытой");
        assertFalse(cell.isFlagged(), "Флаг должен быть снят после открытия");
    }

    @Test
    @DisplayName("Переключение флага на закрытой ячейке")
    void toggleFlagOnClosedCell() {
        assertFalse(cell.isFlagged(), "Изначально флаг не должен быть установлен");

        cell.toggleFlag(); // Ставим флаг
        assertTrue(cell.isFlagged(), "Флаг должен быть установлен после первого переключения");

        cell.toggleFlag(); // Снимаем флаг
        assertFalse(cell.isFlagged(), "Флаг должен быть снят после второго переключения");
    }

    @Test
    @DisplayName("Переключение флага на открытой ячейке не должно менять состояние флага")
    void toggleFlagOnOpenCell() {
        cell.setOpen(true);
        assertFalse(cell.isFlagged(), "Флаг не должен быть установлен на открытой ячейке");

        cell.toggleFlag();
        assertFalse(cell.isFlagged(), "Переключение флага на открытой ячейке не должно его установить");
    }

    @Test
    @DisplayName("Установка флага (setFlagged) на закрытой ячейке")
    void setFlaggedTrueOnClosedCell() {
        cell.setFlagged(true);
        assertTrue(cell.isFlagged(), "Флаг должен быть установлен");
    }

    @Test
    @DisplayName("Снятие флага (setFlagged) с закрытой ячейки")
    void setFlaggedFalseOnClosedCell() {
        cell.setFlagged(true); // Сначала ставим
        cell.setFlagged(false); // Потом снимаем
        assertFalse(cell.isFlagged(), "Флаг должен быть снят");
    }

    @Test
    @DisplayName("Попытка установить флаг (setFlagged true) на открытой ячейке не должна работать")
    void setFlaggedTrueOnOpenCell() {
        cell.setOpen(true);
        cell.setFlagged(true);
        assertFalse(cell.isFlagged(), "Флаг не должен устанавливаться на открытую ячейку");
    }


    @Test
    @DisplayName("Установка и получение количества соседних мин")
    void setAndGetAdjacentMinesCount() {
        cell.setAdjacentMinesCount(5);
        assertEquals(5, cell.getAdjacentMinesCount(), "Количество соседей должно быть 5");
    }

    @Test
    @DisplayName("Сброс состояния ячейки (reset)")
    void resetCell() {
        cell.setMine(true);
        cell.setOpen(true); // Это также снимет флаг
        cell.setFlagged(true); // Пытаемся поставить флаг (не получится на открытой, но проверим reset)
        cell.setAdjacentMinesCount(3);

        cell.reset();

        assertFalse(cell.isMine(), "После сброса не должно быть мины");
        assertFalse(cell.isOpen(), "После сброса ячейка должна быть закрыта");
        assertFalse(cell.isFlagged(), "После сброса не должно быть флага");
        assertEquals(0, cell.getAdjacentMinesCount(), "После сброса количество соседей должно быть 0");
    }

    @Test
    @DisplayName("Проверка toString для закрытой ячейки")
    void toStringClosed() {
        assertEquals("#", cell.toString());
    }

    @Test
    @DisplayName("Проверка toString для ячейки с флагом")
    void toStringFlagged() {
        cell.toggleFlag();
        assertEquals("F", cell.toString());
    }

    @Test
    @DisplayName("Проверка toString для открытой мины")
    void toStringOpenMine() {
        cell.setMine(true);
        cell.setOpen(true);
        assertEquals("*", cell.toString());
    }

    @Test
    @DisplayName("Проверка toString для открытой пустой ячейки")
    void toStringOpenEmpty() {
        cell.setOpen(true);
        cell.setAdjacentMinesCount(0);
        assertEquals(" ", cell.toString());
    }

    @Test
    @DisplayName("Проверка toString для открытой ячейки с числом")
    void toStringOpenWithNumber() {
        cell.setOpen(true);
        cell.setAdjacentMinesCount(3);
        assertEquals("3", cell.toString());
    }
}