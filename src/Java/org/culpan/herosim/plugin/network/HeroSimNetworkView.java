package org.culpan.herosim.plugin.network;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.culpan.herosim.Hero;
import org.culpan.herosim.Person;
import org.culpan.herosim.Utils;
import org.culpan.herosim.Villain;
import org.culpan.herosim.plugin.PlayerViewerPlugin;
import org.jdom.Element;

public class HeroSimNetworkView extends PlayerViewerPlugin {
	ServerSocket ss;

	String netClientHostname;

	public String getNetClientHostname() {
		return netClientHostname;
	}

	public void setNetClientHostname(String netClientHostname) {
		this.netClientHostname = netClientHostname;
	}

	protected InetAddress getHostInetAddress() throws UnknownHostException {
		return InetAddress.getLocalHost();
	}

	public void initialize() {
		GraphicsDevice gs = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		if (gs.isFullScreenSupported()) {
	        frame = new JFrame(gs.getDefaultConfiguration());
	        frame.setTitle("Hero Simulator Player View");
			frame.setUndecorated(true);

	        initializeUi();

	        frame.setVisible(true);
	        
	        gs.setFullScreenWindow(frame);
		}
		
		list.getInputMap().put(KeyStroke.getKeyStroke('Q'), "quit");
		list.getInputMap().put(KeyStroke.getKeyStroke('q'), "quit");
		list.getActionMap().put("quit", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				terminate();
			}
		} );
		
		final String hostName = JOptionPane.showInputDialog(frame, "Server Host Name: ", "Server Host",
				JOptionPane.QUESTION_MESSAGE);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		try {
			new Thread() {
				public void run() {
					try {
						Socket s = new Socket(InetAddress.getByName(hostName), NetworkMonitorPlugin.DEFAULT_SERVER_PORT);
						String msgAdd = "ADD";
						BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
						out.write(msgAdd);
						out.newLine();
						out.flush();

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								frame.setTitle(frame.getTitle() + " - " + hostName);
							}
						});
						
						while (s.isConnected()) {
							BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
							String msg = r.readLine();

							String[] fields = StringUtils.split(msg, "|");
							if (fields[0].equalsIgnoreCase("phase")) {
								final int turn = Integer.parseInt(fields[1]);
								final int phase = Integer.parseInt(fields[2]);

								final List<Person> chars = new ArrayList<Person>();
								for (int i = 3; i < fields.length; i++) {
									Element pElement = Utils.loadXml(fields[i]);
									Person p = null;
									if (pElement.getText().equalsIgnoreCase("hero")) {
										p = new Hero();
									} else {
										p = new Villain();
									}
									p.initFromXml(pElement);
									chars.add(p);
								}

								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										changePhase(turn, phase, chars);
									}
								});
							} else if (fields[0].equalsIgnoreCase(NetworkMonitorPlugin.TERMINATE)) {
								terminate();
							}
						}
					} catch (Exception e) {
						throw new RuntimeException("Unable to init server socket", e);
					}

				}
			}.start();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Unable to initialize network client", "Connection Error",
					JOptionPane.ERROR_MESSAGE);
			terminate();
		}
	}

	public void terminate() {
		super.terminate();
		System.exit(0);
	}
}
