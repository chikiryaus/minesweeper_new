package minesweeper.model;

import minesweeper.model.events.GameActionListener;
import minesweeper.model.events.GameEvent;
import minesweeper.model.navigation.CellPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MinesweeperGameTest {

    private MinesweeperGame game;
    private TestGameActionListener listener;

    // Вспомогательный класс слушателя для сбора событий
    private static class TestGameActionListener implements GameActionListener {
        public final List<GameEvent> receivedEvents = new ArrayList<>();
        public void clearEvents() {
            receivedEvents.clear();
        }
        @Override
        public void gameChanged(GameEvent event) {
            receivedEvents.add(event);
        }
        public GameEvent getLastEvent() {
            if (receivedEvents.isEmpty()) return null;
            return receivedEvents.get(receivedEvents.size() - 1);
        }
        public boolean hasEventOfType(GameEvent.Type type) {
            return receivedEvents.stream().anyMatch(e -> e.getType() == type);
        }
    }

    @BeforeEach
    void setUp() {
        listener = new TestGameActionListener();
        // Для CellPosition, если тесты запускаются в разном порядке
        CellPosition.setVerticalRange(0, 9); // Установим некий дефолт
        CellPosition.setHorizontalRange(0, 9);
    }

    @Test
    @DisplayName("startGame устанавливает состояние PLAYING и генерирует событие GAME_STARTED")
    void startGame_setsPlayingStateAndFiresEvent() {
        game = new MinesweeperGame(3, 3, 1, 1, null);
        game.addGameActionListener(listener);

        assertEquals(MinesweeperGame.GameState.NOT_STARTED, game.getGameState());
        game.startGame();

        assertEquals(MinesweeperGame.GameState.PLAYING, game.getGameState());
        assertNotNull(listener.getLastEvent());
        assertEquals(GameEvent.Type.GAME_STARTED, listener.getLastEvent().getType());
        assertEquals(1, listener.receivedEvents.size());
    }

    @Test
    @DisplayName("openCell на безопасной ячейке (поле без мин) приводит к победе")
    void openCell_onSafeCellNoMines_leadsToWin() {
        game = new MinesweeperGame(1, 1, 0, 1, null); // 1 ячейка, 0 мин
        game.addGameActionListener(listener);
        game.startGame();
        listener.clearEvents(); // Очищаем событие startGame

        CellPosition pos = new CellPosition(0, 0);
        game.openCell(pos);

        assertEquals(MinesweeperGame.GameState.WON, game.getGameState());
        assertTrue(game.getMineField().getCell(pos).isOpen());
        assertTrue(listener.hasEventOfType(GameEvent.Type.CELL_UPDATED));
        assertTrue(listener.hasEventOfType(GameEvent.Type.GAME_OVER_WON));
        assertEquals(2, listener.receivedEvents.size()); // CELL_UPDATED, GAME_OVER_WON
    }

    @Test
    @DisplayName("openCell на мине (поле с 1 миной) приводит к проигрышу")
    void openCell_onSingleMine_leadsToLoss() {
        game = new MinesweeperGame(1, 1, 1, 1, null); // 1 ячейка, 1 мина, 1 жизнь
        // В этом случае, единственная ячейка гарантированно будет миной
        game.addGameActionListener(listener);
        game.startGame(); // placeMinesOnNewField() поставит мину на (0,0)
        listener.clearEvents();

        CellPosition pos = new CellPosition(0, 0);
        game.openCell(pos);

        assertEquals(MinesweeperGame.GameState.LOST, game.getGameState());
        assertEquals(0, game.getPlayer().getLives());
        assertEquals(0, game.getMineField().getMineCount(), "Количество мин на поле должно уменьшиться"); // Была 1, стала 0
        assertTrue(game.getMineField().getCell(pos).isOpen());

        assertTrue(listener.hasEventOfType(GameEvent.Type.CELL_UPDATED));
        assertTrue(listener.hasEventOfType(GameEvent.Type.LIVES_CHANGED));
        assertTrue(listener.hasEventOfType(GameEvent.Type.GAME_OVER_LOST));
        assertEquals(3, listener.receivedEvents.size());
    }

    @Test
    @DisplayName("openCell на мине с несколькими жизнями, игра продолжается")
    void openCell_onMineWithMultipleLives_gameContinues() {
        game = new MinesweeperGame(2, 1, 1, 2, null); // 2 ячейки, 1 мина, 2 жизни
        // Нам нужно, чтобы мина была в известном месте.
        // После startGame, мина будет либо на (0,0) либо на (1,0).
        game.addGameActionListener(listener);
        game.startGame();
        listener.clearEvents();

        CellPosition minePos;
        if (game.getMineField().getCell(0,0).isMine()) {
            minePos = new CellPosition(0,0);
        } else {
            minePos = new CellPosition(1,0);
            assertTrue(game.getMineField().getCell(1,0).isMine(), "Мина должна быть на (1,0) если не на (0,0)");
        }

        int initialMineCountOnField = game.getMineField().getMineCount();

        game.openCell(minePos);

        assertEquals(MinesweeperGame.GameState.PLAYING, game.getGameState());
        assertEquals(1, game.getPlayer().getLives());
        assertEquals(initialMineCountOnField - 1, game.getMineField().getMineCount());
        assertTrue(listener.hasEventOfType(GameEvent.Type.CELL_UPDATED));
        assertTrue(listener.hasEventOfType(GameEvent.Type.LIVES_CHANGED));
        assertEquals(2, listener.receivedEvents.size()); // CELL_UPDATED, LIVES_CHANGED
    }

    @Test
    @DisplayName("openCell на безопасной ячейке, диверсант действует (если есть)")
    void openCell_onSafeCell_saboteurActsIfPresent() {
        // Используем реального RelocatingSaboteur, но нам нужно, чтобы он мог что-то сделать.
        // Поле 3x3. 1 мина, 1 жизнь.
        RelocatingSaboteur realSaboteur = new RelocatingSaboteur();
        game = new MinesweeperGame(3, 3, 1, 1, realSaboteur);
        game.addGameActionListener(listener);
        game.startGame(); // Мина разместится где-то
        listener.clearEvents();

        // Откроем ячейку, которая точно не мина и создаст границу для диверсанта.
        // Найдем безопасную ячейку.
        CellPosition safePosToOpen = null;
        CellPosition minePos = null;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (game.getMineField().getCell(r,c).isMine()) {
                    minePos = new CellPosition(r,c);
                } else {
                    safePosToOpen = new CellPosition(r,c);
                }
            }
        }
        assertNotNull(safePosToOpen, "Должна быть найдена безопасная ячейка");
        assertNotNull(minePos, "Должна быть найдена мина");

        // Перед открытием убедимся, что есть куда перемещать мину
        // (т.е. есть закрытая ячейка, не мина, на границе с будущей открытой)
        // Это условие сложно гарантировать без детального анализа поля.
        // Проще проверить, что performAction был вызван, если saboteur != null.
        // Для этого теста, однако, нам нужен сценарий, где он ВЕРНЕТ true.

        // Для предсказуемости, создадим ситуацию, где диверсант точно сработает.
        // (0,0) - мина, (1,1) - откроем, (0,1) - место для перемещения.
        game = new MinesweeperGame(3, 3, 1, 1, realSaboteur);
        game.getMineField().getCell(0,0).setMine(true); // Мина
        game.getMineField().getCell(0,1).setMine(false); // Место для перемещения
        game.getMineField().getCell(1,1).setMine(false); // Откроем
        game.getMineField().calculateAllAdjacentMines();
        game.addGameActionListener(listener);
        game.startGame(); // Перезапишет наши мины! Нужно вызывать startGame до ручной установки.
        // Либо не вызывать startGame, а вручную установить gameState.

        game = new MinesweeperGame(3, 3, 1, 1, realSaboteur);
        // Установим gameState вручную, чтобы не было случайного размещения мин
        // Это грязновато, но для теста...
        try {
            java.lang.reflect.Field gameStateField = MinesweeperGame.class.getDeclaredField("gameState");
            gameStateField.setAccessible(true);
            gameStateField.set(game, MinesweeperGame.GameState.PLAYING);

            java.lang.reflect.Field mineFieldInternal = MinesweeperGame.class.getDeclaredField("mineField");
            mineFieldInternal.setAccessible(true);
            MineField knownField = new MineField(3,3,1);
            knownField.getCell(0,0).setMine(true);
            knownField.getCell(0,1).setMine(false); // Место
            knownField.getCell(1,1).setMine(false); // Откроем
            knownField.calculateAllAdjacentMines();
            mineFieldInternal.set(game, knownField);

        } catch (Exception e) { fail("Failed to set up test with reflection: " + e.getMessage()); }

        game.addGameActionListener(listener);
        listener.clearEvents();

        game.openCell(new CellPosition(1,1)); // Открываем (1,1)

        assertTrue(listener.hasEventOfType(GameEvent.Type.SABOTEUR_ACTION), "Событие SABOTEUR_ACTION должно было произойти");
        assertTrue(listener.hasEventOfType(GameEvent.Type.FIELD_UPDATED), "Событие FIELD_UPDATED после диверсанта");
        // Проверить, что мина переместилась (например, (0,0) больше не мина, а (0,1) - мина)
        assertFalse(game.getMineField().getCell(0,0).isMine(), "Мина должна была переместиться с (0,0)");
        assertTrue(game.getMineField().getCell(0,1).isMine(), "Мина должна была переместиться на (0,1)");
    }


    @Test
    @DisplayName("toggleFlag на закрытой ячейке переключает флаг")
    void toggleFlag_onClosedCell_togglesFlag() {
        game = new MinesweeperGame(3, 3, 1, 1, null);
        game.addGameActionListener(listener);
        game.startGame();
        listener.clearEvents();

        CellPosition pos = new CellPosition(1, 1);
        MineCell cell = game.getMineField().getCell(pos);

        assertFalse(cell.isFlagged());
        game.toggleFlag(pos);
        assertTrue(cell.isFlagged());
        assertTrue(listener.hasEventOfType(GameEvent.Type.CELL_UPDATED));
        assertEquals(pos, listener.getLastEvent().getPosition());

        listener.clearEvents();
        game.toggleFlag(pos);
        assertFalse(cell.isFlagged());
        assertTrue(listener.hasEventOfType(GameEvent.Type.CELL_UPDATED));
    }

    // Можно добавить тесты для openCell на уже открытой/флагованной ячейке,
    // toggleFlag на открытой ячейке - они должны быть проще.
}