package minesweeper.view;

import minesweeper.model.MineCell;
import minesweeper.model.MinesweeperGame;
// import minesweeper.model.Player; // Не используется напрямую, но game.getPlayer() используется
import minesweeper.model.events.GameEvent;
import minesweeper.model.events.GameActionListener;
import minesweeper.model.navigation.CellPosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Панель Swing, отвечающая за визуальное отображение игры "Сапёр" и обработку ввода пользователя (клики мыши).
 * Эта панель является слушателем событий игры ({@link GameActionListener}) и обновляет свое
 * отображение при получении уведомлений от модели игры ({@link MinesweeperGame}).
 */
public class MinesweeperPanel extends JPanel implements GameActionListener {

    private final MinesweeperGame game;
    private static final int CELL_SIZE = 30;
    private static final int HEADER_HEIGHT = 50;

    // Цвета
    private static final Color COLOR_CLOSED = new Color(192, 192, 192);
    private static final Color COLOR_OPEN_EMPTY = new Color(220, 220, 220);
    private static final Color COLOR_MINE = Color.RED;
    private static final Color COLOR_FLAG = Color.BLUE;
    private static final Color COLOR_GRID = Color.DARK_GRAY;
    private static final Color COLOR_NUMBER_1 = new Color(0, 0, 255);
    private static final Color COLOR_NUMBER_2 = new Color(0, 128, 0);
    private static final Color COLOR_NUMBER_3 = new Color(255, 0, 0);
    private static final Color COLOR_NUMBER_4 = new Color(0, 0, 128);
    private static final Color COLOR_NUMBER_5 = new Color(128, 0, 0);
    private static final Color COLOR_NUMBER_6 = new Color(0, 128, 128);
    private static final Color COLOR_NUMBER_7 = new Color(0, 0, 0);
    private static final Color COLOR_NUMBER_8 = new Color(128, 128, 128);

    /**
     * Флаг для включения/выключения режима отладки, показывающего мины.
     */
    private boolean debugShowMines = false; // <<< НОВОЕ ПОЛЕ

    public MinesweeperPanel(MinesweeperGame game) {
        this.game = game;
        this.game.addGameActionListener(this);

        int fieldWidth = game.getMineField().getColumns() * CELL_SIZE;
        int fieldHeight = game.getMineField().getRows() * CELL_SIZE;
        setPreferredSize(new Dimension(fieldWidth, fieldHeight + HEADER_HEIGHT));
        setBackground(Color.LIGHT_GRAY);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (game.getGameState() != MinesweeperGame.GameState.PLAYING &&
                        game.getGameState() != MinesweeperGame.GameState.NOT_STARTED) { // Разрешим клик, если игра еще не началась (для первого хода)
                    // Если игра закончена
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        // Предложить начать новую игру
                        // int dialogResult = JOptionPane.showConfirmDialog(MinesweeperPanel.this, "Начать новую игру?", "Игра окончена", JOptionPane.YES_NO_OPTION);
                        // if(dialogResult == JOptionPane.YES_OPTION){
                        //     game.startGame();
                        // }
                    }
                    return;
                }
                // Если игра NOT_STARTED, первый клик левой кнопкой должен запустить игру (если такая логика нужна)
                if (game.getGameState() == MinesweeperGame.GameState.NOT_STARTED && e.getButton() == MouseEvent.BUTTON1) {
                    // Здесь можно было бы добавить логику "первый клик всегда безопасен",
                    // но для этого нужно модифицировать MinesweeperGame.startGame() или добавить новый метод.
                    // Пока просто считаем, что игра начнется с клика.
                    // game.startGameIfNotStarted(); // Hypothetical method
                }


                int r = (e.getY() - HEADER_HEIGHT) / CELL_SIZE;
                int c = e.getX() / CELL_SIZE;

                if (e.getY() < HEADER_HEIGHT) return;

