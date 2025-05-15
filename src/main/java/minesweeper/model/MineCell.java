package minesweeper.model;


import minesweeper.model.navigation.CellPosition;

/**
 * Представляет одну ячейку на минном поле игры "Сапёр".
 * Хранит информацию о том, является ли ячейка миной, открыта ли она,
 * установлен ли на ней флаг, и сколько мин находится в соседних ячейках.
 * Также хранит свою позицию на поле.
 */
public class MineCell {
    /**
     * {@code true}, если в этой ячейке находится мина, иначе {@code false}.
     */
    private boolean isMine;
    /**
     * {@code true}, если эта ячейка была открыта игроком, иначе {@code false}.
     */
    private boolean isOpen;
    /**
     * {@code true}, если на этой ячейке игроком установлен флаг, иначе {@code false}.
     */
    private boolean isFlagged;
    /**
     * Количество мин, находящихся в восьми соседних ячейках.
     * Это значение отображается игроку, если ячейка открыта и не является миной.
     */
    private int adjacentMinesCount;

    /**
     * Позиция данной ячейки на игровом поле (координаты строки и столбца).
     */
    private final CellPosition position;

    /**
     * Создает новую ячейку для указанной позиции на поле.
     * По умолчанию ячейка создается закрытой, не миной, без флага,
     * и с нулевым количеством соседних мин.
     *
     * @param position Позиция {@link CellPosition} этой ячейки на игровом поле.
     */
    public MineCell(CellPosition position) {
        this.position = position;
        this.isMine = false;
        this.isOpen = false;
        this.isFlagged = false;
        this.adjacentMinesCount = 0;
    }

    /**
     * Возвращает позицию этой ячейки на игровом поле.
     *
     * @return Объект {@link CellPosition}, представляющий координаты ячейки.
     */
    public CellPosition getPosition() {
        return position;
    }

    /**
     * Проверяет, содержит ли эта ячейка мину.
     *
     * @return {@code true}, если ячейка является миной, иначе {@code false}.
     */
    public boolean isMine() {
        return isMine;
    }

    /**
     * Устанавливает или убирает мину в этой ячейке.
     *
     * @param mine {@code true}, чтобы сделать ячейку миной, {@code false} — чтобы убрать мину.
     */
    public void setMine(boolean mine) {
        isMine = mine;
    }

    /**
     * Проверяет, открыта ли эта ячейка.
     *
     * @return {@code true}, если ячейка открыта, иначе {@code false}.
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Устанавливает состояние "открыта" для ячейки.
     * Если ячейка открывается ({@code open} становится {@code true}),
     * с нее автоматически снимается флаг, если он был установлен.
     *
     * @param open {@code true}, чтобы открыть ячейку, {@code false} — чтобы закрыть (обычно не используется напрямую).
     */
    public void setOpen(boolean open) {
        isOpen = open;
        if (isOpen) {
            isFlagged = false; // Открытая ячейка не может быть помечена флагом
        }
    }

    /**
     * Проверяет, установлен ли флаг на этой ячейке.
     *
     * @return {@code true}, если на ячейке установлен флаг, иначе {@code false}.
     */
    public boolean isFlagged() {
        return isFlagged;
    }

    /**
     * Переключает состояние флага на ячейке.
     * Если ячейка закрыта и не помечена флагом, флаг устанавливается.
     * Если ячейка закрыта и помечена флагом, флаг снимается.
     * Если ячейка уже открыта, этот метод не производит никаких действий.
     */
    public void toggleFlag() {
        if (!isOpen) {
            isFlagged = !isFlagged;
        }
    }

    /**
     * Устанавливает или снимает флаг с ячейки.
     * Флаг можно установить только на закрытую ячейку.
     * Если попытка установить флаг ({@code flagged} равно {@code true}) на открытую ячейку,
     * состояние флага не изменится.
     * Если {@code flagged} равно {@code false}, флаг будет снят независимо от того, открыта ячейка или нет.
     *
     * @param flagged {@code true}, чтобы установить флаг, {@code false} — чтобы снять флаг.
     */
    public void setFlagged(boolean flagged) {
        if (!isOpen) { // Если ячейка закрыта
            isFlagged = flagged;
        } else if (!flagged) { // Если ячейка открыта, но мы хотим снять флаг
            isFlagged = false; // Позволяем снять флаг, если он был как-то установлен на открытой (хотя не должен)
        }
        // Если ячейка открыта и flagged=true, ничего не делаем (флаг не ставим на открытую).
    }


    /**
     * Возвращает количество мин в соседних ячейках.
     *
     * @return Количество мин, примыкающих к этой ячейке.
     */
    public int getAdjacentMinesCount() {
        return adjacentMinesCount;
    }

    /**
     * Устанавливает количество мин в соседних ячейках.
     * Это значение обычно вычисляется при инициализации игрового поля.
     *
     * @param adjacentMinesCount Количество мин, примыкающих к этой ячейке.
     */
    public void setAdjacentMinesCount(int adjacentMinesCount) {
        this.adjacentMinesCount = adjacentMinesCount;
    }

    /**
     * Сбрасывает состояние ячейки к её начальным значениям:
     * не мина, закрыта, без флага, количество соседних мин равно 0.
     * Этот метод может использоваться при полной перезагрузке игрового поля
     * или при перемещении мины с одной ячейки на другую.
     */
    public void reset() {
        isMine = false;
        isOpen = false;
        isFlagged = false;
        adjacentMinesCount = 0;
    }

    /**
     * Возвращает строковое представление состояния ячейки, удобное для отладки или текстового отображения поля.
     * <ul>
     *   <li>"F" - если на ячейке установлен флаг.</li>
     *   <li>"#" - если ячейка закрыта и не помечена флагом.</li>
     *   <li>"*" - если ячейка открыта и является миной.</li>
     *   <li>" " (пробел) - если ячейка открыта, не мина, и нет соседних мин.</li>
     *   <li>Число (например, "1", "2") - если ячейка открыта, не мина, и есть соседние мины (число показывает их количество).</li>
     * </ul>
     *
     * @return Строковое представление ячейки.
     */
    @Override
    public String toString() {
        if (isFlagged) return "F";
        if (!isOpen) return "#"; // Закрытая ячейка
        if (isMine) return "*"; // Открытая мина
        return adjacentMinesCount > 0 ? String.valueOf(adjacentMinesCount) : " "; // Открытая пустая или с числом
    }
}