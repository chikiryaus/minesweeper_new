package minesweeper.model;

import minesweeper.model.navigation.CellPosition;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Реализация "диверсанта" ({@link Saboteur}), который специализируется на перемещении
 * одной из существующих, еще не взорванных и не отмеченных флагом мин на поле.
 * Мина перемещается на случайную закрытую ячейку, которая граничит с уже открытой областью
 * и не содержит другую мину.
 */
public class RelocatingSaboteur implements Saboteur {
    /**
     * Генератор случайных чисел для выбора мины и места для её перемещения.
     */
    private final Random random = new Random();

    /**
     * Выполняет действие диверсанта: пытается переместить одну активную (неоткрытую) мину
     * на новую позицию на границе уже открытой области поля.
     * <p>
     * Логика действия:
     * <ol>
     *   <li>Получает список всех активных (неоткрытых) мин на поле.</li>
     *   <li>Получает список всех доступных для перемещения ячеек (закрытые, без мин, на границе открытой области).</li>
     *   <li>Если нет активных мин или нет подходящих мест для перемещения, действие не выполняется.</li>
     *   <li>Случайным образом выбирается одна активная мина для перемещения.</li>
     *   <li>Случайным образом выбирается подходящее место для перемещения, которое не совпадает
     *       с текущей позицией выбранной мины.</li>
     *   <li>Если такое место найдено, мина перемещается с использованием метода {@link MineField#relocateMine(CellPosition, CellPosition)}.</li>
     * </ol>
     *
     * @param field Игровое поле ({@link MineField}), на котором диверсант будет действовать.
     * @param game  Текущий экземпляр игры ({@link MinesweeperGame}). В данной реализации не используется напрямую,
     *              но присутствует для соответствия интерфейсу {@link Saboteur}.
     * @return {@code true}, если диверсанту удалось успешно переместить мину, иначе {@code false}
     *         (например, если не было мин для перемещения, не было подходящих мест,
     *         или выбранное место совпало с исходным положением мины и других вариантов не нашлось).
     */
    @Override
    public boolean performAction(MineField field, MinesweeperGame game) {
        List<MineCell> activeMines = field.getActiveMines();
        List<CellPosition> relocationSpots = field.getBoundaryCellsForRelocation();

        if (activeMines.isEmpty() || relocationSpots.isEmpty()) {
            // System.out.println("Диверсант: Нет активных мин или мест для перемещения.");
            return false; // Нет мин для перемещения или нет мест для перемещения
        }

        // Выбираем случайную активную мину
        MineCell mineToRelocate = activeMines.get(random.nextInt(activeMines.size()));

        // Выбираем случайное место для перемещения, не совпадающее с текущим положением мины
        Collections.shuffle(relocationSpots); // Перемешиваем, чтобы выбор был более случайным
        CellPosition targetSpot = null;
        for(CellPosition spot : relocationSpots) {
            if (!spot.equals(mineToRelocate.getPosition())) {
                targetSpot = spot;
                break;
            }
        }

        if (targetSpot == null) {
            // Это может произойти, если единственное доступное место для перемещения -
            // это текущая позиция одной из мин, и именно эта мина была выбрана.
            // Или если relocationSpots содержит только позицию выбранной мины.
            // System.out.println("Диверсант: Не удалось найти подходящее отличное от текущего место для мины " + mineToRelocate.getPosition());
            return false;
        }

        System.out.println("Диверсант: Перемещает мину с " + mineToRelocate.getPosition() + " на " + targetSpot);
        boolean success = field.relocateMine(mineToRelocate.getPosition(), targetSpot);
        if (success) {
            System.out.println("Диверсант: Мина успешно перемещена.");
        } else {
            System.out.println("Диверсант: Не удалось переместить мину (внутренняя ошибка MineField.relocateMine).");
        }
        return success;
    }
}