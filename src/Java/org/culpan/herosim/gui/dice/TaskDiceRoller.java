package org.culpan.herosim.gui.dice;

import org.apache.commons.lang.ArrayUtils;

/**
 * Created by USUCUHA on 11/22/2016.
 */
public class TaskDiceRoller extends DiceRoller {
    public static class TaskDiceResult {
        public int total;
        public int dice[];

        protected TaskDiceResult(int dice[]) {
            this.dice = ArrayUtils.clone(dice);
            total = 0;
            for (int i = 0; i < dice.length; i++) {
                total += dice[i];
            }
        }

        public static TaskDiceResult createTaskDiceResult(int d1, int d2, int d3) {
            return new TaskDiceResult(new int[] {d1, d2, d3});
        }

        public static TaskDiceResult createTaskDiceResult(int [] dice) {
            return new TaskDiceResult(dice);
        }
    }

    public TaskDiceResult rollTaskDice() {
        int result[] = rollDice(3, 6);
        if (result.length != 3) {
            throw new RuntimeException("Invalid result");
        }
        return TaskDiceResult.createTaskDiceResult(result);
    }
}
