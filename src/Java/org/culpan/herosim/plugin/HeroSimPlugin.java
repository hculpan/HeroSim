/*
 * Created on Feb 1, 2005
 *
 */
package org.culpan.herosim.plugin;


/**
 * @author CulpanH
 *
 */
public interface HeroSimPlugin {
	final public static String PERSON_ADD = "person-added";
	final public static String PERSON_DEL = "person-removed";
	final public static String CHANGE_PHASE = "change-phase";
	
    public void initialize();
    public void terminate();
    
    public void event(String eventName, Object data);
}
