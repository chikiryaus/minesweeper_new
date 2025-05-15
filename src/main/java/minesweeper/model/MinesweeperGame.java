package minesweeper.model;

import minesweeper.model.events.GameEvent;
import minesweeper.model.events.GameActionListener;
import minesweeper.model.navigation.CellPosition;

import java.util.ArrayList;
import java.util.List;
// import java.util.Objects; // Не используется напрямую

/**
 * Основной класс модели игры "Сапёр".
 * Управляет игровым полем ({@link MineField}), состоянием игрока ({@link Player}),
 * возможными действиями "диверсанта" ({@link Saboteur}),
 * а также общим состоянием игры (начата, играется, выиграна, проиграна).
 * Отвечает за обработку действий игрока и оповещение слушателей об игровых событиях.
 */
public class MinesweeperGame {
    /**
     * Игровое поле с ячейками и минами.
     */
    private MineField mineField;
    /**
     * Объект, представляющий игрока и его жизни.
     */
    private Player player;
    /**
     * Объект "диверсанта", который может влиять на игру. Может быть {@code null}, если диверсант не участвует.
     */
    private Saboteur saboteur;
    /**
     * Текущее состояние игры.
     */
    private GameState gameState;

    /**
     * Количество строк на игровом поле. Определяется при создании игры.
     */
    private final int rows;
    /**
     * Количество столбцов на игровом поле. Определяется при создании игры.
     */
    private final int columns;
    /**
     * Начальное количество мин, установленное для этой игры. Используется для определения условия победы.
     * Фактическое количество мин на поле может меняться (например, если игрок взрывает мину).
     */
    private final int initialMineCountSetting; // Переименовано для ясности
    /**
     * Начальное количество жизней у игрока. Определяется при создании игры.
     */
    private final int initialLives;

    /**
     * Список слушателей, которые будут уведомлены об игровых событиях.
     */
    private final List<GameActionListener> listeners = new ArrayList<>();

    /**
     * Перечисление возможных состояний игры.
     */
    public enum GameState {
        /**
         * Игра еще не началась (поле создано, но первый ход не сделан или игра не запущена).
         */
        NOT_STARTED,
        /**
         * Игра находится в процессе.
         */
        PLAYING,
        /**
         * Игра завершена победой игрока.
         */
        WON,
        /**
         * Игра завершена поражением игрока.
         */
        LOST
    }

    /**
     * Создает новый экземпляр игры "Сапёр" с заданными параметрами.
     * Поле и игрок инициализируются, но сама игра не запускается автоматически;
     * для этого необходимо вызвать метод {@link #startGame()}.
     *
     * @param rows           Количество строк на игровом поле.
     * @param columns        Количество столбцов на игровом поле.
     * @param mineCount      Начальное количество мин.
     * @param initialLives   Начальное количество жизней у игрока.
     * @param saboteur       Экземпляр "диверсанта" или {@code null}, если диверсант не используется.
     */
    public MinesweeperGame(int rows, int columns, int mineCount, int initialLives, Saboteur saboteur) {
        this.rows = rows;
        this.columns = columns;
        this.initialMineCountSetting = mineCount; // Сохраняем начальное значение как настройку
        this.initialLives = initialLives;
        this.saboteur = saboteur;

        this.player = new Player(this.initialLives);
        // MineField создается с количеством мин, равным начальной настройке
        this.mineField = new MineField(this.rows, this.columns, this.initialMineCountSetting);
        this.gameState = GameState.NOT_STARTED;
    }

    /**
     * Запускает или перезапускает игру.
     * Создается новый игрок и новое игровое поле с начальными настройками.
     * Мины размещаются на поле, и состояние игры устанавливается в {@link GameState#PLAYING}.
     * Генерируется событие {@link GameEvent.Type#GAME_STARTED}.
     */
    public void startGame() {
        this.player = new Player(this.initialLives);
        // При каждом старте игры MineField создается с ИЗНАЧАЛЬНЫМ количеством мин
        this.mineField = new MineField(this.rows, this.columns, this.initialMineCountSetting);
        this.mineField.placeMinesOnNewField();
        this.gameState = GameState.PLAYING;
        fireGameEvent(GameEvent.Type.GAME_STARTED, null);
    }

