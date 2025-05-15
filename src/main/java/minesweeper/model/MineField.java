package minesweeper.model;

import minesweeper.model.navigation.CellPosition;
// import minesweeper.model.navigation.CellRange; // Не используется напрямую, но CellPosition его использует

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Представляет игровое поле "Сапёра", содержащее сетку ячеек ({@link MineCell}).
 * Отвечает за инициализацию поля, размещение мин, подсчет соседних мин,
 * а также за операции над ячейками, такие как открытие или перемещение мины.
 */
public class MineField {
    /**
     * Количество строк на игровом поле.
     */
    private final int rows;
    /**
     * Количество столбцов на игровом поле.
     */
    private final int columns;
    /**
     * Текущее количество мин на поле. Это значение может изменяться,
     * если, например, "диверсант" добавляет или убирает мины.
     */
    private int mineCount;
    /**
     * Двумерный массив, представляющий сетку ячеек на поле.
     */
    private final MineCell[][] cells;
    /**
     * Генератор случайных чисел для размещения мин и других случайных событий.
     */
    private final Random random = new Random();

    /**
     * Создает новое игровое поле с указанными размерами и начальным количеством мин.
     * При создании поля также устанавливаются глобальные диапазоны для {@link CellPosition}
     * на основе размеров этого поля.
     *
     * @param rows             Количество строк на поле. Должно быть положительным.
     * @param columns          Количество столбцов на поле. Должно быть положительным.
     * @param initialMineCount Начальное количество мин, которые будут размещены на поле.
     *                         Должно быть не отрицательным и не превышать общее количество ячеек.
     * @throws IllegalArgumentException если размеры поля не положительные или количество мин некорректно.
     */
    public MineField(int rows, int columns, int initialMineCount) {
        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException("Размеры поля должны быть положительными.");
        }
        if (initialMineCount < 0 || initialMineCount > rows * columns) {
            throw new IllegalArgumentException("Некорректное количество мин.");
        }

        this.rows = rows;
        this.columns = columns;
        this.mineCount = initialMineCount;

        // Устанавливаем глобальные диапазоны для CellPosition (0-индексация)
        CellPosition.setVerticalRange(0, rows - 1);
        CellPosition.setHorizontalRange(0, columns - 1);

