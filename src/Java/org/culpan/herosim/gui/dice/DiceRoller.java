package org.culpan.herosim.gui.dice;

import java.util.Random;

/**
 * Created by USUCUHA on 11/21/2016.
 */
public class DiceRoller {
    public enum DamageType {NORMAL_DAMAGE, KILLING_DAMAGE, STANDARD_ROLL};

    protected final static Random rnd = new Random();

    public static class DamageResult {
        public DamageType damageType;
        public int stun;
        public int body;
        public int knockback = 0;
        public int knockbackResisted = 0;

        protected DamageResult(DamageType damageType) {
            this.damageType = damageType;
        }
    }

    public static DamageResult createNormalDamageResult(int stun, int body) {
        DamageResult damageResult = new DamageResult(DamageType.NORMAL_DAMAGE);
        damageResult.stun = stun;
        damageResult.body = body;
        return damageResult;
    }

    public int[] rollDice(float numDice, int numSides) {
        int[] result;// = new int[(int)numDice + 1];
        if (numDice % 1 != 0) {
            result = new int[(int)numDice + 1];
        } else {
            result = new int[(int)numDice];
        }

        for (int i = 0; i < (int)numDice; i++) {
            result[i] = rnd.nextInt(numSides) + 1;
        }

        if (numDice % 1 != 0) {
            result[(int)numDice] = (int)(((double)(rnd.nextInt(numSides) + 1) * (numDice % 1)) + 0.5);
        }

        return result;
    }

    public int[] rollDice(int numDice, int numSides) {
        int[] result = new int[numDice];

        for (int i = 0; i < numDice; i++) {
            result[i] = rnd.nextInt(numSides) + 1;
        }

        return result;
    }

    public int rollDiceTotal(int numDice, int numSides) {
        int result = 0;

        int[] dice = rollDice(numDice, numSides);

        for (int i = 0; i < dice.length; i++) {
            result += dice[i];
        }

        return result;
    }

}
