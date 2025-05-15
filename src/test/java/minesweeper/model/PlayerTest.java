package minesweeper.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для класса {@link Player}.
 */
class PlayerTest {

    @Test
    @DisplayName("Конструктор корректно инициализирует игрока с положительным числом жизней")
    void constructor_withPositiveLives_initializesCorrectly() {
        int initialLives = 3;
        Player player = new Player(initialLives);

        assertEquals(initialLives, player.getInitialLives(), "Начальное количество жизней должно совпадать");
        assertEquals(initialLives, player.getLives(), "Текущее количество жизней должно совпадать с начальным");
        assertTrue(player.hasLives(), "Игрок должен иметь жизни при создании");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5}) // Тестируем с 0 и отрицательными значениями
    @DisplayName("Конструктор должен выбрасывать IllegalArgumentException для неположительного числа жизней")
    void constructor_withNonPositiveLives_throwsIllegalArgumentException(int nonPositiveLives) {
        assertThrows(IllegalArgumentException.class, () -> {
            new Player(nonPositiveLives);
        }, "Должно быть выброшено исключение для неположительного числа жизней");
    }

    @Test
    @DisplayName("loseLife уменьшает количество жизней, если они есть")
    void loseLife_decrementsLives_whenLivesArePositive() {
        Player player = new Player(3);
        player.loseLife();
        assertEquals(2, player.getLives());
        assertTrue(player.hasLives());

        player.loseLife();
        assertEquals(1, player.getLives());
        assertTrue(player.hasLives());
    }

    @Test
    @DisplayName("loseLife не уменьшает количество жизней, если их 0")
    void loseLife_doesNotDecrementLives_whenLivesAreZero() {
        Player player = new Player(1); // Начинаем с 1 жизнью
        player.loseLife(); // Становится 0 жизней
        assertEquals(0, player.getLives());
        assertFalse(player.hasLives());

        player.loseLife(); // Пытаемся отнять еще жизнь
        assertEquals(0, player.getLives(), "Жизни не должны стать отрицательными");
        assertFalse(player.hasLives());
    }

    @Test
    @DisplayName("hasLives возвращает true, когда жизни > 0, и false, когда жизни = 0")
    void hasLives_returnsCorrectBoolean() {
        Player playerWithLives = new Player(1);
        assertTrue(playerWithLives.hasLives());

        playerWithLives.loseLife(); // Жизней становится 0
        assertFalse(playerWithLives.hasLives());

        Player playerWithMoreLives = new Player(5);
        assertTrue(playerWithMoreLives.hasLives());
    }

    @Test
    @DisplayName("resetLives восстанавливает начальное количество жизней")
    void resetLives_restoresInitialLives() {
        int initialLives = 5;
        Player player = new Player(initialLives);

        player.loseLife();
        player.loseLife();
        assertEquals(initialLives - 2, player.getLives(), "Жизни должны были уменьшиться");

        player.resetLives();
        assertEquals(initialLives, player.getLives(), "Жизни должны быть восстановлены до начальных");
        assertTrue(player.hasLives());
    }

    @Test
    @DisplayName("getInitialLives возвращает правильное начальное количество жизней")
    void getInitialLives_returnsCorrectValue() {
        Player player1 = new Player(1);
        assertEquals(1, player1.getInitialLives());

        Player player2 = new Player(10);
        assertEquals(10, player2.getInitialLives());
    }
}