        this.cells = new MineCell[rows][columns];
        initializeField();
    }

    /**
     * Инициализирует поле, создавая объекты {@link MineCell} для каждой позиции.
     * Ячейки создаются в их начальном состоянии (закрыты, без мин).
     */
    private void initializeField() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                cells[r][c] = new MineCell(new CellPosition(r, c));
            }
        }
    }

    /**
     * Размещает заданное {@link #mineCount количество мин} на поле случайным образом.
     * Этот метод обычно вызывается один раз при создании нового игрового поля,
     * после его инициализации методом {@link #initializeField()}.
     * После размещения мин вызывается {@link #calculateAllAdjacentMines()} для подсчета
     * чисел в ячейках.
     */
    public void placeMinesOnNewField() {
        int minesToPlace = this.mineCount;
        if (minesToPlace > rows * columns) { // Дополнительная проверка, если mineCount изменился
            minesToPlace = rows * columns;
        }
        List<CellPosition> availablePositions = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                availablePositions.add(new CellPosition(r,c));
            }
        }

        while (minesToPlace > 0 && !availablePositions.isEmpty()) {
            int randomIndex = random.nextInt(availablePositions.size());
            CellPosition pos = availablePositions.remove(randomIndex);
            MineCell cell = getCell(pos);
            if (cell != null && !cell.isMine()) { // Доп. проверка на null для безопасности
                cell.setMine(true);
                minesToPlace--;
            }
        }
        // Если после попыток разместить все мины остались (например, доступных мест меньше),
        // обновляем фактическое количество мин.
        this.mineCount = this.mineCount - minesToPlace;
        calculateAllAdjacentMines();
    }


    /**
     * Рассчитывает и устанавливает количество мин, соседствующих с каждой ячейкой на всем поле.
     * Для ячеек, которые сами являются минами, количество соседних мин обычно устанавливается в -1
     * или другое специальное значение, чтобы отличить их от пустых ячеек с 0 соседей.
     * Этот метод вызывается после {@link #placeMinesOnNewField()} или после значительных изменений
     * расположения мин на поле.
     */
    public void calculateAllAdjacentMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if (!cells[r][c].isMine()) {
                    cells[r][c].setAdjacentMinesCount(countAdjacentMines(new CellPosition(r, c)));
                } else {
                    cells[r][c].setAdjacentMinesCount(-1); // Мина не имеет числа соседей (или спец. значение)
                }
            }
        }
    }

    /**
     * Пересчитывает количество соседних мин для указанной ячейки и для всех ее восьми непосредственных соседей.
     * Этот метод полезен после того, как мина была перемещена, добавлена или удалена из определенной ячейки,
     * так как это влияет на подсчет соседей для окружающих ячеек.
     *
     * @param pos Позиция {@link CellPosition} ячейки, для которой и вокруг которой
     *            необходимо пересчитать количество соседних мин.
     */
    public void recalculateAdjacentMinesAround(CellPosition pos) {
        // Пересчитываем для самой ячейки
        MineCell centerCell = getCell(pos);
        if (centerCell != null) {
            if (!centerCell.isMine()) {
                centerCell.setAdjacentMinesCount(countAdjacentMines(pos));
            } else {
                centerCell.setAdjacentMinesCount(-1);
            }
        }

        // И для всех ее 8 соседей
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                // if (dr == 0 && dc == 0) continue; // Пропускаем саму центральную ячейку - уже обработана выше

                CellPosition neighborPos = new CellPosition(pos.getRow() + dr, pos.getColumn() + dc);
                if (isValidPosition(neighborPos)) {
                    MineCell neighborCell = getCell(neighborPos);
                    if (neighborCell != null) { // Дополнительная проверка
                        if (!neighborCell.isMine()) {
                            neighborCell.setAdjacentMinesCount(countAdjacentMines(neighborPos));
                        } else {
                            neighborCell.setAdjacentMinesCount(-1);
                        }
                    }
                }
            }
        }
    }

    /**
     * Подсчитывает количество мин, находящихся в восьми соседних ячейках относительно указанной позиции.
     *
     * @param pos Позиция {@link CellPosition}, для которой подсчитываются соседние мины.
     * @return Количество мин в соседних ячейках.
     */
    private int countAdjacentMines(CellPosition pos) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue; // Пропускаем саму ячейку

                CellPosition neighborPos = new CellPosition(pos.getRow() + dr, pos.getColumn() + dc);
                if (isValidPosition(neighborPos)) {
                    MineCell neighborCell = getCell(neighborPos);
                    if (neighborCell != null && neighborCell.isMine()) { // Доп. проверка на null
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Проверяет, является ли указанная позиция валидной (находится ли в пределах игрового поля).
     * Использует статический метод {@link CellPosition#isValid(int, int)}.
     *
     * @param pos Позиция {@link CellPosition} для проверки.
     * @return {@code true}, если позиция находится в пределах поля, иначе {@code false}.
     */
    public boolean isValidPosition(CellPosition pos) {
        if (pos == null) return false;
        return CellPosition.isValid(pos.getRow(), pos.getColumn());
    }

    /**
     * Возвращает объект {@link MineCell} по указанной позиции.
     *
     * @param pos Позиция {@link CellPosition} ячейки.
     * @return Объект {@link MineCell} для данной позиции, или {@code null}, если позиция невалидна.
     */
    public MineCell getCell(CellPosition pos) {
        if (isValidPosition(pos)) {
            return cells[pos.getRow()][pos.getColumn()];
        }
        return null;
    }

    /**
     * Возвращает объект {@link MineCell} по указанным координатам строки и столбца.
     * Является удобной оберткой для {@link #getCell(CellPosition)}.
     *
     * @param r Индекс строки (0-индексация).
     * @param c Индекс столбца (0-индексация).
     * @return Объект {@link MineCell} для данных координат, или {@code null}, если позиция невалидна.
     */
    public MineCell getCell(int r, int c) {
        return getCell(new CellPosition(r,c));
    }

    /**
     * Возвращает количество строк на игровом поле.
     *
     * @return Количество строк.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Возвращает количество столбцов на игровом поле.
     *
     * @return Количество столбцов.
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Возвращает текущее общее количество мин на игровом поле.
     *
     * @return Количество мин.
     */
    public int getMineCount() {
        return mineCount;
    }

    /**
     * Увеличивает счетчик общего количества мин на поле на единицу.
     * Может использоваться, например, при добавлении мины "диверсантом".
     */
    public void incrementMineCount() {
        this.mineCount++;
    }

    /**
     * Уменьшает счетчик общего количества мин на поле на единицу.
     * Может использоваться, например, при удалении мины "диверсантом".
     */
    public void decrementMineCount() {
        if (this.mineCount > 0) {
            this.mineCount--;
        }
    }

    /**
     * Собирает и возвращает список всех мин на поле, которые еще не были открыты.
     *
     * @return Список объектов {@link MineCell}, представляющих активные (неоткрытые) мины.
     */
    public List<MineCell> getActiveMines() {
        List<MineCell> activeMines = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                MineCell cell = cells[r][c];
                if (cell.isMine() && !cell.isOpen()) { // Мина и не открыта
                    activeMines.add(cell);
                }
            }
        }
        return activeMines;
    }

    /**
     * Находит и возвращает список позиций ячеек, которые подходят для перемещения мины "диверсантом".
     * Ячейка считается подходящей, если она:
     * 1. Закрыта.
     * 2. Не содержит мину в данный момент.
     * 3. Хотя бы одна из ее соседних ячеек открыта (т.е. ячейка находится на границе открытой области).
     *
     * @return Список объектов {@link CellPosition}, представляющих подходящие ячейки для перемещения мины.
     */
    public List<CellPosition> getBoundaryCellsForRelocation() {
        List<CellPosition> boundaryCells = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                MineCell currentCell = cells[r][c];
                if (!currentCell.isOpen() && !currentCell.isMine()) {
                    boolean hasOpenNeighbor = false;
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) continue;
                            CellPosition neighborPos = new CellPosition(r + dr, c + dc);
                            if (isValidPosition(neighborPos)) {
                                MineCell neighborCell = getCell(neighborPos);
                                if (neighborCell != null && neighborCell.isOpen()) { // Доп. проверка на null
                                    hasOpenNeighbor = true;
                                    break;
                                }
                            }
                        }
                        if (hasOpenNeighbor) break;
                    }
                    if (hasOpenNeighbor) {
                        boundaryCells.add(currentCell.getPosition());
                    }
                }
            }
        }
        return boundaryCells;
    }

    /**
     * Перемещает мину из одной ячейки ({@code fromPos}) в другую ({@code toPos}).
     * Предполагается, что {@code fromPos} содержит мину, а {@code toPos} - нет и закрыта.
     * После перемещения мины, ячейка {@code fromPos} становится обычной закрытой ячейкой (флаг снимается).
     * Ячейка {@code toPos} становится миной (остается закрытой).
     * Затем вызывается {@link #recalculateAdjacentMinesAround(CellPosition)} для обеих позиций,
     * чтобы обновить счетчики соседних мин в их окрестностях.
     *
     * @param fromPos Исходная позиция {@link CellPosition}, где находится мина.
     * @param toPos   Целевая позиция {@link CellPosition}, куда мина будет перемещена.
     * @return {@code true}, если перемещение было успешно выполнено,
     *         {@code false} — если условия для перемещения не выполнены (например, некорректные позиции,
     *         в {@code fromPos} нет мины, или {@code toPos} уже содержит мину или открыта).
     */
    public boolean relocateMine(CellPosition fromPos, CellPosition toPos) {
        MineCell fromCell = getCell(fromPos);
        MineCell toCell = getCell(toPos);

        if (fromCell == null || toCell == null || !fromCell.isMine() || toCell.isMine() || toCell.isOpen()) {
            return false; // Некорректные условия для перемещения
        }

        // Убираем мину со старого места
        fromCell.setMine(false);
        fromCell.setOpen(false); // Закрываем ячейку, как указано в ТЗ
        fromCell.setFlagged(false); // Снимаем флаг, если был

        // Ставим мину на новое место
        toCell.setMine(true);
        // toCell остается закрытой, флаг не трогаем, если он был (хотя по логике его не должно быть)

        // Пересчитываем соседей вокруг старой и новой позиции мины
        recalculateAdjacentMinesAround(fromPos); // Пересчитать для старой локации и ее соседей
        recalculateAdjacentMinesAround(toPos);   // Пересчитать для новой локации и ее соседей

        return true;
    }

    /**
     * Открывает ячейку в указанной позиции.
     * Если ячейка уже открыта или на ней стоит флаг, действие не выполняется.
     * Если открытая ячейка является миной, метод возвращает {@code true} (мина взорвана).
     * Если открытая ячейка не содержит мины и количество соседних мин равно 0,
     * то рекурсивно открываются все соседние ячейки.
     *
     * @param pos Позиция {@link CellPosition} ячейки, которую нужно открыть.
     * @return {@code true}, если при открытии ячейки была активирована мина, иначе {@code false}.
     */
    public boolean openCellRecursive(CellPosition pos) {
        MineCell cell = getCell(pos);
        if (cell == null || cell.isOpen() || cell.isFlagged()) {
            return false; // Нельзя открыть, или уже открыта, или помечена флагом
        }

        cell.setOpen(true);

        if (cell.isMine()) {
            return true; // Взорвались!
        }

        // Если adjacentMinesCount == 0 (для пустых ячеек) или -1 (для мин, но этот случай уже обработан)
        // Интересует только случай == 0 для рекурсии
        if (cell.getAdjacentMinesCount() == 0) {
            // Рекурсивно открываем соседей
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    CellPosition neighborPos = new CellPosition(pos.getRow() + dr, pos.getColumn() + dc);
                    if (isValidPosition(neighborPos)) {
                        // Рекурсивный вызов не должен возвращать true, если соседи - мины,
                        // так как текущий вызов уже безопасен. Нас интересует только взорвалась ли *эта* ячейка.
                        openCellRecursive(neighborPos);
                    }
                }
            }
        }
        return false; // Безопасно открыли эту ячейку (или она была пустой и инициировала рекурсию)
    }

    /**
     * Подсчитывает общее количество открытых ячеек на поле.
     *
     * @return Количество ячеек, которые были открыты игроком.
     */
    public int getNumberOfOpenedCells() {
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if (cells[r][c].isOpen()) {
                    count++;
                }
            }
        }
        return count;
    }
}