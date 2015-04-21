package org.culpan.herosim.plugin.bonjour;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.MemoryImageSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.culpan.herosim.Hero;
import org.culpan.herosim.Person;
import org.culpan.herosim.Utils;
import org.culpan.herosim.Villain;
import org.culpan.herosim.plugin.PlayerViewerPlugin;
import org.culpan.herosim.plugin.network.NetworkMonitorPlugin;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTargetAdapter;
import org.jdesktop.animation.timing.interpolation.Interpolator;
import org.jdesktop.animation.timing.interpolation.KeyFrames;
import org.jdesktop.animation.timing.interpolation.KeyTimes;
import org.jdesktop.animation.timing.interpolation.KeyValues;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
import org.jdom.Element;

import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.ResolveListener;
import com.apple.dnssd.TXTRecord;

public class BonjourMonitorView extends PlayerViewerPlugin implements ResolveListener, Runnable {
	private final static Logger logger = Logger.getLogger(BonjourMonitorView.class);

	protected Socket socket;

	protected int lastTurn = 0;

	protected int lastPhase = 12;

	JPanel midPanel;

	JPanel mainPanel;
	
	class CustomGlassPane extends JComponent {
		int progress = 0;
		
		final static int BAR_WIDTH = 250;
		
		final static int BAR_HEIGHT = 35;
		
	    private final float[] GRADIENT_FRACTIONS = new float[] {
	        0.0f, 0.499f, 0.5f, 1.0f
	    };

	    private final Color[] GRADIENT_COLORS = new Color[] {
	        Color.GRAY, Color.DARK_GRAY, Color.BLACK, Color.GRAY
	    };

		protected void paintComponent(Graphics g) {
			Rectangle clip = g.getClipBounds();
			
			Graphics2D g2 = (Graphics2D)g;
			
			AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f);
			g2.setComposite(alpha);
			
			g2.setColor(getBackground());
			g2.fillRect(clip.x, clip.y, clip.width, clip.height);
			
			// put text
			
