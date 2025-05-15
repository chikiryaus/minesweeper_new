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
        // Устанавливаем дефолтные диапазоны для CellPosition, чтобы избежать проблем
        // если тесты запускаются в разном порядке или другие тесты их меняют.
        // Выберите размеры, которые не конфликтуют с размерами полей в ваших тестах,
        // или устанавливайте их более конкретно в каждом тесте, если нужно.
        CellPosition.setVerticalRange(0, 9);
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
        listener.clearEvents();

        CellPosition pos = new CellPosition(0, 0);
        game.openCell(pos);

        assertEquals(MinesweeperGame.GameState.WON, game.getGameState());
        assertTrue(game.getMineField().getCell(pos).isOpen());

        List<GameEvent> events = listener.receivedEvents;
        assertEquals(2, events.size());
        assertEquals(GameEvent.Type.CELL_UPDATED, events.get(0).getType());
        assertEquals(pos, events.get(0).getPosition());
        assertEquals(GameEvent.Type.GAME_OVER_WON, events.get(1).getType());
    }

    @Test
    @DisplayName("openCell на мине (поле с 1 миной, 1 жизнь) приводит к проигрышу")
    void openCell_onSingleMine_leadsToLoss() {
        game = new MinesweeperGame(1, 1, 1, 1, null); // 1 ячейка, 1 мина, 1 жизнь
        game.addGameActionListener(listener);
        game.startGame();
        listener.clearEvents();

        CellPosition pos = new CellPosition(0, 0); // Эта ячейка будет миной
        int initialMineCountOnField = game.getMineField().getMineCount(); // Должно быть 1

        game.openCell(pos);

        assertEquals(MinesweeperGame.GameState.LOST, game.getGameState());
        assertEquals(0, game.getPlayer().getLives());
        assertEquals(initialMineCountOnField - 1, game.getMineField().getMineCount(), "Количество мин на поле должно уменьшиться");
        assertTrue(game.getMineField().getCell(pos).isOpen());

        List<GameEvent> events = listener.receivedEvents;
        assertEquals(4, events.size(), "Ожидается 4 события: CELL_UPDATED, LIVES_CHANGED, FIELD_UPDATED (от revealAllMines), GAME_OVER_LOST");
        assertEquals(GameEvent.Type.CELL_UPDATED, events.get(0).getType());
        assertEquals(pos, events.get(0).getPosition());
        assertEquals(GameEvent.Type.LIVES_CHANGED, events.get(1).getType());
        assertEquals(GameEvent.Type.FIELD_UPDATED, events.get(2).getType(), "Ожидается FIELD_UPDATED от revealAllMines");
        assertEquals(GameEvent.Type.GAME_OVER_LOST, events.get(3).getType());
    }

    @Test
    @DisplayName("openCell на мине с несколькими жизнями, игра продолжается")
    void openCell_onMineWithMultipleLives_gameContinues() {
        game = new MinesweeperGame(2, 1, 1, 2, null); // 2 ячейки, 1 мина, 2 жизни
        game.addGameActionListener(listener);
        game.startGame(); // Мина разместится на (0,0) или (1,0)
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

        List<GameEvent> events = listener.receivedEvents;
        assertEquals(2, events.size());
        assertEquals(GameEvent.Type.CELL_UPDATED, events.get(0).getType());
        assertEquals(minePos, events.get(0).getPosition());
        assertEquals(GameEvent.Type.LIVES_CHANGED, events.get(1).getType());
    }

    @Test
    @DisplayName("openCell на безопасной ячейке, диверсант действует и перемещает мину на единственное доступное место")
    void openCell_onSafeCell_saboteurActsAndRelocatesToOnlySpot() {
        RelocatingSaboteur realSaboteur = new RelocatingSaboteur();
        // Создаем поле 3x3. Мина на (0,0). Откроем (1,1).
        // Единственное место для перемещения будет (0,1).
        // Остальные соседи (1,1) будут либо минами, либо открыты.
        game = new MinesweeperGame(3, 3, 1, 1, realSaboteur); // 1 мина изначально для placeMinesOnNewField

        // --- Начало ручной настройки поля для предсказуемости ---
        // Это грязный способ, лучше бы MineField имел методы для такой настройки.
        // После startGame() MineField уже создан и мина размещена случайно.
        // Нам нужно поле с известной конфигурацией.
        MineField controlledField = new MineField(3, 3, 1); // 1 мина, но мы ее поставим сами
        CellPosition initialMinePos = new CellPosition(0,0);
        CellPosition cellToOpenPos = new CellPosition(1,1);
        CellPosition expectedRelocationSpot = new CellPosition(0,1);

        controlledField.getCell(initialMinePos).setMine(true);
        // Остальные ячейки - не мины
        for(int r=0; r<3; r++) {
            for(int c=0; c<3; c++) {
                CellPosition current = new CellPosition(r,c);
                if (!current.equals(initialMinePos) && controlledField.getCell(current) != null) {
                    controlledField.getCell(current).setMine(false);
                }
            }
        }
        // Сделаем все соседи cellToOpenPos (кроме initialMinePos и expectedRelocationSpot) невалидными для перемещения
        controlledField.getCell(0,2).setOpen(true);
        controlledField.getCell(1,0).setOpen(true);
        controlledField.getCell(1,2).setOpen(true);
        controlledField.getCell(2,0).setOpen(true);
        controlledField.getCell(2,1).setOpen(true);
        controlledField.getCell(2,2).setOpen(true);
        // expectedRelocationSpot (0,1) остается закрытым и не миной.
        // cellToOpenPos (1,1) остается закрытым и не миной.
        // initialMinePos (0,0) - мина, закрыта.

        controlledField.calculateAllAdjacentMines(); // Важно для getBoundaryCellsForRelocation

        // "Внедряем" наше поле в игру (используя рефлексию, т.к. нет сеттера)
        try {
            java.lang.reflect.Field mineFieldInternal = MinesweeperGame.class.getDeclaredField("mineField");
            mineFieldInternal.setAccessible(true);
            mineFieldInternal.set(game, controlledField);

            // Также установим gameState в PLAYING, так как startGame не вызывался с этим полем
            java.lang.reflect.Field gameStateField = MinesweeperGame.class.getDeclaredField("gameState");
            gameStateField.setAccessible(true);
            gameStateField.set(game, MinesweeperGame.GameState.PLAYING);

            // Обновим player и mineCount в game, если это нужно (зависит от конструктора)
            java.lang.reflect.Field playerInternal = MinesweeperGame.class.getDeclaredField("player");
            playerInternal.setAccessible(true);
            playerInternal.set(game, new Player(1)); // 1 жизнь

        } catch (Exception e) {
            fail("Ошибка настройки теста через рефлексию: " + e.getMessage());
        }
        // --- Конец ручной настройки поля ---

        game.addGameActionListener(listener);
        listener.clearEvents();

        assertFalse(game.getMineField().getCell(expectedRelocationSpot).isMine(), "Ожидаемое место для перемещения изначально не мина");

        game.openCell(cellToOpenPos); // Открываем (1,1)

        assertTrue(listener.hasEventOfType(GameEvent.Type.SABOTEUR_ACTION), "Событие SABOTEUR_ACTION должно было произойти");
        assertTrue(listener.hasEventOfType(GameEvent.Type.FIELD_UPDATED), "Событие FIELD_UPDATED после диверсанта");

        assertFalse(game.getMineField().getCell(initialMinePos).isMine(), "Мина должна была переместиться с " + initialMinePos);
        assertTrue(game.getMineField().getCell(expectedRelocationSpot).isMine(), "Мина должна была переместиться на " + expectedRelocationSpot);
        assertEquals(MinesweeperGame.GameState.PLAYING, game.getGameState(), "Игра должна продолжаться, если не было других мин");
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
        assertEquals(1, listener.receivedEvents.size());


        listener.clearEvents();
        game.toggleFlag(pos); // Снимаем флаг
        assertFalse(cell.isFlagged());
        assertTrue(listener.hasEventOfType(GameEvent.Type.CELL_UPDATED));
        assertEquals(1, listener.receivedEvents.size());
    }
}