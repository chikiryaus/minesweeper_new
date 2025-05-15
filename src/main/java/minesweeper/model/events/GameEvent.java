package minesweeper.model.events;

import minesweeper.model.navigation.CellPosition;
import java.util.EventObject;

/**
 * Представляет событие, произошедшее в игре "Сапёр".
 * Содержит информацию о типе события и, возможно, о позиции ячейки,
 * с которой связано событие.
 */
public class GameEvent extends EventObject {

    /**
     * Перечисление типов событий, которые могут произойти в игре.
     */
    public enum Type {
        /**
         * Событие: Игра началась.
         */
        GAME_STARTED,
        /**
         * Событие: Состояние ячейки было обновлено (например, ячейка открыта,
         * или на ней установлен/снят флаг).
         * Для этого типа события поле {@link GameEvent#getPosition()} будет содержать
         * позицию измененной ячейки.
         */
        CELL_UPDATED,
        /**
         * Событие: Игровое поле было обновлено в целом.
         * Это может произойти, например, после действия "диверсанта",
         * которое затрагивает несколько ячеек или состояние поля.
         */
        FIELD_UPDATED,
        /**
         * Событие: Количество жизней игрока изменилось.
         */
        LIVES_CHANGED,
        /**
         * Событие: Диверсант совершил какое-то действие на поле.
         * Может сопровождаться {@link #FIELD_UPDATED} или {@link #CELL_UPDATED}
         * для конкретизации изменений.
         */
        SABOTEUR_ACTION,
        /**
         * Событие: Игра завершена победой игрока.
         */
        GAME_OVER_WON,
        /**
         * Событие: Игра завершена поражением игрока.
         */
        GAME_OVER_LOST
    }

    /**
     * Тип произошедшего события.
     */
    private final Type type;
    /**
     * Позиция ячейки, связанная с событием.
     * Актуально для событий типа {@link Type#CELL_UPDATED}.
     * Может быть {@code null} для других типов событий.
     */
    private final CellPosition position;

    /**
     * Создает новый объект игрового события.
     *
     * @param source Источник события (обычно это экземпляр класса, управляющего логикой игры, например, {@code MinesweeperGame}).
     * @param type Тип произошедшего события из перечисления {@link Type}.
     * @param position Позиция ячейки {@link CellPosition}, связанная с событием.
     *                 Может быть {@code null}, если событие не привязано к конкретной ячейке
     *                 (например, для {@link Type#GAME_STARTED} или {@link Type#LIVES_CHANGED}).
     */
    public GameEvent(Object source, Type type, CellPosition position) {
        super(source);
        this.type = type;
        this.position = position;
    }

    /**
     * Возвращает тип произошедшего события.
     *
     * @return Тип события из перечисления {@link Type}.
     */
    public Type getType() {
        return type;
    }

    /**
     * Возвращает позицию ячейки, связанную с этим событием.
     *
     * @return Объект {@link CellPosition}, представляющий позицию ячейки,
     *         или {@code null}, если событие не связано с конкретной ячейкой.
     */
    public CellPosition getPosition() {
        return position;
    }
}