	//		int w = (int)(BAR_WIDTH * ((float) progress / 100.0f));
	//		int h = BAR_HEIGHT;
			
//			Paint gradient = new LinearGradientPaint(x, y, x, y + h, )
		}
	}

	public void initialize() {
		logger.info("Initializing client view");

		final Dimension windowSize;
		if (System.getProperties().containsKey("org.culpan.net-client.geometry")) {
			String[] geoms = System.getProperty("org.culpan.net-client.geometry").split("x");
			if (geoms.length != 2) {
				throw new RuntimeException("Invalid geometry : " + System.getProperty("org.culpan.net-client.geometry"));
			}
			windowSize = new Dimension(Integer.parseInt(geoms[0]), Integer.parseInt(geoms[1]));
		} else {
			windowSize = new Dimension(600, 800);
		}

		final GraphicsDevice gs = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
		if (gs.isFullScreenSupported()) {
			frame = new JFrame(gs.getDefaultConfiguration());
			frame.setTitle("Hero Simulator Player View");
			frame.setGlassPane(new CustomGlassPane());
			
			initializeUi();

			if (System.getProperties().containsKey("org.culpan.net-client.mode")
					&& System.getProperty("org.culpan.net-client.mode").equalsIgnoreCase("windowed")) {
				frame.setSize(windowSize);
				frame.setLocationRelativeTo(null);
			} else {
				frame.setUndecorated(true);
				gs.setFullScreenWindow(frame);
				hideMouseCursor();
			}

			frame.setVisible(true);
		}

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		findServer();
	}
	
	protected void findServer() {
		final DiscoveredInstance di;
		if (System.getProperty("org.culpan.net-client.first-server").equals("true")) {
			di = BonjourBrowser.findFirstServer(BonjourMonitorPlugin.SERVICE_TYPE, frame);
		} else {
			BonjourBrowser bb = new BonjourBrowser(BonjourMonitorPlugin.SERVICE_TYPE, "Select Host");
			bb.setVisible(true);
			di = bb.selectedInstance;
			if (!bb.selected) {
				terminate();
			}
		}

		logger.info("Host selected : domain = " + di.getDomain() + ", name = " + di.getName());

		di.resolve(this);
	}

	protected void addPersonsToPanel(List<Person> chars, JPanel midPanel) {
		if (chars != null) {
			for (Person p : chars) {
				PersonLabel pl = new PersonLabel(p);
				pl.setOpaque(false);
				midPanel.add(pl);
			}

		}
	}
	
	protected PersonLabel findPersonLabel(Person p) {
		PersonLabel result = null;
		for (int i = 0; i < midPanel.getComponentCount(); i++) {
			PersonLabel pl = (PersonLabel)midPanel.getComponent(i);
			if (pl.person.getDisplayName().equals(p.getDisplayName())) {
				result = pl;
				break;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.culpan.herosim.plugin.HeroSimPlugin#changePhase(int, int,
	 *      java.util.List)
	 */
	public void changePhase(final int turn, final int phase, final List<Person> chars) {
		logger.debug("changePhase called");

		if (turn != lastTurn || phase != lastPhase || midPanel.getComponentCount() == 0) {
			// if new phase or no one in phase, slide in new panel
			final JPanel oldMainPanel = mainPanel;
			mainPanel = newMainPanel(chars);
			frame.getContentPane().add(mainPanel);

			mainPanel.setSize(oldMainPanel.getSize());
			mainPanel.setLocation(oldMainPanel.getX() + oldMainPanel.getWidth(), oldMainPanel.getY());

			mainPanel.validate();
			
			layoutPanel(midPanel, chars, null, null);

			setTurnPhase(turn, phase);

			Animator leftAnimator = new Animator(500);
			leftAnimator.setAcceleration(0.3f);
			leftAnimator.setDeceleration(0.2f);
			leftAnimator.setEndBehavior(Animator.EndBehavior.HOLD);

			KeyTimes keyTimes = new KeyTimes(0, 1);

			KeyValues<Point> keyValuesNew = KeyValues.create(new Point(mainPanel.getX(), mainPanel.getY()), new Point(
					oldMainPanel.getX(), oldMainPanel.getY()));
			KeyValues<Point> keyValuesOld = KeyValues.create(new Point(oldMainPanel.getX(), oldMainPanel.getY()),
					new Point(oldMainPanel.getX() - oldMainPanel.getWidth(), oldMainPanel.getY()));
			if (turn < lastTurn || phase < lastPhase % 12 || (turn == lastTurn && phase == 12 && lastPhase != 12)) {
				keyValuesNew = KeyValues.create(new Point(oldMainPanel.getX() - oldMainPanel.getWidth(), oldMainPanel
						.getY()), new Point(oldMainPanel.getX(), oldMainPanel.getY()));
				keyValuesOld = KeyValues.create(new Point(oldMainPanel.getX(), oldMainPanel.getY()), new Point(
						oldMainPanel.getX() + oldMainPanel.getWidth(), oldMainPanel.getY()));
			}
			KeyFrames keyFramesNew = new KeyFrames(keyValuesNew, keyTimes, (Interpolator) null);
			KeyFrames keyFramesOld = new KeyFrames(keyValuesOld, keyTimes, (Interpolator) null);

			leftAnimator.addTarget(new PropertySetter(mainPanel, "location", keyFramesNew));
			leftAnimator.addTarget(new PropertySetter(oldMainPanel, "location", keyFramesOld));
			leftAnimator.addTarget(new TimingTargetAdapter() {
				public void begin() {
					logger.debug("Starting animation...");
				}

				public void end() {
					frame.getContentPane().remove(oldMainPanel);
					logger.debug("Ending animation...");
				}
			});

			leftAnimator.start();

			lastTurn = turn;
			lastPhase = phase;
		} else {
			// same phase, animate moves
			Animator leftAnimator = new Animator(300);
			leftAnimator.setAcceleration(0.3f);
			leftAnimator.setDeceleration(0.2f);
			leftAnimator.setEndBehavior(Animator.EndBehavior.HOLD);

			KeyTimes keyTimes = new KeyTimes(0, 1);
			
			final List<PersonLabel> deleted = layoutPanel(midPanel, chars, leftAnimator, keyTimes);
			
			leftAnimator.addTarget(new TimingTargetAdapter() {
				public void end() {
					for (PersonLabel pl : deleted) {
						midPanel.remove(pl);
					}
				}
			});

			leftAnimator.start();
		}
	}
	
	protected List<PersonLabel> layoutPanel(Container midPanel, List<Person> chars, Animator anim, KeyTimes keyTimes) {
		List<PersonLabel> result = new ArrayList<PersonLabel>();
		
		List<PersonLabel> acted = new ArrayList<PersonLabel>();
		List<PersonLabel> notActed = new ArrayList<PersonLabel>();
		List<PersonLabel> stunned = new ArrayList<PersonLabel>();

		Map<PersonLabel, Boolean> deleted = new HashMap<PersonLabel, Boolean>();
		for (int i = 0; i < midPanel.getComponentCount(); i++) {
			deleted.put((PersonLabel)midPanel.getComponent(i), null);
		}
		
		for (Person p : chars) {
			PersonLabel pl = findPersonLabel(p);
			if (pl == null) { // new person
				PersonLabel newpl = new PersonLabel(p);
				newpl.setOpaque(false);
				newpl.setVisible(true);
				midPanel.add(newpl);
				if (p.hasActed()) {
					acted.add(newpl);
				} else if (p.isStunned() || p.isUnconscious()) {
					stunned.add(newpl);
				} else {
					notActed.add(notActed.size(), newpl);
				}
			} else { // existing person
				pl.person = p;
				if (p.isStunned() || p.isUnconscious()) {
					stunned.add(pl);
				} else if (p.hasActed()) {
					acted.add(pl);
				} else {
					notActed.add(notActed.size(), pl);
				}
				deleted.remove(pl);
			}
		}

		positionPersons((midPanel.getWidth() + midPanel.getInsets().left) / 6, acted, anim, keyTimes);
		positionPersons((midPanel.getWidth() + midPanel.getInsets().left) / 2, notActed, anim, keyTimes);
		positionPersons(midPanel.getWidth() - ((midPanel.getWidth() + midPanel.getInsets().left) / 6), stunned, anim, keyTimes);
		
		for (PersonLabel pl : deleted.keySet()) {
			result.add(pl);
			
			KeyValues<Point> keyValues = KeyValues.create(new Point(pl.getX(), pl.getY()), new Point(pl.getX() + (pl.getWidth() / 2), pl.getY() + (pl.getHeight() /2 )));
			KeyFrames keyFrames = new KeyFrames(keyValues, keyTimes, (Interpolator) null);
			anim.addTarget(new PropertySetter(pl, "location", keyFrames));

			KeyValues<Dimension> keyValuesSize = KeyValues.create(new Dimension(pl.getWidth(), pl.getHeight()), new Dimension(0, 0));
			KeyFrames keyFramesSize = new KeyFrames(keyValuesSize, keyTimes, (Interpolator) null);
			anim.addTarget(new PropertySetter(pl, "size", keyFramesSize));
		}
		
		return result;
	}
	
	protected void positionPersons(int centerX, List<PersonLabel> acted, Animator anim, KeyTimes keyTimes) {
		Insets insets = midPanel.getInsets();

		int numComponents = (midPanel.getComponentCount() > 11 ? midPanel.getComponentCount() : 11);
		int heightBuffer = (int) (midPanel.getHeight() * .025);
		int containerHeight = midPanel.getHeight() - (insets.bottom + insets.top) - (heightBuffer * 2);
		int heightInterval = containerHeight / numComponents;
		int containerWidth = midPanel.getWidth() - (insets.left + insets.right);
		int componentWidth = containerWidth / 3;
		
		int index = 0;
		for (PersonLabel pl : acted) {
			int newHeight = heightInterval;
			int newWidth = componentWidth;
			int newX = centerX - (newWidth / 2);
			int newY = heightBuffer + (index * heightInterval);

			if (newX != pl.getX() || newY != pl.getY()) {
				if (anim == null || keyTimes == null) {
					pl.setLocation(new Point(newX, newY));
				} else {
					KeyValues<Point> keyValues = KeyValues.create(new Point(pl.getX(), pl.getY()), new Point(newX, newY));
					KeyFrames keyFrames = new KeyFrames(keyValues, keyTimes, (Interpolator) null);
					anim.addTarget(new PropertySetter(pl, "location", keyFrames));
				}
			} 
			
			if (newHeight != pl.getHeight() || newWidth != pl.getWidth()) {
				if (anim == null || keyTimes == null) {
					pl.setSize(newWidth, newHeight);
				} else {
					KeyValues<Dimension> keyValuesSize = KeyValues.create(new Dimension(pl.getWidth(), pl.getHeight()), new Dimension(newWidth, newHeight));
					KeyFrames keyFramesSize = new KeyFrames(keyValuesSize, keyTimes, (Interpolator) null);
					anim.addTarget(new PropertySetter(pl, "size", keyFramesSize));
				}
				
			}
			index++;
		}
	}

	class MidPanelLayout implements LayoutManager {
		public MidPanelLayout() {
		}

		/* Required by LayoutManager. */
		public void addLayoutComponent(String name, Component comp) {
		}

		/* Required by LayoutManager. */
		public void removeLayoutComponent(Component comp) {
		}

		/* Required by LayoutManager. */
		public Dimension preferredLayoutSize(Container parent) {
			return parent.getPreferredSize();
		}

		/* Required by LayoutManager. */
		public Dimension minimumLayoutSize(Container parent) {
			return parent.getMinimumSize();
		}

		/* Required by LayoutManager. */
		/*
		 * This is called when the panel is first displayed, and every time its
		 * size changes. Note: You CAN'T assume preferredLayoutSize or
		 * minimumLayoutSize will be called -- in the case of applets, at least,
		 * they probably won't be.
		 */
		public void layoutContainer(Container parent) {
			// I'm not doing anything here, because we will manually
			// layout components
			return;
		}
	}

	protected JPanel newMidPanel(List<Person> chars) {
		JPanel result = new JPanel();

		result.setBackground(Color.white);
		result.setBorder(BorderFactory.createLineBorder(Color.black));
		result.setLayout(new MidPanelLayout());

		return result;
	}

	protected JPanel newMainPanel(List<Person> chars) {
		JPanel mainPanel = new JPanel(new BorderLayout());

		JPanel leftPanel = new JPanel(new FlowLayout());
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		leftPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		mainPanel.add(leftPanel, BorderLayout.WEST);

		phaseLabel = new JLabel("Phase 12");
		phaseLabel.setFont(phaseLabel.getFont().deriveFont(32f));
		phaseLabel.setForeground(Color.BLUE);
		phaseLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		phaseLabel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
		// phaseLabel.setHorizontalAlignment(JLabel.CENTER);
		phaseLabel.setPreferredSize(new Dimension(250, 75));
		leftPanel.add(phaseLabel);

		midPanel = newMidPanel(null);
		mainPanel.add(midPanel, BorderLayout.CENTER);

		turnLabel = new JLabel("Turn 1");
		turnLabel.setFont(phaseLabel.getFont());
		turnLabel.setForeground(Color.BLUE);
		turnLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		turnLabel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
		leftPanel.add(turnLabel);

		addPersonsToPanel(chars, midPanel);

		return mainPanel;
	}

	protected void initializeUi() {
		mainPanel = newMainPanel(null);
		frame.getContentPane().add(mainPanel);
	}

	protected void hideMouseCursor() {
		int[] pixels = new int[16 * 16];
		Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
		Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0),
				"invisibleCursor");
		frame.setCursor(transparentCursor);
	}

	public void terminate() {
		super.terminate();

		System.exit(0);
	}

	public void operationFailed(DNSSDService arg0, int arg1) {
		logger.error("Operation failed; errCode = " + arg1);
		arg0.stop();
		terminate();
	}

	public void run() {
		while (socket.isConnected()) {
			try {
				BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				String msg = r.readLine();
				logger.debug("Got message");
				
				if (msg == null || msg.trim().length() == 0) {
					logger.debug("Lost connection...I think");
					try {
						socket.close();
					} catch (IOException e) {
						logger.debug("Got exception while closing socket : " + e.getLocalizedMessage());
					}
					findServer();
					break;
				}
					
				String[] fields = StringUtils.split(msg, "|");
				if (fields == null || fields.length == 0) {
					continue;
				}

				if (fields[0].equalsIgnoreCase("phase")) {
					logger.debug("Got phase message");

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
			} catch (Exception e) {
				logger.error("Got exception : " + e.getLocalizedMessage());
				try {
					socket.close();
				} catch (IOException ex) {
					logger.error("Unable to close channel : " + ex.getLocalizedMessage());
				}
			}
		}
	}

	public void serviceResolved(DNSSDService resolver, int flags, int ifIndex, String name, String hostName, int port,
			TXTRecord txtRecord) {
		logger.info("Service resolved successfully");

		try {
			InetAddress socketAddress = InetAddress.getByName(hostName);
			logger.debug("Setting socket to host " + hostName);
			socket = new Socket(socketAddress, port);
			new Thread(this).start();

			resolver.stop();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getLocalizedMessage());
			terminate();
		}
	}

}
