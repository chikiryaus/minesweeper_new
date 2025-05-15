package minesweeper.model.events;

import java.util.EventListener;

/**
 * Интерфейс слушателя для событий, происходящих в игре "Сапёр".
 * Реализации этого интерфейса могут быть зарегистрированы для получения уведомлений
 * об изменениях состояния игры.
 */
public interface GameActionListener extends EventListener {
    /**
     * Вызывается, когда в игре происходит какое-либо значимое событие.
     *
     * @param event Объект {@link GameEvent}, содержащий информацию о произошедшем событии.
     */
    void gameChanged(GameEvent event);
}