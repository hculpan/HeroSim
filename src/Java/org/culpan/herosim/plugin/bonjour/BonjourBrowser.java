package org.culpan.herosim.plugin.bonjour;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.apple.dnssd.BrowseListener;
import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDException;
import com.apple.dnssd.DNSSDService;

public class BonjourBrowser extends JDialog implements BrowseListener {
	private final static Logger logger = Logger.getLogger(BonjourBrowser.class);

	protected DNSSDService service;

	protected DefaultListModel listModel = new DefaultListModel();

	protected JList list;

	public boolean selected = false;

	public DiscoveredInstance selectedInstance;

	protected String serviceType;

	public BonjourBrowser(String serviceType, String title) {
		this.serviceType = serviceType;

		setTitle(title);
		setSize(400, 600);
		setModal(true);
		setLocationRelativeTo(null);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				terminate();
			}
		});

		try {
			service = DNSSD.browse(serviceType, this);
		} catch (DNSSDException e) {
			logger.error(e.getLocalizedMessage());
		}

		setLayout(new BorderLayout());

		JPanel btnPanel = new JPanel();
		btnPanel.add(new JButton(new AbstractAction("Ok") {
			public void actionPerformed(ActionEvent e) {
				if (list.getSelectedIndex() > -1) {
					selectedInstance = (DiscoveredInstance) listModel.getElementAt(list.getSelectedIndex());
					selected = true;
					terminate();
					setVisible(false);
				}
			}
		}));
		btnPanel.add(new JButton(new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				selected = false;
				terminate();
				setVisible(false);
			}
		}));

		add(btnPanel, BorderLayout.SOUTH);

		list = new JList(listModel);
		add(new JScrollPane(list), BorderLayout.CENTER);
	}
	
	static class FirstServiceBrowseListener implements BrowseListener {
		public DiscoveredInstance result = null;
		
		protected String serviceType;
		
		public FirstServiceBrowseListener(String serviceType) {
			this.serviceType = serviceType;
		}
		
		public void operationFailed(DNSSDService browser, int errCode) {
			logger.error("Operation failed; errCode = " + errCode);
			throw new RuntimeException("Unable to find first server");
		}

		public void serviceLost(DNSSDService browser, int flags, int ifIndex, String name, String regType,
				String domain) {
			// ignore
		}

		public void serviceFound(DNSSDService browser, int flags, final int ifIndex, final String name,
				String regType, final String domain) {
			result = new DiscoveredInstance(serviceType, ifIndex, name, domain);
		}
	}

	public static DiscoveredInstance findFirstServer(String serviceType, JFrame parent) {
		logger.debug("Finding server...");
		
		parent.getGlassPane().setVisible(true);
		
		JDialog statWindow = new JDialog(parent);
		statWindow.setTitle("Searching...");
		statWindow.setResizable(false);
		statWindow.setUndecorated(true);
		statWindow.setSize(300, 55);
		statWindow.setLocationRelativeTo(parent);
		statWindow.getContentPane().setLayout(new BorderLayout());
		JLabel label = new JLabel("Searching for server...");
		label.setHorizontalAlignment(JLabel.CENTER);
		JProgressBar pBar = new JProgressBar();
		pBar.setIndeterminate(true);
		statWindow.getContentPane().add(label, BorderLayout.NORTH);
		statWindow.getContentPane().add(pBar, BorderLayout.CENTER);
		
		statWindow.setVisible(true);
		
		DNSSDService service = null;
		FirstServiceBrowseListener bl = new FirstServiceBrowseListener(serviceType);
		try {
			service = DNSSD.browse(serviceType, bl);
		} catch (DNSSDException e) {
			logger.error(e.getLocalizedMessage());
		}

		try {
			while (bl.result == null) {
				Thread.sleep(250);
			}
		} catch (InterruptedException e) {
		}
		
		service.stop();
		
		statWindow.setVisible(false);
		statWindow.dispose();
		parent.getGlassPane().setVisible(false);
		logger.debug("Server found.");

		return bl.result;
	}

	public void serviceFound(DNSSDService browser, int flags, final int ifIndex, final String name, String regType,
			final String domain) {
		logger.info("found service");
		logger.info("  flags   : " + flags);
		logger.info("  ifIndex : " + ifIndex);
		logger.info("  name    : " + name);
		logger.info("  regType : " + regType);
		logger.info("  domain  : " + domain);

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					listModel.addElement(new DiscoveredInstance(serviceType, ifIndex, name, domain));
				}
			});
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	public void serviceLost(DNSSDService browser, int flags, int ifIndex, String name, String regType, String domain) {
		logger.info("lost service");
		logger.info("  flags   : " + flags);
		logger.info("  ifIndex : " + ifIndex);
		logger.info("  name    : " + name);
		logger.info("  regType : " + regType);
		logger.info("  domain  : " + domain);

		final DiscoveredInstance di = new DiscoveredInstance(serviceType, ifIndex, name, domain);

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					String name = di.toString();
					for (int i = 0; i < listModel.size(); i++) {
						if (listModel.getElementAt(i).toString().equals(name)) {
							listModel.removeElementAt(i);
							return;
						}

					}
				}
			});
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	public void operationFailed(DNSSDService browser, int errCode) {
		logger.error("Browse operation failed; errCode = " + errCode);
		terminate();
		setVisible(false);
	}

	public void terminate() {
		if (service != null) {
			logger.info("Terminating browser service");
			service.stop();
		}
	}

}
