package minesweeper;

import minesweeper.model.MinesweeperGame;
import minesweeper.model.RelocatingSaboteur;
import minesweeper.view.MinesweeperPanel;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Параметры игры
        int rows = 10;
        int cols = 10;
        int mineCount = 15; // или (int) (rows * cols * 0.15); // 15% мин
        int lives = 3;

        // Создаем экземпляр диверсанта
        RelocatingSaboteur saboteur = new RelocatingSaboteur();

        // Создаем модель игры
        MinesweeperGame game = new MinesweeperGame(rows, cols, mineCount, lives, saboteur);

        // Создаем UI
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Сапёр (Расширенный)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            MinesweeperPanel panel = new MinesweeperPanel(game);
            frame.add(panel, BorderLayout.CENTER);

            // ----- ВКЛЮЧЕНИЕ РЕЖИМА ОТОБРАЖЕНИЯ МИН -----
            panel.setDebugShowMines(false);
            // --------------------------------------------

            // Меню для новой игры (простое)
            JMenuBar menuBar = new JMenuBar();
            JMenu gameMenu = new JMenu("Игра");
            JMenuItem newGameItem = new JMenuItem("Новая игра");
            newGameItem.addActionListener(e -> {
                // Здесь можно добавить диалог для ввода параметров новой игры
                // Для простоты, перезапускаем с теми же параметрами
                game.startGame();
                // panel.repaint(); // repaint() будет вызван автоматически из-за GameEvent.GAME_STARTED
            });
            gameMenu.add(newGameItem);

            // <<< НАЧАЛО: Добавление меню отладки >>>
            JMenu debugMenu = new JMenu("Отладка");
            JCheckBoxMenuItem toggleDebugMinesItem = new JCheckBoxMenuItem("Показывать мины");
            toggleDebugMinesItem.setSelected(panel.isDebugShowMinesEnabled()); // Установить начальное состояние
            toggleDebugMinesItem.addActionListener(e -> {
                panel.setDebugShowMines(toggleDebugMinesItem.isSelected());
            });
            debugMenu.add(toggleDebugMinesItem);
            menuBar.add(debugMenu);
            // <<< КОНЕЦ: Добавление меню отладки >>>

            frame.setJMenuBar(menuBar);


            frame.pack(); // Устанавливает размер окна на основе PreferredSize панели
            frame.setLocationRelativeTo(null); // Центрируем окно
            frame.setResizable(false); // Чтобы не ломать расчеты ячеек
            frame.setVisible(true);

            // Начинаем игру после того, как UI готов
            game.startGame();
        });
    }
}