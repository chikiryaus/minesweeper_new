package minesweeper.model.navigation;

import java.util.Objects;

/**
 * Представляет позицию ячейки на игровом поле, определяемую строкой и столбцом.
 * Использует 0-индексацию для строк и столбцов.
 * Этот класс также управляет глобальными допустимыми диапазонами для всех позиций ячеек.
 */
public class CellPosition {
    private static CellRange _horizontalRange = new CellRange(0, 0);
    private static CellRange _verticalRange = new CellRange(0, 0);

    /**
     * Устанавливает глобальный допустимый горизонтальный диапазон (столбцы) для всех позиций ячеек.
     * Диапазон будет установлен, только если он валиден (min <= max).
     *
     * @param min Минимальный допустимый индекс столбца (включительно).
     * @param max Максимальный допустимый индекс столбца (включительно).
     */
    public static void setHorizontalRange(int min, int max) {
        if (CellRange.isValidRange(min, max)) {
            _horizontalRange = new CellRange(min, max);
        }
    }

    /**
     * Возвращает текущий установленный глобальный горизонтальный диапазон.
     *
     * @return Объект {@link CellRange}, представляющий горизонтальный диапазон.
     */
    public static CellRange horizontalRange() {
        return _horizontalRange;
    }

    /**
     * Устанавливает глобальный допустимый вертикальный диапазон (строки) для всех позиций ячеек.
     * Диапазон будет установлен, только если он валиден (min <= max).
     *
     * @param min Минимальный допустимый индекс строки (включительно).
     * @param max Максимальный допустимый индекс строки (включительно).
     */
    public static void setVerticalRange(int min, int max) {
        if (CellRange.isValidRange(min, max)) {
            _verticalRange = new CellRange(min, max);
        }
    }

    /**
     * Возвращает текущий установленный глобальный вертикальный диапазон.
     *
     * @return Объект {@link CellRange}, представляющий вертикальный диапазон.
     */
    public static CellRange verticalRange() {
        return _verticalRange;
    }

    /**
     * Индекс строки ячейки (0-индексация).
     */
    private final int _row;
    /**
     * Индекс столбца ячейки (0-индексация).
     */
    private final int _column;

    /**
     * Создает новый объект {@code CellPosition} с указанными координатами строки и столбца.
     * Примечание: конструктор не выполняет проверку нахождения позиции в установленных
     * {@link #setHorizontalRange(int, int)} и {@link #setVerticalRange(int, int)} диапазонах.
     * Используйте метод {@link #isValid()} или {@link #isValid(int, int)} для такой проверки.
     *
     * @param row Индекс строки (0-индексация).
     * @param col Индекс столбца (0-индексация).
     */
    public CellPosition(int row, int col) {
        // Проверка при создании не обязательна, если мы доверяем, что диапазоны установлены,
        // но может быть полезна для отладки.
        // if (!isValid(row, col)) {
        //     throw new IllegalArgumentException("Ошибка: позиция (" + row + " , " + col + ") находится вне границ " +
        //             "допустимого диапазона. Горизонтальный: " + _horizontalRange + ", Вертикальный: " + _verticalRange);
        // }
        _row = row;
        _column = col;
    }

    /**
     * Возвращает индекс строки данной позиции ячейки.
     *
     * @return Индекс строки (0-индексация).
     */
    public int getRow() {
        return _row;
    }

    /**
     * Возвращает индекс столбца данной позиции ячейки.
     *
     * @return Индекс столбца (0-индексация).
     */
    public int getColumn() {
        return _column;
    }

    /**
     * Проверяет, находится ли данная позиция ячейки в пределах глобально установленных
     * горизонтального и вертикального диапазонов.
     *
     * @return {@code true}, если позиция валидна (находится в диапазонах), иначе {@code false}.
     * @see #setHorizontalRange(int, int)
     * @see #setVerticalRange(int, int)
     */
    public boolean isValid() {
        return isValid(_row, _column);
    }

    /**
     * Статический метод для проверки, находятся ли указанные координаты строки и столбца
     * в пределах глобально установленных горизонтального и вертикального диапазонов.
     *
     * @param row Индекс строки для проверки.
     * @param col Индекс столбца для проверки.
     * @return {@code true}, если указанные координаты валидны, иначе {@code false}.
     * @see #setHorizontalRange(int, int)
     * @see #setVerticalRange(int, int)
     */
    public static boolean isValid(int row, int col) {
        return _verticalRange.contains(row) && _horizontalRange.contains(col);
    }

    /**
     * Сравнивает данную позицию ячейки с другим объектом на равенство.
     * Две позиции ячеек считаются равными, если их индексы строк и столбцов совпадают.
     *
     * @param o Объект для сравнения с данной позицией ячейки.
     * @return {@code true}, если указанный объект является {@code CellPosition}
     *         с такими же координатами строки и столбца, иначе {@code false}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CellPosition that = (CellPosition) o;
        return _row == that._row && _column == that._column;
    }

    /**
     * Возвращает хеш-код для данной позиции ячейки.
     * Хеш-код вычисляется на основе индексов строки и столбца.
     *
     * @return Хеш-код для этой позиции ячейки.
     */
    @Override
    public int hashCode() {
        return Objects.hash(_row, _column);
    }

    /**
     * Возвращает строковое представление данной позиции ячейки.
     * Формат строки: "CellPosition(row, column)".
     *
     * @return Строковое представление позиции ячейки.
     */
    @Override
    public String toString() {
        return "CellPosition(" + _row + ", " + _column + ")";
    }

    // Метод для получения соседних позиций, если понадобится Direction
    // public CellPosition getNeighbor(Direction direction) {
    //     // Логика смещения на основе направления
    // }
}