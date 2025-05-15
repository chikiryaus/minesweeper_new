package minesweeper.model;

import minesweeper.model.navigation.CellPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MineFieldTest {

    private MineField field;
    private final int ROWS = 5;
    private final int COLS = 5;
    private final int MINES = 5;

    @BeforeEach
    void setUp() {
        // Убедимся, что глобальные диапазоны для CellPosition установлены перед каждым тестом
        // Это важно, т.к. MineField их устанавливает в конструкторе,
        // а тесты могут выполняться в разном порядке или параллельно.
        CellPosition.setVerticalRange(0, ROWS - 1);
        CellPosition.setHorizontalRange(0, COLS - 1);
        field = new MineField(ROWS, COLS, MINES);
    }

    @Test
    @DisplayName("Конструктор создает поле правильных размеров и с корректным начальным количеством мин")
    void constructor_initializesFieldCorrectly() {
        assertEquals(ROWS, field.getRows());
        assertEquals(COLS, field.getColumns());
        assertEquals(MINES, field.getMineCount(), "Начальное количество мин в MineField");

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                assertNotNull(field.getCell(r, c), "Ячейка (" + r + "," + c + ") не должна быть null");
                assertFalse(field.getCell(r,c).isMine(), "Изначально ячейки не должны быть минами до placeMines");
                assertFalse(field.getCell(r,c).isOpen(), "Изначально ячейки должны быть закрыты");
            }
        }
    }

    @Test
    @DisplayName("placeMinesOnNewField размещает правильное количество мин")
    void placeMinesOnNewField_placesCorrectNumberOfMines() {
        field.placeMinesOnNewField(); // Размещаем мины

        int actualMineCount = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (field.getCell(r, c).isMine()) {
                    actualMineCount++;
                }
            }
        }
        assertEquals(MINES, actualMineCount, "Фактическое количество размещенных мин должно совпадать");
        assertEquals(MINES, field.getMineCount(), "Внутренний счетчик мин MineField должен быть корректен");
    }

    @Test
    @DisplayName("calculateAllAdjacentMines корректно считает соседей для известной конфигурации")
    void calculateAllAdjacentMines_calculatesCorrectly() {
        // Создаем поле 3x3 для простоты
        MineField smallField = new MineField(3, 3, 2);
        // Вручную расставляем мины
        smallField.getCell(0, 0).setMine(true);
        smallField.getCell(1, 1).setMine(true);
        // Счетчик мин в smallField нужно установить вручную, т.к. мы не вызывали placeMinesOnNewField
        // Для теста calculateAllAdjacentMines это не так важно, т.к. он смотрит на isMine()

        smallField.calculateAllAdjacentMines();

        // Ожидаемые значения для не-мин
        // (0,2) соседи: (0,1)нет (1,1)да (1,2)нет. => 1.
        assertEquals(2, smallField.getCell(1, 0).getAdjacentMinesCount()); // (0,0) и (1,1) - да.
        assertEquals(1, smallField.getCell(1, 2).getAdjacentMinesCount()); // (1,1) - да.
        assertEquals(1, smallField.getCell(2, 0).getAdjacentMinesCount()); // (1,0)нет (1,1)да.
        assertEquals(1, smallField.getCell(2, 1).getAdjacentMinesCount()); // (1,1)да (2,0)нет (2,2)нет.
        assertEquals(1, smallField.getCell(2, 2).getAdjacentMinesCount()); // (1,1)да (1,2)нет (2,1)нет.

        // Для мин значение должно быть -1 (согласно логике calculateAllAdjacentMines)
        assertEquals(-1, smallField.getCell(0, 0).getAdjacentMinesCount());
        assertEquals(-1, smallField.getCell(1, 1).getAdjacentMinesCount());

        // Перепроверим соседей для (0,2):
        // (0,1) - не мина. getCell(0,1).setAdjacentMinesCount(2)
        // (1,1) - мина.
        // (1,2) - не мина. getCell(1,2).setAdjacentMinesCount(1)
        // Ожидаемое для (0,2) - 1.
        assertEquals(1, smallField.getCell(0,2).getAdjacentMinesCount());

    }

    @Test
    @DisplayName("recalculateAdjacentMinesAround корректно пересчитывает соседей")
    void recalculateAdjacentMinesAround_recalculatesCorrectly() {
        MineField field3x3 = new MineField(3, 3, 1);
        field3x3.getCell(0,0).setMine(true); // Ставим мину
        field3x3.calculateAllAdjacentMines(); // Начальный подсчет

        assertEquals(1, field3x3.getCell(0,1).getAdjacentMinesCount());
        assertEquals(1, field3x3.getCell(1,0).getAdjacentMinesCount());
        assertEquals(1, field3x3.getCell(1,1).getAdjacentMinesCount());

        field3x3.getCell(0,0).setMine(false); // Убираем мину
        field3x3.recalculateAdjacentMinesAround(new CellPosition(0,0)); // Пересчитываем вокруг старой позиции мины

        assertEquals(0, field3x3.getCell(0,0).getAdjacentMinesCount()); // Сама ячейка
        assertEquals(0, field3x3.getCell(0,1).getAdjacentMinesCount());
        assertEquals(0, field3x3.getCell(1,0).getAdjacentMinesCount());
        assertEquals(0, field3x3.getCell(1,1).getAdjacentMinesCount());
    }


    @ParameterizedTest
    @CsvSource({
            "0,0,true", "2,2,true", // Валидные
            "-1,0,false", "0,-1,false", // Невалидные < 0
            "5,2,false", "2,5,false"  // Невалидные >= размеру (для поля 5x5)
    })
    @DisplayName("isValidPosition проверяет валидность позиции")
    void isValidPosition_checksCorrectly(int r, int c, boolean expected) {
        // CellPosition.isValid() статичен и не зависит от экземпляра MineField,
        // но мы используем isValidPosition из MineField, который его вызывает.
        // Диапазоны уже установлены в setUp().
        assertEquals(expected, field.isValidPosition(new CellPosition(r, c)));
    }

    @Test
    @DisplayName("getCell возвращает ячейку для валидной позиции и null для невалидной")
    void getCell_returnsCellOrNull() {
        assertNotNull(field.getCell(new CellPosition(0, 0)));
        assertNull(field.getCell(new CellPosition(ROWS, COLS))); // Невалидная позиция
    }

    @Test
    @DisplayName("incrementMineCount и decrementMineCount изменяют счетчик мин")
    void incrementAndDecrementMineCount() {
        int initialCount = field.getMineCount();
        field.incrementMineCount();
        assertEquals(initialCount + 1, field.getMineCount());
        field.decrementMineCount();
        assertEquals(initialCount, field.getMineCount());
        field.decrementMineCount(); // Уменьшаем еще раз
        assertEquals(Math.max(0, initialCount - 1), field.getMineCount()); // Убедимся, что не уходит в минус если initialCount=0
    }

    @Test
    @DisplayName("openCellRecursive открывает пустую ячейку и ее соседей")
    void openCellRecursive_opensEmptyCellAndNeighbors() {
        MineField field3x3 = new MineField(3, 3, 0); // Поле без мин
        field3x3.calculateAllAdjacentMines(); // Все adjacentMinesCount будут 0

        assertFalse(field3x3.getCell(1, 1).isOpen());
        field3x3.openCellRecursive(new CellPosition(1, 1)); // Открываем центр

        // Все ячейки должны стать открытыми
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                assertTrue(field3x3.getCell(r, c).isOpen(), "Ячейка (" + r + "," + c + ") должна быть открыта");
            }
        }
    }

    @Test
    @DisplayName("openCellRecursive открывает ячейку с числом, но не ее соседей")
    void openCellRecursive_opensNumberedCellNotNeighbors() {
        MineField field3x3 = new MineField(3, 3, 1);
        field3x3.getCell(0,0).setMine(true); // Мина в углу
        field3x3.calculateAllAdjacentMines();

        CellPosition cellToOpen = new CellPosition(1,1); // Эта ячейка будет иметь '1'
        assertEquals(1, field3x3.getCell(cellToOpen).getAdjacentMinesCount());

        field3x3.openCellRecursive(cellToOpen);

        assertTrue(field3x3.getCell(cellToOpen).isOpen(), "Ячейка (1,1) должна быть открыта");
        // Соседи не должны быть открыты (кроме самой (1,1))
        assertFalse(field3x3.getCell(0,1).isOpen(), "Сосед (0,1) не должен быть открыт");
        assertFalse(field3x3.getCell(1,0).isOpen(), "Сосед (1,0) не должен быть открыт");
        assertFalse(field3x3.getCell(2,1).isOpen(), "Сосед (2,1) не должен быть открыт");
        // Мина (0,0) тоже не должна быть открыта этим действием
        assertFalse(field3x3.getCell(0,0).isOpen());
    }

    @Test
    @DisplayName("openCellRecursive возвращает true при открытии мины")
    void openCellRecursive_returnsTrueOnMine() {
        field.getCell(0,0).setMine(true);
        assertTrue(field.openCellRecursive(new CellPosition(0,0)));
        assertTrue(field.getCell(0,0).isOpen()); // Мина должна стать открытой
    }

    @Test
    @DisplayName("openCellRecursive не открывает помеченную флагом ячейку")
    void openCellRecursive_doesNotOpenFlaggedCell() {
        CellPosition pos = new CellPosition(0,0);
        field.getCell(pos).toggleFlag(); // Ставим флаг
        assertTrue(field.getCell(pos).isFlagged());

        assertFalse(field.openCellRecursive(pos)); // Попытка открыть не должна удасться
        assertFalse(field.getCell(pos).isOpen());   // Ячейка должна остаться закрытой
        assertTrue(field.getCell(pos).isFlagged()); // Флаг должен остаться
    }

    @Test
    @DisplayName("getNumberOfOpenedCells считает открытые ячейки")
    void getNumberOfOpenedCells_countsCorrectly() {
        assertEquals(0, field.getNumberOfOpenedCells());
        field.getCell(0,0).setOpen(true);
        field.getCell(0,1).setOpen(true);
        assertEquals(2, field.getNumberOfOpenedCells());
    }

    @Test
    @DisplayName("relocateMine перемещает мину и обновляет соседей")
    void relocateMine_movesMineAndUpdatesAdjacents() {
        MineField field3x3 = new MineField(3, 3, 1);
        CellPosition fromPos = new CellPosition(0,0);
        CellPosition toPos = new CellPosition(2,2);

        field3x3.getCell(fromPos).setMine(true);
        // Устанавливаем mineCount вручную, т.к. placeMines не вызывался
        // field3x3.mineCount = 1; // или через setMineCount, если бы он был
        field3x3.calculateAllAdjacentMines();

        assertEquals(1, field3x3.getCell(0,1).getAdjacentMinesCount());
        assertEquals(0, field3x3.getCell(2,1).getAdjacentMinesCount()); // У toPos (2,2) изначально нет соседей-мин

        assertTrue(field3x3.relocateMine(fromPos, toPos));

        assertFalse(field3x3.getCell(fromPos).isMine(), "Мина должна быть убрана с fromPos");
        assertTrue(field3x3.getCell(toPos).isMine(), "Мина должна быть на toPos");

        // Проверяем соседей
        assertEquals(0, field3x3.getCell(0,1).getAdjacentMinesCount(), "Соседи fromPos должны обновиться");
        assertEquals(0, field3x3.getCell(fromPos).getAdjacentMinesCount(), "fromPos (теперь не мина) должна иметь 0 соседей (если toPos не рядом)");

        assertEquals(1, field3x3.getCell(1,2).getAdjacentMinesCount(), "Соседи toPos должны обновиться");
        assertEquals(1, field3x3.getCell(2,1).getAdjacentMinesCount(), "Соседи toPos должны обновиться");
        assertEquals(-1, field3x3.getCell(toPos).getAdjacentMinesCount(), "toPos (теперь мина) должна иметь -1");
    }

    @Test
    @DisplayName("getActiveMines возвращает только неоткрытые мины")
    void getActiveMines_returnsOnlyUnopenedMines() {
        field.placeMinesOnNewField(); // MINES = 5
        assertEquals(MINES, field.getActiveMines().size());

        // Найдем первую мину и откроем ее
        boolean mineOpened = false;
        for (int r = 0; r < field.getRows(); r++) {
            for (int c = 0; c < field.getColumns(); c++) {
                MineCell cell = field.getCell(r,c);
                if (cell.isMine()) {
                    cell.setOpen(true);
                    mineOpened = true;
                    break;
                }
            }
            if (mineOpened) break;
        }
        assertTrue(mineOpened, "Хотя бы одна мина должна была быть найдена и открыта для теста");
        assertEquals(MINES - 1, field.getActiveMines().size(), "Количество активных мин должно уменьшиться");
    }

    @Test
    @DisplayName("getBoundaryCellsForRelocation находит правильные ячейки")
    void getBoundaryCellsForRelocation_findsCorrectCells() {
        MineField field3x3 = new MineField(3,3,0); // Без мин для простоты
        // Открываем центральную ячейку
        field3x3.getCell(1,1).setOpen(true);
        field3x3.calculateAllAdjacentMines();

        List<CellPosition> boundary = field3x3.getBoundaryCellsForRelocation();
        // Ожидаем 8 соседей центральной ячейки
        assertEquals(8, boundary.size(), "Должно быть 8 ячеек на границе");
        assertTrue(boundary.contains(new CellPosition(0,0)));
        assertTrue(boundary.contains(new CellPosition(0,1)));
        // ... и т.д. для всех 8 соседей (1,1)

        // Если мина на границе, она не должна быть в списке
        field3x3.getCell(0,0).setMine(true);
        boundary = field3x3.getBoundaryCellsForRelocation();
        assertEquals(7, boundary.size(), "Ячейка с миной не должна быть в списке для перемещения");
        assertFalse(boundary.contains(new CellPosition(0,0)));
    }
}