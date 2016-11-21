package org.culpan.herosim;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.culpan.herosim.gui.dice.NormalDamageDiceRoller;
import org.jdom.Element;

public class ImpairmentChart {
	protected static ImpairmentChart instance;
	
	protected String filename;
	
	protected String diceRoll;
	
	protected Map<String, String> locations = new HashMap<String, String>();
	
	protected List<String> rollResults = new ArrayList<String>();
	
	public String getDiceResult() throws Exception {
		//return rollResults.get(NormalDamageDiceRoller.rollDiceTotal(diceRoll) - 1);
		return null;
	}
	
	public String getLocationDescr(String loc) {
		return locations.get(loc);
	}
	
	public Set<String> getLocationsSet() {
		return locations.keySet();
	}
	
	protected ImpairmentChart() {
	}
	
	public static void init() {
		getInstance();
	}
	
	public static ImpairmentChart getInstance() {
		if (instance == null) {
			instance = createInstanceFromClasspath("impair.xml");
		}
		return instance;
	}
	
    protected static ImpairmentChart createInstanceFromClasspath(String filename) {
        ImpairmentChart result = null;
        InputStream input = ImpairmentChart.class.getClassLoader().getResourceAsStream(filename);
        if (input != null) {
        	result = createInstanceFromStream(input);
            result.setFilename(filename);
        }
        
        return result;
    }
    
    @SuppressWarnings("unchecked")
	protected static ImpairmentChart createInstanceFromStream(InputStream input) {
        ImpairmentChart result = new ImpairmentChart();
    	
        Element root = Utils.loadXml(input);
        if (root.getName().equalsIgnoreCase("impairing")) {
        	Element roll = root.getChild("roll");
        	result.diceRoll = roll.getAttributeValue("dice");
        	for (Iterator i = roll.getChildren("result").iterator(); i.hasNext();) {
        		Element e = (Element)i.next();
        		result.rollResults.add(e.getText());
        	}
        	
        	for (Iterator i = root.getChildren("location-descr").iterator(); i.hasNext();) {
        		Element e = (Element)i.next();
        		String [] locs = e.getAttributeValue("id").split(",");
        		for (int j = 0; j < locs.length; j++) {
            		result.locations.put(locs[j].trim(), e.getText());
        		}
        	}
        }
        
        return result;
    }

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return the diceRoll
	 */
	public String getDiceRoll() {
		return diceRoll;
	}

	/**
	 * @param diceRoll the diceRoll to set
	 */
	public void setDiceRoll(String diceRoll) {
		this.diceRoll = diceRoll;
	}
}
