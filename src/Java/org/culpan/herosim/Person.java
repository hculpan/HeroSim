/*
 * Created on Dec 10, 2004
 *
 */
package org.culpan.herosim;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * @author CulpanH
 *  
 */
public abstract class Person {
    public final static int[][] PHASES = { { 7 }, { 6, 12 }, { 4, 8, 12 }, { 3, 6, 9, 12 }, { 3, 5, 8, 10, 12 },
            { 2, 4, 6, 8, 10, 12 }, { 2, 4, 6, 7, 9, 11, 12 }, { 2, 3, 5, 6, 8, 9, 11, 12 },
            { 2, 3, 4, 6, 7, 8, 10, 11, 12 }, { 2, 3, 4, 5, 6, 8, 9, 10, 11, 12 },
            { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 }, { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 } };

    public final static int NO_DELAYED_PHASE = -1;

    protected static final Logger logger = Logger.getLogger(Person.class);
    
    protected boolean acted;
    protected boolean aborted;

    protected int speed;
    protected int dex;
    protected int con;
    protected int rec;
    protected String name;
    protected String displayName;
    protected String displaySuffix;
    protected int nextPhase = NO_DELAYED_PHASE;
    protected boolean partyMember;
    private Person target;
    protected String targetName;

    protected int stunnedPhases = 0;
    protected int flashed = 0;

    protected int body = 0;
    protected int stun = 0;

	protected int pd = 0;
	protected int ed = 0;
	protected int dcv = 0;
    
    protected int currentBody = 0;
    protected int currentStun = 0; 

    protected int[] phases;
    
    public Person() {
    	
    }
    
    public Person(String name, int spd, int dex) {
    	setName(name);
    	setSpeed(spd);
    	setDex(dex);
    }

    /**
     * @return Returns the dex.
     */
    public int getDex() {
        return dex;
    }

