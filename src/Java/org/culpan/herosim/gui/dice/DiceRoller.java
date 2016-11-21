package org.culpan.herosim.gui.dice;

import java.util.Random;

/**
 * Created by USUCUHA on 11/21/2016.
 */
public class DiceRoller {
    public enum DamageType {NORMAL_DAMAGE, KILLING_DAMAGE};

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
}
