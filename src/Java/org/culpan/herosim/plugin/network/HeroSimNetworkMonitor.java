/*
 * Created on Feb 1, 2005
 *
 */
package org.culpan.herosim.plugin.network;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.culpan.herosim.gui.PhaseTrackerPanel;

/**
 * @author CulpanH
 *  
 */
public class HeroSimNetworkMonitor {
    private static Logger logger = Logger.getLogger(HeroSimNetworkMonitor.class);
    
    @SuppressWarnings("static-access")
	protected static Options getOptions() {
    	Options result = new Options();
    	
    	result.addOption(OptionBuilder.hasArg(true).withArgName("port").withDescription("Port to use for connection").withLongOpt("port").create('p'));
    	
    	return result;
    }

    public static void main(String[] args) {
        try {
//            CommandLineParser parser = new PosixParser();
//            CommandLine line = parser.parse(getOptions(), args);
            
/*            int port = NetworkMonitorPlugin.DEFAULT_CLIENT_PORT;
            if (line.hasOption('p')) {
            	port = Integer.parseInt(line.getOptionValue('p'));
            } */
            
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Hero System Phases");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.setProperty("apple.laf.useScreenMenuBar", "true");

            JFrame frame = new JFrame();
            PhaseTrackerPanel v = new PhaseTrackerPanel(frame);

            frame.setJMenuBar(v.getJMenuBar());
            frame.getContentPane().add(v);
            frame.addWindowListener(v);
            frame.setTitle("Hero System Phases");
            frame.setSize(new Dimension(600, 600));
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception e) {
            logger.error(e);
        }
    }
    
    
}