package org.culpan.herosim.gui.dice;

import java.util.ArrayList;

public class NormalDamageDiceRoller extends DiceRoller {
    protected boolean rollKnockback = true;
    
    public DamageResult rollNormalDamage(String text) {
        String[] fields = splitText(text);

        if (text.trim().length() == 0) {
            return null; // Empty text, nothing to do
        } else if (fields.length < 1) {
            throw new DiceParseException("Invalid syntax; found only " + Integer.toString(fields.length) + " tokens");
        } else if (fields[fields.length - 1].equals("+") || fields[fields.length - 1].equals("-")) {
            throw new DiceParseException("Cannot terminate an expression with a '+' or '-'");
        }

        float num = 0;
        int mod = 0, sides = 6, stunMult = 0;
        try {
            num = Float.parseFloat(fields[0]);

            for (int i = 1; i < fields.length; i += 2) {
                if (fields[i].equalsIgnoreCase("d")) {
                    sides = Integer.parseInt(fields[i + 1]);
                } else if (fields[i].equals("+")) {
                    mod += Integer.parseInt(fields[i + 1]);
                } else if (fields[i].equals("-")) {
                    mod -= Integer.parseInt(fields[i + 1]);
                } else if (fields[i].equalsIgnoreCase("k") || fields[i].equalsIgnoreCase("ka")) {
                    throw new DiceParseException("Unable to process killing attacks");
                } else if (fields[i].equalsIgnoreCase("n") || fields[i].equalsIgnoreCase("na")
                        || fields[i].equalsIgnoreCase("nd")) {
                    if (i < fields.length - 1) {
                        throw new DiceParseException("Expected EOL; found '" + fields[i + 1] + "'");
                    }
                } else {
                    throw new DiceParseException("Unrecognized token '" + fields[i] + "'");
                }
            }
        } catch (Exception e) {
            throw new DiceParseException("Error : " + e.getMessage(), e);
        }

        return rollNormalDamage(num, mod);
    }

    protected String[] splitText(String text) {
        ArrayList<String> fields = new ArrayList<String>();

        String currField = "";
        boolean isDigitField = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c) && currField.length() > 0) {
                fields.add(currField);
                currField = "";
            } else if (Character.isDigit(c) || c == '.') {
                if (!isDigitField && currField.length() > 0) {
                    fields.add(currField);
                    currField = "";
                }
                isDigitField = true;
                currField += c;
            } else if (Character.isLetter(c)) {
                if (isDigitField && currField.length() > 0) {
                    fields.add(currField);
                    currField = "";
                }
                isDigitField = false;
                currField += c;
            } else if (currField.length() > 0) {
                fields.add(currField);
                currField = "";
                fields.add(Character.toString(c));
            } else if (c == '+' || c == '-' || c == 'd' || c == 'D') {
                fields.add(Character.toString(c));
            }
        }

        if (currField.length() > 0) {
            fields.add(currField);
        }

        String[] result = new String[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            result[i] = fields.get(i).toString();
        }

        return result;
    }

    protected int rollKnockback(DamageType damageType) {
        int result = 0;

        if (rollKnockback && damageType == DamageType.KILLING_DAMAGE) {
            result = rollDiceTotal(3, 6);
        } else if (rollKnockback && damageType == DamageType.NORMAL_DAMAGE) {
            result = rollDiceTotal(2, 6);
        }

        return result;
    }

    protected DamageResult rollNormalDamage(float num, int mod) {
//        HitLocationChart.Location hitLocation = null;
        int[] rolls = rollDice(num, 6);
        int stun = mod, body = 0, knockbackRes = rollKnockback(DamageType.NORMAL_DAMAGE);
        for (int i = 0; i < rolls.length; i++) {
            stun += rolls[i];
            if (rolls[i] > 5) {
                body += 2;
            } else if (rolls[i] > 1) {
                body += 1;
            }
        }

        DamageResult damageResult = DiceRoller.createNormalDamageResult(stun, body);
        damageResult.knockbackResisted = knockbackRes;
        damageResult.knockback = (body - knockbackRes > 0 ? body - knockbackRes : 0);
        return damageResult;
    }

}
