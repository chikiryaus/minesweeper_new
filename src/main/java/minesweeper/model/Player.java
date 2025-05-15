package minesweeper.model;

/**
 * Представляет игрока в игре "Сапёр".
 * Основная функция этого класса - отслеживание количества жизней игрока.
 */
public class Player {
    /**
     * Текущее количество жизней игрока.
     */
    private int lives;
    /**
     * Начальное количество жизней, с которым игрок начинает игру.
     * Это значение не изменяется после создания объекта.
     */
    private final int initialLives;

    /**
     * Создает нового игрока с указанным начальным количеством жизней.
     *
     * @param initialLives Начальное количество жизней. Должно быть положительным числом.
     * @throws IllegalArgumentException если {@code initialLives} не является положительным числом.
     */
    public Player(int initialLives) {
        if (initialLives <= 0) {
            throw new IllegalArgumentException("Начальное количество жизней должно быть положительным.");
        }
        this.initialLives = initialLives;
        this.lives = initialLives;
    }

    /**
     * Возвращает текущее количество жизней игрока.
     *
     * @return Текущее количество жизней.
     */
    public int getLives() {
        return lives;
    }

    /**
     * Уменьшает количество жизней игрока на единицу.
     * Жизни не могут стать отрицательными; если текущее количество жизней равно 0,
     * этот метод не производит никаких действий.
     */
    public void loseLife() {
        if (lives > 0) {
            lives--;
        }
    }

    /**
     * Проверяет, остались ли у игрока жизни.
     *
     * @return {@code true}, если у игрока есть хотя бы одна жизнь (lives > 0), иначе {@code false}.
     */
    public boolean hasLives() {
        return lives > 0;
    }

    /**
     * Сбрасывает текущее количество жизней игрока до начального значения.
     * Используется, например, при перезапуске игры.
     */
    public void resetLives() {
        this.lives = this.initialLives;
    }

    /**
     * Возвращает начальное количество жизней, с которым игрок начал игру.
     *
     * @return Начальное количество жизней.
     */
    public int getInitialLives() {
        return initialLives;
    }
}