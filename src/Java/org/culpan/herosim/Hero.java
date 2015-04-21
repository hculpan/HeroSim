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
public class Hero extends Person {
	public Hero() {
		super();
	}
	
    public Hero(String name, int spd, int dex) {
    	super(name, spd, dex);
    }
    
	/* (non-Javadoc)
	 * @see org.culpan.herosim.Person#getRootElement()
	 */
	protected Element getRootElement() {
		return new Element("hero");
	}
	
	/* (non-Javadoc)
	 * @see org.culpan.herosim.Person#copy()
	 */
	public Person copy() {
		Person result = new Hero();
		copyDataTo(result);
		return result;
	}
}
