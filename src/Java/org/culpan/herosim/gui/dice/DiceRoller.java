package org.culpan.herosim.gui.dice;

import java.util.Random;

public class DiceRoller {
    protected final static Random rnd = new Random();
    
    public static int rollDiceTotal(String diceText) throws Exception {
    	int numDice = 0, numSides = 0;
    	
    	String [] diceStats = diceText.split("d");
    	if (diceStats.length != 2) {
    		throw new Exception("Unable to parse '" + diceText + "'");
    	}
    	numDice = Integer.parseInt(diceStats[0]);
    	numSides = Integer.parseInt(diceStats[1]);
    	
    	return rollDiceTotal(numDice, numSides);
    }

    public static int[] rollDice(int numDice, int numSides) {
        int[] result = new int[numDice];

        for (int i = 0; i < numDice; i++) {
            result[i] = rnd.nextInt(numSides) + 1;
        }

        return result;
    }
    
    public static int[] rollDice(float numDice, int numSides) {
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
    
    public static int rollDiceTotal(int numDice, int numSides) {
    	int result = 0;
    	
    	int[] dice = rollDice(numDice, numSides);
    	
    	for (int i = 0; i < dice.length; i++) {
    		result += dice[i];
    	}
    	
    	return result;
    }
}