    /**
     * @param dex
     *            The dex to set.
     */
    public void setDex(int dex) {
        this.dex = dex;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the speed.
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * @param speed
     *            The speed to set.
     */
    public void setSpeed(int speed) {
        this.speed = speed;
        if (speed > 0 && speed < 13) {
            phases = PHASES[speed - 1];
        } else {
            logger.error("Invalid speed '" + Integer.toString(speed) + "'");
        }
    }

    public boolean actsInPhase(int phase) {
        boolean result = false;

        if (phases != null) {
            for (int i = 0; i < phases.length; i++) {
                if (phases[i] == phase) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * @return Returns the phases.
     */
    public int[] getPhases() {
        return phases;
    }

    /**
     * This method takes a JDOM Element representing a character and initializes
     * the object from it.
     * 
     * @param root
     */
    public void initFromXml(Element root) {
    	if (root.getAttributeValue("party-member") != null) {
    		partyMember = Boolean.valueOf(root.getAttributeValue("party-member")).booleanValue();
    	}
    	
        setName(root.getChildText("name"));

        Element chars = root.getChild("characteristics");
        Element dex = chars.getChild("dex");
        setDex(Utils.parseInt(dex.getChildText("total")));
        Element speed = chars.getChild("spd");
        setSpeed(Utils.parseInt(speed.getChildText("total")));
        Element con = chars.getChild("con");
        if (con != null) {
        	setCon(Utils.parseInt(con.getChildText("total")));
        }
        Element rec = chars.getChild("rec");
        if (rec != null) {
        	setRec(Utils.parseInt(rec.getChildText("total")));
        }
		Element pd = chars.getChild("pd");
		if (pd != null) {
			setPd(Utils.parseInt(pd.getChildText("total")));
		}
		Element ed = chars.getChild("ed");
		if (ed != null) {
			setEd(Utils.parseInt(ed.getChildText("total")));
		}
		Element dcv = chars.getChild("dcv");
		if (dcv != null) {
			setDcv(Utils.parseInt(dcv.getChildText("total")));
		}

		Element body;
        if ((body = chars.getChild("body")) !=null) {
        	setBody(Utils.parseInt(body.getChildText("total")));
        	if (body.getChildText("current") != null) {
        		setCurrentBody(Utils.parseInt(body.getChildText("current")));
        	} else {
        		setCurrentBody(Utils.parseInt(body.getChildText("total")));
        	}
        }
        
        Element stun;
        if ((stun = chars.getChild("stun")) !=null) {
        	setStun(Utils.parseInt(stun.getChildText("total")));
        	if (stun.getChildText("current") != null) {
        		setCurrentStun(Utils.parseInt(stun.getChildText("current")));
        	} else {
        		setCurrentStun(Utils.parseInt(stun.getChildText("total")));
        	}
        }
        
        Element acted;
        if ((acted = chars.getChild("acted")) != null) {
        	setActed(Boolean.parseBoolean(acted.getText()));
        }
        
        if (chars.getChild("aborted") != null) {
        	setAborted(Boolean.parseBoolean(chars.getChildText("aborted")));
        }
        
        if (chars.getChild("stunned") != null) {
        	setStunnedPhases(Integer.parseInt(chars.getChildText("stunned")));
        }
        
        if (chars.getChild("flashed") != null) {
        	setFlashed(Integer.parseInt(chars.getChildText("flashed")));
        }
        
        if (chars.getChild("target-name") != null) {
        	setTargetName(chars.getChildText("target-name"));
        }
    }
    
    protected abstract Element getRootElement();
    
    public Element toXml() {
    	Element result = getRootElement();
    	
    	result.addContent(new Element("name").setText(getDisplayName()));
    	Element characteristics = new Element("characteristics")
    		.addContent(new Element("dex").addContent(new Element("total").setText(Integer.toString(getDex()))))
    		.addContent(new Element("con").addContent(new Element("total").setText(Integer.toString(getCon()))))
			.addContent(new Element("pd").addContent(new Element("total").setText(Integer.toString(getPd()))))
			.addContent(new Element("ed").addContent(new Element("total").setText(Integer.toString(getEd()))))
			.addContent(new Element("dcv").addContent(new Element("total").setText(Integer.toString(getDcv()))))
    		.addContent(new Element("rec").addContent(new Element("total").setText(Integer.toString(getRec()))))
    		.addContent(new Element("spd").addContent(new Element("total").setText(Integer.toString(getSpeed()))))
    		.addContent(new Element("body").addContent(new Element("total").setText(Integer.toString(getBody())))
    				.addContent(new Element("current").setText(Integer.toString(getCurrentBody())))
    				)
    		.addContent(new Element("stun").addContent(new Element("total").setText(Integer.toString(getStun())))
    				.addContent(new Element("current").setText(Integer.toString(getCurrentStun())))
    				)
    		.addContent(new Element("acted").setText(Boolean.toString(hasActed())))
    		.addContent(new Element("aborted").setText(Boolean.toString(hasAborted())))
    		.addContent(new Element("stunned").setText(Integer.toString(getStunnedPhases())))
    		.addContent(new Element("flashed").setText(Integer.toString(getFlashed())));

    	if (getTargetName() != null) {
    		characteristics.addContent(new Element("target-name").setText(getTargetName()));
    	}
    	
    	result.addContent(characteristics);
    	
    	return result;
    }

    /**
     * @return Returns the displayName.
     */
    public String getDisplayName() {
        if (displayName == null) {
            displayName = name;
        }
        return displayName;
    }

    /**
     * @param displayName
     *            The displayName to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return Returns the nextPhase.
     */
    public int getNextPhase(int currentPhase) {
        if (nextPhase == NO_DELAYED_PHASE) {
            int index = 0;
            int rounds = 0;
            while (true) {
                if (phases[index % phases.length] + (rounds * 12) > currentPhase) {
                    return phases[index % phases.length];
                }
                index++;
                rounds = index / phases.length;
                if (rounds > 1) {
                    logger.error("Unable to calculate next phase.");
                    break;
                }
            }
        }

        return nextPhase;
    }
    
    public abstract Person copy();

    protected void copyDataTo(Person p) {
    	p.setDisplayName(displayName);
    	p.setName(getName());
    	p.setSpeed(getSpeed());
    	p.setDex(getDex());
    	p.setCon(getCon());
    	p.setBody(getBody());
    	p.setCurrentBody(getCurrentBody());
    	p.setStun(getStun());
    	p.setCurrentStun(getCurrentStun());
    	p.setRec(getRec());
    	p.setActed(hasActed());
    	p.setAborted(hasAborted());
    	p.setStunnedPhases(getStunnedPhases());
    	p.setFlashed(getFlashed());
//    	p.setTargetName(targetName);
    }

    /**
     * @param nextPhase
     *            The nextPhase to set.
     */
    public void setNextPhase(int nextPhase) {
        this.nextPhase = nextPhase;
    }

    public boolean isStunned() {
        return stunnedPhases > 0;
    }

    public boolean isUnconscious() {
        return currentStun <= 0;
    }
    
    public int getStunnedPhases() {
    	return stunnedPhases;
    }

    public void setStunnedPhases(int numPhases) {
        stunnedPhases = numPhases;
    }

    public void endingPhase(int phase) {
		if ((phase == 12 && getCurrentStun() >= -20) || (isUnconscious() && getCurrentStun() >= -10)) {
			recoveryPhase();
		} 
        stunnedPhases--;
    }

    public String toString() {
    	return name;
    }

	/**
	 * @return Returns the partyMember.
	 */
	public boolean isPartyMember() {
		return partyMember;
	}

	/**
	 * @param partyMember The partyMember to set.
	 */
	public void setPartyMember(boolean partyMember) {
		this.partyMember = partyMember;
	}

	/**
	 * @return the currentBody
	 */
	public int getCurrentBody() {
		return currentBody;
	}

	/**
	 * @param currentBody the currentBody to set
	 */
	public void setCurrentBody(int currentBody) {
		this.currentBody = currentBody;
	}

	/**
	 * @return the currentStun
	 */
	public int getCurrentStun() {
		return currentStun;
	}

	/**
	 * @param currentStun the currentStun to set
	 */
	public void setCurrentStun(int currentStun) {
		this.currentStun = currentStun;
	}

	/**
	 * @return the body
	 */
	public int getBody() {
		return body;
	}

	/**
	 * @param body the body to set
	 */
	public void setBody(int body) {
		this.body = body;
	}

	/**
	 * @return the stun
	 */
	public int getStun() {
		return stun;
	}

	/**
	 * @param stun the stun to set
	 */
	public void setStun(int stun) {
		this.stun = stun;
	}

	/**
	 * @return the con
	 */
	public int getCon() {
		return con;
	}

	/**
	 * @param con the con to set
	 */
	public void setCon(int con) {
		this.con = con;
	}

	/**
	 * @return the rec
	 */
	public int getRec() {
		return rec;
	}

	/**
	 * @param rec the rec to set
	 */
	public void setRec(int rec) {
		this.rec = rec;
	}
	
	public void adjustCurrentStun(int value) {
		currentStun += value;
	}
	
	public void adjustCurrentBody(int value) {
		currentBody += value;
	}
	
	public void recoveryPhase() {
		currentStun += getRec();
		if (currentStun > stun) {
			currentStun = stun;
		}
	}

	/**
	 * @return the displaySuffix
	 */
	public String getDisplaySuffix(int currPhase) {
		if (displaySuffix != null) {
			return displaySuffix;
		} else {
			String result = null;
			
			if (isUnconscious()) {
				result = "u";
			} else if (isStunned()) {
				result = "c";
			} else if (hasAborted()) {
				result = "a";
			} else if (!hasActed() && !actsInPhase(currPhase)) {
				result = "ha";
			}
			
			if (isFlashed() && result == null) {
				return "(f-" + Integer.toString(getFlashed())+ ")";
			} else if (isFlashed()) {
				return "(" + result + ",f-" + Integer.toString(getFlashed())+ ")";
			} else if (result != null){
				return "(" + result + ")";
			} else {
				return "";
			}
		}
	}

	/**
	 * @param displaySuffix the displaySuffix to set
	 */
	public void setDisplaySuffix(String displaySuffix) {
		if (displaySuffix == null) {
			this.displaySuffix = "";
		} else {
			this.displaySuffix = displaySuffix;
		}
	}

	/**
	 * @return the target
	 */
	public Person getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(Person target) {
		this.target = target;
		// We're changing target, so reset targetName
		this.targetName = null;
	}
	
	public boolean equals(Object o) {
		boolean result = false;
		
		if (o instanceof Person) {
			Person p = (Person)o;
			result = (this.getName().equals(p.getName()) && this.getDisplayName().equals(p.getDisplayName()));
		}
		
		return result;
	}

	public boolean hasActed() {
		return acted;
	}

	public void setActed(boolean acted) {
		this.acted = acted;
	}

	public boolean hasAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		// If you've aborted, that counts as an action
		if (aborted) {
			setActed(aborted);
		}
		this.aborted = aborted;
	}
	
	public boolean isFlashed() {
		return flashed > 0;
	}
	
	public int getFlashed() {
		return flashed;
	}
	
	public void setFlashed(int flashed) {
		this.flashed = flashed;
	}

	public String getTargetName() {
		if (targetName == null && target != null) {
			return target.getDisplayName();
		} 
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public int getPd() {
		return pd;
	}

	public void setPd(int pd) {
		this.pd = pd;
	}

	public int getEd() {
		return ed;
	}

	public void setEd(int ed) {
		this.ed = ed;
	}

	public int getDcv() {
		return dcv;
	}

	public void setDcv(int dcv) {
		this.dcv = dcv;
	}
}