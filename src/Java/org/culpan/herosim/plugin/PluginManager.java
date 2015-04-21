/*
 * Created on Feb 1, 2005
 *
 */
package org.culpan.herosim.plugin;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.culpan.herosim.Person;

/**
 * @author CulpanH
 *
 */
public class PluginManager {
	private final static Logger logger = Logger.getLogger(PluginManager.class);
	
	public static class PhaseReport {
		public int turn;
		public int phase;
		public List<Person> chars;
	}
	
    protected static PluginManager pluginManager = new PluginManager();
    
    protected List<HeroSimPlugin> plugins = new LinkedList<HeroSimPlugin>();
    
    public static HeroSimPlugin findFirstPlugin(Class<HeroSimPlugin> pluginClass) {
    	HeroSimPlugin result = null;
    	
    	for (HeroSimPlugin c : pluginManager.plugins) {
    		if (c.getClass() == pluginClass) {
    			result = c;
    			break;
    		}
    	}
    	
    	return result;
    }
    
    public static void addPlugin(HeroSimPlugin plugin) {
        pluginManager.plugins.add(plugin);
        plugin.initialize();
    }
    
    public static void removePlugin(HeroSimPlugin plugin) {
        if (pluginManager.plugins.remove(plugin)) {
            plugin.terminate();
        }
    }
    
    @SuppressWarnings("unchecked")
	public static void removePlugin(Class pluginClass) {
    	HeroSimPlugin p = findFirstPlugin(pluginClass);
    	while (p != null) {
    		removePlugin(p);
    		p = findFirstPlugin(pluginClass);
    	}
    }
    
    public static void terminate() {
    	logger.debug("Sending terminate to plugins");
        for (HeroSimPlugin plugin : pluginManager.plugins) {
            plugin.terminate();
        }
    }
    
    public static void personAdded(Person p) {
        for (HeroSimPlugin plugin : pluginManager.plugins) {
            plugin.event(HeroSimPlugin.PERSON_ADD, p);
        }
    }
    
    public static void personRemoved(Person p) {
        for (HeroSimPlugin plugin : pluginManager.plugins) {
            plugin.event(HeroSimPlugin.PERSON_DEL, p);
        }
    }
    
    public static void changePhase(int turn, int phase, List<Person> chars) {
        for (HeroSimPlugin plugin : pluginManager.plugins) {
            PhaseReport pr = new PhaseReport();
            pr.turn = turn;
            pr.phase = phase;
            pr.chars = chars;
            plugin.event(HeroSimPlugin.CHANGE_PHASE, pr);
        }
    }
}
