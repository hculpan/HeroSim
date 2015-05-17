/*
 * Created on Dec 10, 2004
 *
 */
package org.culpan.herosim;

import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.culpan.herosim.gui.PhaseTrackerPanel;
import org.culpan.herosim.plugin.LoggingPlugin;
import org.culpan.herosim.plugin.PlayerViewerPlugin;
import org.culpan.herosim.plugin.PluginManager;
import org.culpan.herosim.plugin.bonjour.BonjourMonitorView;
import org.culpan.herosim.plugin.bonjour.BonjourMonitorPlugin;
import org.culpan.herosim.plugin.network.HeroSimNetworkView;
import org.culpan.herosim.plugin.network.NetworkMonitorPlugin;

/**
 * @author CulpanH
 * 
 */
public class HeroSimMain {
	protected static final Logger logger = Logger.getLogger(HeroSimMain.class);

	protected List<String> files = new LinkedList<String>();

	public static JFrame frame;

	protected boolean networkClient = false;

	protected String netClientHostname = null;

	@SuppressWarnings("static-access")
	public Options getOptions() {
		Options result = new Options();

		result.addOption("h", false, "Display help");
		result.addOption(OptionBuilder.withDescription("Run client in window").withLongOpt("window").hasArg(false)
				.create('w'));
		result.addOption(OptionBuilder.withDescription("Start as network client").withLongOpt("net-client").hasArg(
				false).create('n'));
		result.addOption(OptionBuilder.withDescription("Host name (for network client only)").withLongOpt("host")
				.hasArg().withArgName("hostname").create());
		result.addOption(OptionBuilder.withDescription("Geometry of client screen as WxH").withLongOpt("geom").hasArg()
				.withArgName("WxH").create('g'));
		result.addOption(OptionBuilder.withDescription("Show dialog of available servers").withLongOpt("dialog").hasArg(
				false).create('d'));

		return result;
	}

	public boolean processCommandLine(String[] args) {
		boolean result = true;

		try {
			CommandLineParser parser = new PosixParser();
			CommandLine line = parser.parse(getOptions(), args);

			if (line.hasOption('w')) {
				System.setProperty("org.culpan.net-client.mode", "windowed");
			}

			if (line.hasOption('g')) {
				System.setProperty("org.culpan.net-client.geometry", line.getOptionValue('g'));
			}

			System.setProperty("org.culpan.net-client.first-server", line.hasOption('d') ? "false" : "true");

			if (line.hasOption('h')) {
				result = false;
			} else {
				networkClient = line.hasOption('n');

				if (line.hasOption("host")) {
					netClientHostname = line.getOptionValue("host");
				}
			}
		} catch (Throwable e) {
			logger.error(e);
			result = false;
		}

		return result;
	}

	protected void loadPlugins() {
		PluginManager.addPlugin(new LoggingPlugin());
		if (HeroSimProperties.getBooleanProperty("playerView", true)) {
			PluginManager.addPlugin(new PlayerViewerPlugin());
		}

		if (Utils.isMacOs()) {
			PluginManager.addPlugin(new BonjourMonitorPlugin());
		} else {
			PluginManager.addPlugin(new NetworkMonitorPlugin());
		}
	}

	public void run() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			if (networkClient) {
				if (Utils.isMacOs()) {
					System.setProperty("com.apple.mrj.application.apple.menu.about.name", "HeroSim Network Client");
					System.setProperty("apple.laf.useScreenMenuBar", "true");

					BonjourMonitorView bm = new BonjourMonitorView();
					bm.initialize();
				} else {
					HeroSimNetworkView v = new HeroSimNetworkView();
					v.setNetClientHostname(netClientHostname);
					v.initialize();
				}
			} else {
				loadPlugins();

				if (System.getProperty("os.name").equals("Mac OS X")) {
					System.setProperty("com.apple.mrj.application.apple.menu.about.name", "HeroSim");
					System.setProperty("apple.laf.useScreenMenuBar", "true");
				}

				frame = new JFrame();

				final PhaseTrackerPanel v = new PhaseTrackerPanel(frame);

				if (Utils.isMacOs()) {
/*					com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
					app.addApplicationListener(new com.apple.eawt.ApplicationAdapter() {
						public void handleQuit(com.apple.eawt.ApplicationEvent e) {
							v.close();
						}
					});
*/
				}

				frame.setJMenuBar(v.getJMenuBar());
				frame.getContentPane().add(v);
				frame.addWindowListener(v);
				frame.setTitle("HeroSim");
				frame.setSize(new Dimension(700, 600));
				frame.setResizable(false);
				frame.setVisible(true);
				v.setLocation(frame);
			}
		} catch (Exception e) {
			Utils.notifyError(e);
		}

	}

	public void displayUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("herosim", getOptions());
	}

	public static void main(String[] args) {
		HeroSimMain main = new HeroSimMain();
		if (main.processCommandLine(args)) {
			main.run();
		} else {
			main.displayUsage();
		}
	}
}
