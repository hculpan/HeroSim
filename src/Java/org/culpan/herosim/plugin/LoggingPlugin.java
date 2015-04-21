/*
 * Created on Feb 1, 2005
 *
 */
package org.culpan.herosim.plugin;

import org.apache.log4j.Logger;

/**
 * @author CulpanH
 *  
 */
public class LoggingPlugin implements HeroSimPlugin {
    protected Logger logger = Logger.getLogger(LoggingPlugin.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.culpan.herosim.plugin.HeroSimPlugin#initialize()
     */
    public void initialize() {
        logger.info("Initialize called");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.culpan.herosim.plugin.HeroSimPlugin#terminate()
     */
    public void terminate() {
        logger.info("Terminate called");
    }
    
    /* (non-Javadoc)
	 * @see org.culpan.herosim.plugin.HeroSimPlugin#event(java.lang.String, java.lang.Object)
	 */
	public void event(String eventName, Object data) {
		if (data != null) {
			logger.info(eventName + " : " + data.toString());
		} else {
			logger.info(eventName);
		}
	}
}