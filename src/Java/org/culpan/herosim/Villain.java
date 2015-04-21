/*
 * Created on Dec 10, 2004
 *
 */
package org.culpan.herosim;

import org.jdom.Element;

/**
 * @author CulpanH
 *
 */
public class Villain extends Person {
	public Villain() {
		super();
	}
	
    public Villain(String name, int spd, int dex) {
    	super(name, spd, dex);
    }
    
	/* (non-Javadoc)
	 * @see org.culpan.herosim.Person#getRootElement()
	 */
	protected Element getRootElement() {
		return new Element("villain");
	}

	/* (non-Javadoc)
	 * @see org.culpan.herosim.Person#copy()
	 */
	public Person copy() {
		Person result = new Villain();
		copyDataTo(result);
		return result;
	}
}