    /**
     * Обрабатывает попытку игрока открыть ячейку в указанной позиции.
     * Если ячейка открывается успешно (не мина), проверяется условие победы.
     * Если на ячейке мина, игрок теряет жизнь, а количество активных мин на поле уменьшается.
     * Диверсант действует только после успешного открытия ячейки игроком, если игра не завершена.
     *
     * @param pos Позиция {@link CellPosition} ячейки, которую пытается открыть игрок.
     */
    public void openCell(CellPosition pos) {
        if (gameState != GameState.PLAYING) {
            return; // Игра не в процессе
        }

        MineCell cell = mineField.getCell(pos);

        // Проверка, можно ли вообще открыть эту ячейку.
        // Если нет (невалидна, уже открыта, помечена флагом), то это не "успешное открытие".
        if (cell == null || cell.isOpen() || cell.isFlagged()) {
            return; // Диверсант не должен срабатывать, т.к. не было нового открытия
        }

        // Попытка открыть ячейку
        boolean hitMine = mineField.openCellRecursive(pos);
        fireGameEvent(GameEvent.Type.CELL_UPDATED, pos); // Уведомляем об изменении ячейки

        if (hitMine) {
            player.loseLife();
            mineField.decrementMineCount(); // <<< УМЕНЬШАЕМ ТЕКУЩЕЕ КОЛИЧЕСТВО МИН НА ПОЛЕ
            fireGameEvent(GameEvent.Type.LIVES_CHANGED, null);
            // Событие FIELD_UPDATED не нужно здесь напрямую, т.к. LIVES_CHANGED и CELL_UPDATED
            // обычно приводят к перерисовке нужных частей UI. Счетчик мин обновится
            // через GameActionListener -> repaint -> countRemainingFlagsOrMines (который использует mineField.getMineCount()).

            if (!player.hasLives()) {
                gameState = GameState.LOST;
                revealAllMines();
                fireGameEvent(GameEvent.Type.GAME_OVER_LOST, null);
                return; // Игра окончена (проигрыш), диверсант не действует
            }
            // Игрок подорвался, но игра продолжается. Ход считается "успешным открытием" (мины).
        } else {
            // Мина НЕ была взорвана. Проверяем условие победы.
            checkWinCondition();
            if (gameState == GameState.WON) {
                return; // Игра окончена (выигрыш), диверсант не действует
            }
            // Ход считается "успешным открытием" (безопасной ячейки).
        }

        // Если мы здесь, это означает:
        // 1. Игрок сделал ход по валидной, закрытой, не флагнутой ячейке.
        // 2. Этот ход не привел к немедленному окончательному проигрышу или выигрышу.
        // => Игра все еще в состоянии PLAYING.
        // => Это и есть "успешное открытие", после которого может действовать диверсант.
        if (saboteur != null) { // gameState == GameState.PLAYING здесь подразумевается
            boolean saboteurActed = saboteur.performAction(mineField, this);
            if (saboteurActed) {
                fireGameEvent(GameEvent.Type.SABOTEUR_ACTION, null);
                fireGameEvent(GameEvent.Type.FIELD_UPDATED, null); // Поле изменилось диверсантом

                // После действия диверсанта состояние игры могло измениться.
                // Например, если диверсант убрал последнюю "ложную" мину и условие победы выполнилось.
                // Или если бы диверсант мог взорвать игрока (не текущий случай).
                if (!hitMine) { // Если игрок не взорвался на своем ходу, проверяем победу после диверсанта
                    checkWinCondition();
                }
                // Если игра завершилась после действия диверсанта, выходим.
                if (gameState == GameState.WON || gameState == GameState.LOST) {
                    return;
                }
            }
        }
    }

    /**
     * Открывает все мины на поле. Обычно вызывается при проигрыше.
     * Генерирует событие {@link GameEvent.Type#FIELD_UPDATED}.
     */
    private void revealAllMines() {
        for (int r = 0; r < mineField.getRows(); r++) {
            for (int c = 0; c < mineField.getColumns(); c++) {
                MineCell cell = mineField.getCell(r, c);
                if (cell.isMine() && !cell.isOpen()) {
                    cell.setOpen(true);
                }
            }
        }
        fireGameEvent(GameEvent.Type.FIELD_UPDATED, null);
    }


    /**
     * Переключает состояние флага на ячейке в указанной позиции.
     * Действует, только если игра в процессе и ячейка не открыта.
     * Генерирует событие {@link GameEvent.Type#CELL_UPDATED}.
     *
     * @param pos Позиция {@link CellPosition} ячейки.
     */
    public void toggleFlag(CellPosition pos) {
        if (gameState != GameState.PLAYING) return;

        MineCell cell = mineField.getCell(pos);
        if (cell != null && !cell.isOpen()) {
            cell.toggleFlag();
            fireGameEvent(GameEvent.Type.CELL_UPDATED, pos);
        }
    }

    /**
     * Проверяет, выполнены ли условия для победы в игре.
     * Условие победы: все ячейки, которые изначально не содержали мин, должны быть открыты.
     * Если условия выполнены, состояние игры меняется на {@link GameState#WON},
     * и генерируется событие {@link GameEvent.Type#GAME_OVER_WON}.
     * Проверка выполняется, только если игра находится в состоянии {@link GameState#PLAYING}.
     */
    // В MinesweeperGame.java

    private void checkWinCondition() {
        if (gameState != GameState.PLAYING) {
            return;
        }

        int totalCells = rows * columns;
        int openCellsCount = mineField.getNumberOfOpenedCells();

        // Количество ячеек, которые ИЗНАЧАЛЬНО не были минами и должны быть открыты для победы.
        int nonMineCellsToOpenTarget = totalCells - this.initialMineCountSetting;

        // Основное и единственное строгое условие победы:
        // Открыты все ячейки, которые изначально не были минами.
        if (openCellsCount == nonMineCellsToOpenTarget) {
            gameState = GameState.WON;
            fireGameEvent(GameEvent.Type.GAME_OVER_WON, null);

        }
    }

    /**
     * Возвращает текущее состояние игры.
     * @return Текущее {@link GameState}.
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Возвращает объект игрового поля.
     * @return Текущий {@link MineField}.
     */
    public MineField getMineField() {
        return mineField;
    }

    /**
     * Возвращает объект игрока.
     * @return Текущий {@link Player}.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Добавляет слушателя игровых событий.
     * @param listener Слушатель {@link GameActionListener}.
     */
    public void addGameActionListener(GameActionListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Удаляет слушателя игровых событий.
     * @param listener Слушатель {@link GameActionListener}.
     */
    public void removeGameActionListener(GameActionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Создает и отправляет игровое событие всем зарегистрированным слушателям.
     *
     * @param type     Тип события {@link GameEvent.Type}.
     * @param position Позиция {@link CellPosition}, связанная с событием (может быть {@code null}).
     */
    private void fireGameEvent(GameEvent.Type type, CellPosition position) {
        GameEvent event = new GameEvent(this, type, position);
        List<GameActionListener> listenersCopy = new ArrayList<>(listeners); // Для безопасной итерации
        for (GameActionListener listener : listenersCopy) {
            listener.gameChanged(event);
        }
    }
}