                CellPosition clickedPos = new CellPosition(r, c);
                if (game.getMineField().isValidPosition(clickedPos)) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        game.openCell(clickedPos);
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        game.toggleFlag(clickedPos);
                    }
                }
            }
        });
    }

    /**
     * Включает или выключает режим отладки, при котором отображаются все мины.
     * После изменения состояния панель будет перерисована.
     *
     * @param show {@code true} для включения отображения мин, {@code false} для выключения.
     */
    public void setDebugShowMines(boolean show) { // <<< НОВЫЙ МЕТОД
        this.debugShowMines = show;
        repaint(); // Перерисовать панель, чтобы изменения вступили в силу
    }

    /**
     * Возвращает текущее состояние режима отладки отображения мин.
     * @return {@code true}, если режим отладки включен, иначе {@code false}.
     */
    public boolean isDebugShowMinesEnabled() { // <<< НОВЫЙ МЕТОД (необязательный, но полезный)
        return this.debugShowMines;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawHeader(g);
        drawMineField(g);

        if (game.getGameState() == MinesweeperGame.GameState.WON) {
            drawGameStatus(g, "ПОБЕДА!", Color.GREEN.darker());
        } else if (game.getGameState() == MinesweeperGame.GameState.LOST) {
            drawGameStatus(g, "ПОРАЖЕНИЕ!", Color.RED.darker());
        }
    }

    private void drawHeader(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), HEADER_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));

        String livesText = "Жизни: " + (game.getPlayer() != null ? game.getPlayer().getLives() : "-");
        String minesText = "Мины: " + (game.getMineField() != null ? countRemainingFlagsOrMines() : "-");

        g.drawString(livesText, 10, HEADER_HEIGHT / 2 + 5);

        FontMetrics fm = g.getFontMetrics();
        int minesTextWidth = fm.stringWidth(minesText);
        g.drawString(minesText, getWidth() - minesTextWidth - 10, HEADER_HEIGHT / 2 + 5);
    }

    private int countRemainingFlagsOrMines() {
        if (game.getMineField() == null) return 0;
        int flagsSet = 0;
        for (int r = 0; r < game.getMineField().getRows(); r++) {
            for (int c = 0; c < game.getMineField().getColumns(); c++) {
                MineCell cell = game.getMineField().getCell(r,c);
                if (cell != null && cell.isFlagged()) {
                    flagsSet++;
                }
            }
        }
        return game.getMineField().getMineCount() - flagsSet;
    }


    private void drawMineField(Graphics g) {
        if (game.getMineField() == null) return;

        int rows = game.getMineField().getRows();
        int cols = game.getMineField().getColumns();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                MineCell cell = game.getMineField().getCell(new CellPosition(r, c));
                if (cell == null) continue;

                int x = c * CELL_SIZE;
                int y = r * CELL_SIZE + HEADER_HEIGHT;

                // Заливка ячейки
                if (!cell.isOpen()) {
                    g.setColor(COLOR_CLOSED);
                    g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    if (cell.isFlagged()) {
                        drawFlag(g, x, y);
                    }
                    // <<< НАЧАЛО ОТЛАДОЧНОЙ ОТРИСОВКИ МИН >>>
                    if (debugShowMines && cell.isMine()) {
                        // Не рисуем отладочный маркер поверх флага, если флаг уже нарисован
                        if (!cell.isFlagged()) {
                            drawDebugMineMarker(g, x, y);
                        }
                    }
                    // <<< КОНЕЦ ОТЛАДОЧНОЙ ОТРИСОВКИ МИН >>>
                } else {
                    if (cell.isMine()) {
                        g.setColor(COLOR_MINE);
                        g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        drawMineSymbol(g, x, y);
                    } else {
                        g.setColor(COLOR_OPEN_EMPTY);
                        g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        if (cell.getAdjacentMinesCount() > 0) {
                            drawNumber(g, x, y, cell.getAdjacentMinesCount());
                        }
                    }
                }

                // Сетка
                g.setColor(COLOR_GRID);
                g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    /**
     * Отрисовывает отладочный маркер мины.
     * Используется, когда debugShowMines = true, для закрытых ячеек с минами.
     */
    private void drawDebugMineMarker(Graphics g, int x, int y) { // <<< НОВЫЙ МЕТОД
        g.setColor(new Color(255, 100, 0, 150)); // Полупрозрачный оранжевый
        // Маленький кружок в центре
        int markerSize = CELL_SIZE / 3;
        g.fillOval(x + (CELL_SIZE - markerSize) / 2, y + (CELL_SIZE - markerSize) / 2, markerSize, markerSize);
    }


    private void drawFlag(Graphics g, int x, int y) {
        g.setColor(COLOR_FLAG);
        g.setFont(new Font("Arial", Font.BOLD, CELL_SIZE / 2 + 2));
        String flagSymbol = "P";
        FontMetrics fm = g.getFontMetrics();
        int stringX = x + (CELL_SIZE - fm.stringWidth(flagSymbol)) / 2;
        int stringY = y + (CELL_SIZE - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(flagSymbol, stringX, stringY);
    }

    private void drawMineSymbol(Graphics g, int x, int y) {
        g.setColor(Color.BLACK);
        int padding = CELL_SIZE / 4;
        g.fillOval(x + padding, y + padding, CELL_SIZE - 2 * padding, CELL_SIZE - 2 * padding);
    }

    private void drawNumber(Graphics g, int x, int y, int number) {
        switch (number) {
            case 1: g.setColor(COLOR_NUMBER_1); break;
            case 2: g.setColor(COLOR_NUMBER_2); break;
            case 3: g.setColor(COLOR_NUMBER_3); break;
            case 4: g.setColor(COLOR_NUMBER_4); break;
            case 5: g.setColor(COLOR_NUMBER_5); break;
            case 6: g.setColor(COLOR_NUMBER_6); break;
            case 7: g.setColor(COLOR_NUMBER_7); break;
            case 8: g.setColor(COLOR_NUMBER_8); break;
            default: g.setColor(Color.BLACK);
        }
        g.setFont(new Font("Arial", Font.BOLD, CELL_SIZE / 2));
        String numStr = String.valueOf(number);
        FontMetrics fm = g.getFontMetrics();
        int stringX = x + (CELL_SIZE - fm.stringWidth(numStr)) / 2;
        int stringY = y + (CELL_SIZE - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(numStr, stringX, stringY);
    }

    private void drawGameStatus(Graphics g, String message, Color color) {
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 200));
        g.fillRect(0, HEADER_HEIGHT, getWidth(), getHeight() - HEADER_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics fm = g.getFontMetrics();
        int msgWidth = fm.stringWidth(message);
        int msgHeight = fm.getHeight();

        int fieldDrawingAreaHeight = getHeight() - HEADER_HEIGHT;
        int stringX = (getWidth() - msgWidth) / 2;
        int stringY = HEADER_HEIGHT + (fieldDrawingAreaHeight - msgHeight) / 2 + fm.getAscent();
        g.drawString(message, stringX, stringY);
    }


    @Override
    public void gameChanged(GameEvent event) {
        repaint();
    }
}