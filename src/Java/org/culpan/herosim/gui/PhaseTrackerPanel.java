/*
 * Created on Dec 10, 2004
 *
 */
package org.culpan.herosim.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.culpan.herosim.Hero;
import org.culpan.herosim.HeroSimMain;
import org.culpan.herosim.HeroSimProperties;
import org.culpan.herosim.ImpairmentChart;
import org.culpan.herosim.Person;
import org.culpan.herosim.PersonManager;
import org.culpan.herosim.Utils;
import org.culpan.herosim.Villain;
import org.culpan.herosim.gui.dice.NormalDamageDialog;
import org.culpan.herosim.plugin.PlayerViewerPlugin;
import org.culpan.herosim.plugin.PluginManager;
import org.jdom.Element;

/**
 * @author CulpanH
 * 
 */
public class PhaseTrackerPanel extends JPanel implements WindowListener, PersonManager {
	/** Used to mark if list is those who act only in phase */
	protected final static int PHASE_LIST = 0;

	/** Used to mark if list is all those in combat */
	protected final static int ALL_CHARS_LIST = 1;

	protected static final Logger logger = Logger.getLogger(PhaseTrackerPanel.class);

	protected DefaultListModel listModel;

	protected JList list;

	protected DefaultTableModel tableModel;

	protected JTable table;

	protected List<Person> chars = new ArrayList<Person>();

	protected JFrame frame;

	protected JLabel currPhaseLabel;

	protected JLabel currTurnLabel;

	/** This is the current display phase */
	protected int currPhase = 12;

	/** This is the current display turn */
	protected int currTurn = 1;

	/** This is the actual active phase */
	protected int activePhase = 12;

	/** This is the actual active turn */
	protected int activeTurn = 1;

	protected boolean skipEmptySegments = true;

	protected boolean rollImpairment = HeroSimProperties.getBooleanProperty("rollImpairment", false);

	protected boolean playerView = HeroSimProperties.getBooleanProperty("playerView", true);

	protected Person currentPerson;
	
	/** When set to true, will suppress updatePhaseDisplay from doing anything */
	protected boolean massUpdate = false;

	protected final Action clearTargetAction = new AbstractAction() {
		{
			putValue(Action.MNEMONIC_KEY, new Integer('C'));
			putValue(Action.NAME, "Clear Target");
		}

		public void actionPerformed(ActionEvent e) {
			if (currentPerson != null && currentPerson.getTarget() != null) {
				currentPerson.setTarget(null);
				refreshViews();
			}
		}
	};
	
	protected final Action personInfoAction = new AbstractAction() {
		{
			putValue(Action.MNEMONIC_KEY, new Integer('I'));
			putValue(Action.NAME, "Person Info");
		}

		public void actionPerformed(ActionEvent e) {
			if (currentPerson != null) {
				displayInfo(currentPerson);
			}
		}
	};

 

	protected final Action addPersonAction = new AbstractAction() {
		{
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('A', Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(Action.MNEMONIC_KEY, new Integer('A'));
			putValue(Action.NAME, "Add Person");
		}

		public void actionPerformed(ActionEvent e) {
			AddPersonDialog d = new AddPersonDialog(HeroSimMain.frame);
			d.setVisible(true);
			if (d.result != null) {
				if (d.resultCount > 1) {
					massUpdate = true;
					for (int i = 0; i < d.resultCount; i++) {
						Person p = d.result.copy();
						p.setDisplayName(d.result.getDisplayName() + " " + Integer.toString(i + 1));
						add(p);
					}
					massUpdate = false;
					updatePhaseDisplay();
				} else {
					add(d.result);
				}
			}
		}
	};

	protected final Action removePersonAction = new AbstractAction() {
		{
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('D', Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(Action.MNEMONIC_KEY, new Integer('D'));
			putValue(Action.NAME, "Delete Person");
		}

		public void actionPerformed(ActionEvent e) {
			if (currentPerson != null) {
				remove(currentPerson);
				currentPerson = null;
			} else if (list.getSelectedIndex() > -1) {
				remove((Person) listModel.elementAt(list.getSelectedIndex()));
			}
		}
	};

	protected final Action editPersonAction = new AbstractAction() {
		{
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('E', Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(Action.MNEMONIC_KEY, new Integer('E'));
			putValue(Action.NAME, "Edit Person");
		}

		public void actionPerformed(ActionEvent e) {
			AddPersonDialog d = new AddPersonDialog(HeroSimMain.frame, currentPerson);
			d.setVisible(true);
			if (d.result != null) {
				remove(currentPerson);
				add(d.result);
			}

			updatePhaseDisplay();
		}
	};

	protected final Action removeAllAction = new AbstractAction() {
		{
			putValue(Action.MNEMONIC_KEY, new Integer('l'));
			putValue(Action.NAME, "Delete all");
		}

		public void actionPerformed(ActionEvent e) {
			massUpdate = true;
			while (chars.size() > 0) {
				Person p = chars.get(0);
				remove(p);
			}
			massUpdate = false;
			updatePhaseDisplay();
		}
	};

	protected final Action removeAllNonPlayerAction = new AbstractAction() {
		{
			putValue(Action.MNEMONIC_KEY, new Integer('n'));
			putValue(Action.NAME, "Delete all non-players");
		}

		public void actionPerformed(ActionEvent e) {
			int index = 0;
			massUpdate = true;
			while (index < chars.size()) {
				Person p = chars.get(index);
				if (p instanceof Villain) {
					remove(p);
				} else {
					index++;
				}
			}
			massUpdate = false;
			updatePhaseDisplay();
		}
	};

	protected final Action openAction = new AbstractAction() {
		{
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(Action.MNEMONIC_KEY, new Integer('O'));
			putValue(Action.NAME, "Open");
		}

		public void actionPerformed(ActionEvent e) {
			openFile();
		}
	};

	protected final Action saveAction = new AbstractAction() {
		{
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(Action.MNEMONIC_KEY, new Integer('S'));
			putValue(Action.NAME, "Save");
		}

		public void actionPerformed(ActionEvent e) {
			saveFile();
		}
	};

	protected final Action nextAction = new AbstractAction() {
		{
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('N', Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(Action.MNEMONIC_KEY, new Integer('N'));
			putValue(Action.NAME, "Next");
		}

		public void actionPerformed(ActionEvent e) {
			nextPhase();
		}
	};

	protected final Action prevAction = new AbstractAction() {
		{
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('P', Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(Action.MNEMONIC_KEY, new Integer('P'));
			putValue(Action.NAME, "Previous");
		}

		public void actionPerformed(ActionEvent e) {
			prevPhase();
		}
	};

	protected final Action quitAction = new AbstractAction() {
		{
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(Action.MNEMONIC_KEY, new Integer('Q'));
			putValue(Action.NAME, "Quit");
		}

		public void actionPerformed(ActionEvent e) {
			close();
		}
	};

	protected final Action skipSegmentsAction = new AbstractAction() {
		{
			putValue(Action.MNEMONIC_KEY, new Integer('S'));
			putValue(Action.NAME, "Skip Empty Segments");
		}

		public void actionPerformed(ActionEvent e) {
			skipEmptySegments = !skipEmptySegments;
		}
	};

	protected final Action playerViewAction = new AbstractAction() {
		{
			putValue(Action.MNEMONIC_KEY, new Integer('P'));
			putValue(Action.NAME, "Show Player View");
		}

		public void actionPerformed(ActionEvent e) {
			playerView = !playerView;

			if (!playerView) {
				PluginManager.removePlugin(PlayerViewerPlugin.class);
			} else {
				PluginManager.addPlugin(new PlayerViewerPlugin());
			}
		}
	};

	protected final Action rollImpairmentAction = new AbstractAction() {
		{
			putValue(Action.MNEMONIC_KEY, new Integer('I'));
			putValue(Action.NAME, "Roll Impairment");
		}

		public void actionPerformed(ActionEvent e) {
			rollImpairment = !rollImpairment;
		}
	};

	protected final Action changeSpeedAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Change Speed");
		}

		public void actionPerformed(ActionEvent e) {
			if (currentPerson != null) {
				String newValue = JOptionPane.showInputDialog(null, "New speed", "Change Speed",
						JOptionPane.QUESTION_MESSAGE);
				int newSpeed = Utils.parseInt(newValue);
				if (newSpeed > 0 && newSpeed < 13) {
					currentPerson.setSpeed(newSpeed);
				}
			}
		}
	};

	protected final Action flashAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Flashed");
		}

		public void actionPerformed(ActionEvent e) {
			if (currentPerson != null) {
				String newValue = JOptionPane.showInputDialog(null, "Segments of Flash", "Set Flashed",
						JOptionPane.QUESTION_MESSAGE);
				if (newValue != null && newValue.trim().length() > 0) {
					currentPerson.setFlashed(Integer.parseInt(newValue));
					refreshViews();
				}
			}
		}
	};

	protected final Action recoverAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Recover");
		}

		public void actionPerformed(ActionEvent e) {
			if (currentPerson != null) {
				if (!currentPerson.actsInPhase(currPhase)) {
					JOptionPane.showMessageDialog(frame, "Error : " + currentPerson.getDisplayName()
							+ " does not act in this phase", "Recover", JOptionPane.ERROR_MESSAGE);
				} else if (currentPerson.isStunned() || currentPerson.isUnconscious()) {
					JOptionPane.showMessageDialog(frame, "Error : " + currentPerson.getDisplayName()
							+ " is stunned or unconscious", "Recover", JOptionPane.ERROR_MESSAGE);
				} else if (currentPerson.hasActed()) {
					JOptionPane.showMessageDialog(frame, "Error : " + currentPerson.getDisplayName()
							+ " has already acted", "Recover", JOptionPane.ERROR_MESSAGE);
				} else {
					currentPerson.recoveryPhase();
					currentPerson.setActed(true);
					refreshViews();
				}
			}
		}
	};

	protected final Action damagePersonAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Damage Person");
		}

		public void actionPerformed(ActionEvent e) {
			if (currentPerson != null) {
				String newValue = JOptionPane.showInputDialog(null, "BODY Damage", "Damage "
						+ currentPerson.getDisplayName(), JOptionPane.QUESTION_MESSAGE);
				int bodyDmg = Utils.parseInt(newValue, -1);
				if (bodyDmg > -1) {
					currentPerson.setCurrentBody(currentPerson.getCurrentBody() - bodyDmg);

					newValue = JOptionPane.showInputDialog(null, "STUN Damage", "Damage "
							+ currentPerson.getDisplayName(), JOptionPane.QUESTION_MESSAGE);
					int stunDmg = Utils.parseInt(newValue, -1);
					if (stunDmg > -1) {
						currentPerson.setCurrentStun(currentPerson.getCurrentStun() - stunDmg);
						if (stunDmg > currentPerson.getCon() && !currentPerson.isUnconscious()
								&& !currentPerson.isStunned()) {
							int c = JOptionPane.showConfirmDialog(HeroSimMain.frame, "Damage exceeds CON.\nIs "
									+ currentPerson.getDisplayName() + " stunned?", "CON Stun Confirmation",
									JOptionPane.YES_NO_CANCEL_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								setPersonStunned(currentPerson);
							}
						} else if (currentPerson.isUnconscious()) {
							currentPerson.setActed(true);
						}
					}

					if (bodyDmg >= currentPerson.getBody() / 2 && rollImpairment) {
						rollImpairmentDamage();
					}
				}

				refreshViews();
			}
		}
	};

	protected final Action healPersonAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Heal Person");
		}

		public void actionPerformed(ActionEvent e) {
			if (currentPerson != null) {
				String newValue = JOptionPane.showInputDialog(null, "BODY healed", "Heal "
						+ currentPerson.getDisplayName(), JOptionPane.QUESTION_MESSAGE);
				int dmg = Utils.parseInt(newValue, -1);
				if (dmg > -1) {
					currentPerson.setCurrentBody(currentPerson.getCurrentBody() + dmg);

					newValue = JOptionPane.showInputDialog(null, "STUN healed", "Heal "
							+ currentPerson.getDisplayName(), JOptionPane.QUESTION_MESSAGE);
					dmg = Utils.parseInt(newValue, -1);
					if (dmg > -1) {
						currentPerson.setCurrentStun(currentPerson.getCurrentStun() + dmg);
					}
				}

				refreshViews();
			}
		}
	};

	protected final Action restorePersonAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Restore Person");
		}

		public void actionPerformed(ActionEvent e) {
			if (currentPerson != null) {
				currentPerson.setCurrentBody(currentPerson.getBody());
				currentPerson.setCurrentStun(currentPerson.getStun());

				updatePhaseDisplay();
			}
		}
	};

	protected final Action refreshViewsAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Refresh Views");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('V', Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(Action.MNEMONIC_KEY, new Integer('V'));
		}

		public void actionPerformed(ActionEvent e) {
			refreshViews();
		}
	};

	protected final Action setAsActiveAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Set As Active");
			putValue(Action.MNEMONIC_KEY, new Integer('S'));
		}

		public void actionPerformed(ActionEvent e) {
			setCurrentAsActive();
		}
	};

	protected final Action restoreToActiveAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Restore to Active");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('E', Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(Action.MNEMONIC_KEY, new Integer('e'));
		}

		public void actionPerformed(ActionEvent e) {
			restoreCurrentToActive();
		}
	};

	protected final Action restoreAllAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Restore All");
		}

		public void actionPerformed(ActionEvent e) {
			for (Iterator<Person> i = chars.iterator(); i.hasNext();) {
				Person p = i.next();
				p.setCurrentBody(p.getBody());
				p.setCurrentStun(p.getStun());
				p.setStunnedPhases(0);
			}
			updatePhaseDisplay();
		}
	};

	protected final Action nextPhaseAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Next Phase");
		}

		public void actionPerformed(ActionEvent e) {
			if (currentPerson != null) {
				System.out.println("Next phase is " + Integer.toString(currentPerson.getNextPhase(currPhase)));
			}
		}
	};

	protected final Action resetAction = new AbstractAction() {
		{
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(Action.MNEMONIC_KEY, new Integer('R'));
			putValue(Action.NAME, "Reset");
		}

		public void actionPerformed(ActionEvent e) {
			resetPhase();
		}
	};

	protected final Action diceMenuAction = new AbstractAction() {
		{
			putValue(Action.MNEMONIC_KEY, new Integer('D'));
			putValue(Action.NAME, "Dice");
		}

		public void actionPerformed(ActionEvent e) {

		}
	};

	protected final Action attackAction = new AbstractAction() {
		{
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('A', Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(Action.MNEMONIC_KEY, new Integer('A'));
			putValue(Action.NAME, "Attack Damage");
		}

		public void actionPerformed(ActionEvent e) {
			normalDamage();
		}
	};

	protected final Action killingAttackAction = new AbstractAction() {
		{
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('K', Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(Action.MNEMONIC_KEY, new Integer('K'));
			putValue(Action.NAME, "Killing Attack Damage");
		}

		public void actionPerformed(ActionEvent e) {

		}
	};

	protected final Action abortAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Abort");
		}

		public void actionPerformed(ActionEvent e) {
			if (currentPerson != null) {
				if (!isActingInCurrentPhase(currentPerson)) {
					currentPerson.setAborted(true);
				}
				currentPerson.setActed(true);
				refreshViews();
			}
		}
	};

	public void revalidate() {
		invalidate();
		repaint();
	}

	public PhaseTrackerPanel(JFrame frame) {
		this.frame = frame;
		init();
	}

	protected void setPersonStunned(Person p) {
		if (isActingInCurrentPhase(p) && !p.isUnconscious() && p.hasActed()) {
			p.setStunnedPhases(2);
		} else {
			p.setStunnedPhases(1);
		}

		p.setActed(true);

		refreshViews();
	}

	protected JPopupMenu getPopupMenu(int source) {
		JPopupMenu popupMenu = new JPopupMenu();

		popupMenu.add(new JMenuItem(addPersonAction));
		if (currentPerson != null) {
			popupMenu.add(new JMenuItem(editPersonAction));
			popupMenu.add(new JMenuItem(removePersonAction));

			if (currentPerson.getTarget() != null) {
				popupMenu.add(new JMenuItem(clearTargetAction));
			}
		}

		popupMenu.addSeparator();
		popupMenu.add(new JMenuItem(nextPhaseAction));
		popupMenu.addSeparator();

		if (currentPerson != null) {
			popupMenu.add(new JMenuItem(personInfoAction));
			
			// These are options that a character can perform
			if (source != PHASE_LIST) {
				popupMenu.add(new JMenuItem(abortAction));
			}

			if (currentPerson.actsInPhase(activePhase)) {
				popupMenu.add(new JMenuItem(recoverAction));
			}

			popupMenu.addSeparator();

			// These are changes to an individual character
			popupMenu.add(new JMenuItem(flashAction));
			popupMenu.add(new JMenuItem(changeSpeedAction));

			popupMenu.add(new JMenuItem(new AbstractAction() {
				{
					putValue(Action.NAME, "Stunned");
				}

				public void actionPerformed(ActionEvent e) {
					setPersonStunned(currentPerson);
				}
			}));

			popupMenu.add(new JMenuItem(damagePersonAction));
			popupMenu.add(new JMenuItem(healPersonAction));
			popupMenu.add(new JMenuItem(restorePersonAction));
			popupMenu.addSeparator();
		}

		popupMenu.add(new JMenuItem(restoreAllAction));

		return popupMenu;
	}

	public void setLocation(Window w) {
		if (!HeroSimProperties.hasProperty("mainFrameX") || !HeroSimProperties.hasProperty("mainFrameY")) {
			w.setLocationRelativeTo(null);
		} else {
			w.setLocation(HeroSimProperties.getIntProperty("mainFrameX"), HeroSimProperties
					.getIntProperty("mainFrameY"));
		}
	}

	protected MouseListener popupMenuMouseListener = new MouseListener() {
		public void mouseClicked(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		protected void handleMousePressedReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				if (e.getComponent() instanceof JList) {
					JList list = (JList) e.getComponent();
					int i = list.locationToIndex(e.getPoint());
					if (i > -1) {
						list.setSelectedIndex(i);
						// currentPerson =
						// findPerson(list.getModel().getElementAt(i).toString());
						currentPerson = (Person) list.getModel().getElementAt(i);
					} else {
						currentPerson = null;
					}
				} else if (e.getComponent() instanceof JTable) {
					JTable list = (JTable) e.getComponent();
					int i = list.rowAtPoint(e.getPoint());
					if (i > -1) {
						currentPerson = findPerson(list.getModel().getValueAt(i, 0).toString().trim());
					} else {
						currentPerson = null;
					}
				} else {
					currentPerson = null;
				}

				getPopupMenu((e.getComponent() instanceof JList ? PHASE_LIST : ALL_CHARS_LIST)).show(e.getComponent(),
						e.getX(), e.getY());
			}
		}

		public void mousePressed(MouseEvent e) {
			handleMousePressedReleased(e);
		}

		public void mouseReleased(MouseEvent e) {
			handleMousePressedReleased(e);

			if (!e.isPopupTrigger() && e.getComponent() instanceof JList) {
				JList list = (JList) e.getComponent();
				int i = list.locationToIndex(e.getPoint());
				if (i > -1) {
					list.setSelectedIndex(i);
					currentPerson = (Person) list.getModel().getElementAt(i);
					currentPerson.setActed(!currentPerson.hasActed());
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							refreshViews();
						}
					});
				}
			}
		}
	};

	protected void init() {
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing");
		}

		setLayout(new BorderLayout());

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(new JButton(resetAction));
		buttonPanel.add(new JButton(prevAction));
		buttonPanel.add(new JButton(nextAction));
		buttonPanel.add(new JButton(quitAction));

		JPanel phaseTurnPanel = new JPanel();

		currPhaseLabel = new JLabel("Phase 12");
		currPhaseLabel.setFont(getFont().deriveFont(28f));
		currPhaseLabel.setForeground(Color.BLUE);
		currPhaseLabel.setHorizontalAlignment(JLabel.CENTER);
		currPhaseLabel.setPreferredSize(new Dimension(340, 40));

		phaseTurnPanel.add(currPhaseLabel);

		currTurnLabel = new JLabel("Turn 1");
		currTurnLabel.setFont(getFont().deriveFont(28f));
		currTurnLabel.setForeground(Color.BLUE);
		currTurnLabel.setHorizontalAlignment(JLabel.CENTER);
		currTurnLabel.setPreferredSize(new Dimension(340, 40));

		phaseTurnPanel.add(currTurnLabel);

		add(phaseTurnPanel, BorderLayout.NORTH);

		add(buttonPanel, BorderLayout.SOUTH);

		Box leftPanel = Box.createHorizontalBox();

		listModel = new DefaultListModel();
		list = new JList(listModel);
		/*
		 * list.setDragEnabled(true); list.setTransferHandler(new
		 * PersonTransferHandler(this));
		 * 
		 * list.addMouseListener(new MouseAdapter() { public void
		 * mousePressed(MouseEvent e) { JComponent c = (JComponent)
		 * e.getSource(); TransferHandler handler = c.getTransferHandler();
		 * handler.exportAsDrag(c, e, TransferHandler.COPY); } });
		 */

		list.setCellRenderer(new CharCellRenderer());
		list.addMouseListener(popupMenuMouseListener);
		list.setFont(list.getFont().deriveFont(20f));

		table = new JTable(tableModel = new DefaultTableModel());
		table.setTransferHandler(new PersonTransferHandler(this));
		table.setDragEnabled(true);
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				JComponent c = (JComponent) e.getSource();
				TransferHandler handler = c.getTransferHandler();
				handler.exportAsDrag(c, e, TransferHandler.COPY);
			}
		});
		table.addMouseListener(popupMenuMouseListener);
		tableModel.setColumnCount(7);
		tableModel.setColumnIdentifiers(new String[] { "Name", "CON", "DEX", "REC", "SPD", "BODY", "STUN" });

		DefaultTableCellRenderer r = new DefaultTableCellRenderer();
		r.setHorizontalAlignment(JLabel.CENTER);
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			TableColumn col = table.getColumnModel().getColumn(i);
			if (i == 0) {
				col.setPreferredWidth(200);
			} else {
				col.setCellRenderer(r);
			}
		}

		leftPanel.add(Box.createHorizontalStrut(5));
		JScrollPane listPane = new JScrollPane(list);
		listPane.setPreferredSize(new Dimension(200, 0));
		listPane.setMinimumSize(new Dimension(200, 0));
		leftPanel.add(listPane);

		leftPanel.add(Box.createHorizontalStrut(5));
		JScrollPane tablePane = new JScrollPane(table);
		tablePane.addMouseListener(popupMenuMouseListener);
		tablePane.setPreferredSize(new Dimension(500, 500));
		tablePane.setMaximumSize(new Dimension(500, 500));
		leftPanel.add(tablePane);
		leftPanel.add(Box.createHorizontalStrut(5));

		add(leftPanel);

		getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_MASK),
				"next-phase");
		getActionMap().put("next-phase", nextAction);

		getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_MASK),
				"prev-phase");
		getActionMap().put("prev-phase", prevAction);

		if (logger.isDebugEnabled()) {
			logger.debug("Done with initialization");
		}

		resetPhase();
	}

	protected Person findPerson(String displayName) {
		Person result = null;

		int pos = -1;
		String[] nameSegs = StringUtils.split(displayName, "(");
		displayName = nameSegs[0].trim();

		if ((pos = displayName.indexOf("->")) > -1) {
			displayName = displayName.substring(0, pos).trim();
		}

		for (Iterator<Person> i = chars.iterator(); i.hasNext();) {
			Person p = i.next();
			if (p.getDisplayName().equals(displayName.trim())) {
				result = p;
				break;
			}
		}

		return result;
	}

	protected void normalDamage() {
		NormalDamageDialog dlg = new NormalDamageDialog(frame);
		dlg.setVisible(true);
	}

	@SuppressWarnings("unchecked")
	protected Person createPerson(Element root, Class c) {
		try {
			Person result = (Person)c.newInstance();
			if (root.getAttributeValue("file") != null) {
				root = Utils.loadXml(new File(root.getAttributeValue("file")));
			}
			result.initFromXml(root);

			return result;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	protected void openFile() {
		try {
			JFileChooser chooser = new JFileChooser();
			chooser.addChoosableFileFilter(new FileFilter() {
				public boolean accept(File file) {
					return (file.isDirectory() || (file.getName().toLowerCase().endsWith(".xml")));
				}

				public String getDescription() {
					return "XML Files";
				}
			});

			if (HeroSimProperties.hasProperty(HeroSimProperties.DATA_DIR)) {
				chooser.setCurrentDirectory(new File(HeroSimProperties.getProperty(HeroSimProperties.DATA_DIR)));
			}

			chooser.showOpenDialog(this);
			if (chooser.getSelectedFile() != null) {
				massUpdate = true;
				
				HeroSimProperties.setProperty(HeroSimProperties.DATA_DIR, chooser.getCurrentDirectory().getPath());

				Element root = Utils.loadXml(chooser.getSelectedFile());

				if (root.getName().equalsIgnoreCase("character")) {
					StringBuffer msg = new StringBuffer();
					msg.append("Load this as a villain?\n").append("Click 'Yes' to load as a villain,\n").append(
							"Click 'No' to load as a hero.");
					int option = JOptionPane.showConfirmDialog(null, msg, "Hero or Villain",
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
					if (option == JOptionPane.YES_OPTION) {
						Person p = createPerson(root, Villain.class);
						add(p);
					} else if (option == JOptionPane.NO_OPTION) {
						Person p = createPerson(root, Hero.class);
						add(p);
					}
				} else if (root.getName().equalsIgnoreCase("villain")) {
					String num = JOptionPane.showInputDialog("Number to add", "1");
					if (num != null && num.trim().length() > 0) {
						try {
							int max = Integer.parseInt(num);
							if (max == 1) {
								Person p = createPerson(root, Villain.class);
								add(p);
							} else {
								for (int i = 0; i < max; i++) {
									Person p = createPerson(root, Villain.class);
									p.setDisplayName(p.getDisplayName() + " " + Integer.toString(i + 1));
									add(p);
								}
							}
						} catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(frame, num + " is not a valid number", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				} else {
					for (Iterator i = root.getChildren("hero").iterator(); i.hasNext();) {
						Element person = (Element) i.next();
						Person p = createPerson(person, Hero.class);
						add(p);
					}

					for (Iterator i = root.getChildren("villain").iterator(); i.hasNext();) {
						Element person = (Element) i.next();
						Person p = createPerson(person, Villain.class);
						add(p);
					}
				}
				
				massUpdate = false;
				updatePhaseDisplay();
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	protected void saveFile() {
		try {
			JFileChooser chooser = new JFileChooser();
			chooser.addChoosableFileFilter(new FileFilter() {
				public boolean accept(File file) {
					return (file.isDirectory() || (file.getName().toLowerCase().endsWith(".xml")));
				}

				public String getDescription() {
					return "XML Files";
				}
			});

			if (HeroSimProperties.hasProperty(HeroSimProperties.DATA_DIR)) {
				chooser.setCurrentDirectory(new File(HeroSimProperties.getProperty(HeroSimProperties.DATA_DIR)));
			}

			chooser.showSaveDialog(this);
			if (chooser.getSelectedFile() != null) {
				String filename;
				if (chooser.getSelectedFile().getName().indexOf('.') < 0) {
					filename = chooser.getSelectedFile().getAbsolutePath() + ".xml";
				} else {
					filename = chooser.getSelectedFile().getAbsolutePath();
				}
				HeroSimProperties.setProperty(HeroSimProperties.DATA_DIR, chooser.getCurrentDirectory().getPath());

				boolean saveAsMulti = true;
				if (chars.size() == 1) {
					saveAsMulti = JOptionPane.showConfirmDialog(frame, "Save as individual villain file?", "Save Confirm",
							JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION;
				}

				Element root;
				if (saveAsMulti) {
					root = new Element("characters");

					for (Person p : chars) {
						root.addContent(p.toXml());
					}
				} else {
					root = chars.get(0).toXml();
				}

				Utils.saveXml(root, filename);
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public JMenuBar getJMenuBar() {
		JMenuBar result = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		fileMenu.add(new JMenuItem(openAction));
		fileMenu.add(new JMenuItem(saveAction));
		fileMenu.add(new JSeparator());
		fileMenu.add(new JMenuItem(addPersonAction));
		fileMenu.add(new JMenuItem(removePersonAction));
		fileMenu.add(new JMenuItem(removeAllAction));
		fileMenu.add(new JMenuItem(removeAllNonPlayerAction));
		fileMenu.add(new JSeparator());
		fileMenu.add(new JMenuItem(quitAction));
		result.add(fileMenu);

		JMenu optionsMenu = new JMenu("Options");

		JCheckBoxMenuItem skipSegmentsMenu = new JCheckBoxMenuItem(skipSegmentsAction);
		skipSegmentsMenu.setSelected(skipEmptySegments);
		optionsMenu.add(skipSegmentsMenu);

		JCheckBoxMenuItem playerViewMenu = new JCheckBoxMenuItem(playerViewAction);
		playerViewMenu.setSelected(playerView);
		optionsMenu.add(playerViewMenu);

		JCheckBoxMenuItem rollImpairmentMenu = new JCheckBoxMenuItem(rollImpairmentAction);
		rollImpairmentMenu.setSelected(rollImpairment);
		optionsMenu.add(rollImpairmentMenu);

		result.add(optionsMenu);

		JMenu phaseMenu = new JMenu("Phase");
		phaseMenu.add(new JMenuItem(prevAction));
		phaseMenu.add(new JMenuItem(nextAction));
		phaseMenu.add(new JSeparator());
		phaseMenu.add(new JMenuItem(resetAction));
		phaseMenu.add(new JMenuItem(refreshViewsAction));
		phaseMenu.add(new JSeparator());
		phaseMenu.add(new JMenuItem(setAsActiveAction));
		phaseMenu.add(new JMenuItem(restoreToActiveAction));

		result.add(phaseMenu);

		return result;
	}

	public void close() {
		PluginManager.terminate();
		HeroSimProperties.setIntProperty("mainFrameX", HeroSimMain.frame.getX());
		HeroSimProperties.setIntProperty("mainFrameY", HeroSimMain.frame.getY());
		HeroSimProperties.setBooleanProperty("playerView", playerView);
		HeroSimProperties.setBooleanProperty("rollImpairment", rollImpairment);
		HeroSimProperties.save();
		System.exit(0);
	}

	protected void editSet() {
		EditSetDialog dlg = new EditSetDialog(frame);
		for (Iterator<Person> i = chars.iterator(); i.hasNext();) {
			dlg.getCharacters().add(i.next());
		}
		dlg.setVisible(true);
		if (dlg.getCharacters() != null && dlg.getCharacters().size() > 0) {
			chars.clear();
			for (Person p : dlg.getCharacters()) {
				add(p);
			}
			resetPhase();
		}
	}

	protected void rollImpairmentDamage() {
		ImpairmentChart i = ImpairmentChart.getInstance();
		try {
			ImpairmentDialog d = new ImpairmentDialog(HeroSimMain.frame);
			d.result = i.getDiceResult();
			d.display();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(HeroSimMain.frame, e.getLocalizedMessage(), "Exception",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void updatePhaseDisplay() {
		if (massUpdate) {
			return;
		}
		
		boolean isActivePhase = (currPhase == activePhase) && (currTurn == activeTurn);

		if (isActivePhase) {
			currPhaseLabel.setText("Phase " + Integer.toString(currPhase));
			currTurnLabel.setText("Turn " + Integer.toString(currTurn));
			setAsActiveAction.setEnabled(false);
			restoreToActiveAction.setEnabled(false);
		} else {
			currPhaseLabel.setText("Phase " + Integer.toString(currPhase) + " (" + Integer.toString(activePhase) + ")");
			currTurnLabel.setText("Turn " + Integer.toString(currTurn) + " (" + Integer.toString(activeTurn) + ")");
			setAsActiveAction.setEnabled(true);
			restoreToActiveAction.setEnabled(true);
		}

		List<Person> charsInPhase = new LinkedList<Person>();

		listModel.clear();
		tableModel.setRowCount(0);
		for (Person p : chars) {
			if (logger.isDebugEnabled()) {
				logger.debug("Checking " + p.getName());
			}

			String displayText = p.getDisplayName();
			if (p.getDisplaySuffix() != null && p.getDisplaySuffix().length() > 0) {
				displayText += " " + p.getDisplaySuffix();
			} else if (p.getTarget() != null) {
				displayText += " -> " + p.getTarget().getDisplayName();
			}

			tableModel.addRow(new Object[] { displayText, Integer.toString(p.getCon()), Integer.toString(p.getDex()),
					Integer.toString(p.getRec()), Integer.toString(p.getSpeed()),
					Integer.toString(p.getBody()) + " / " + Integer.toString(p.getCurrentBody()),
					Integer.toString(p.getStun()) + " / " + Integer.toString(p.getCurrentStun()) });

			if (p.actsInPhase(currPhase)) {
				listModel.addElement(p);
				charsInPhase.add(p);
			}
		}

		invalidate();
		repaint();

		PluginManager.changePhase(currTurn, currPhase, charsInPhase);
		charsInPhase.clear();
	}

	protected void resetPhase() {
		currPhase = activePhase = 12;
		currTurn = activeTurn = 1;
		for (Person p : chars) {
			p.setTarget(null);
			p.setActed(false);
			p.setAborted(false);
			p.setFlashed(0);
			p.setStunnedPhases(0);
			p.setCurrentStun(p.getStun());
		}
		refreshViews();
	}

	protected boolean isEmptySegment(int segment) {
		boolean result = true;

		for (Person p : chars) {
			if (p.actsInPhase(segment)) {
				result = false;
				break;
			}
		}
		return result;
	}

	protected void refreshViews() {
		if (massUpdate) {
			return;
		}
		
		PluginManager.changePhase(currTurn, currPhase, chars);
		updatePhaseDisplay();
	}

	protected void setCurrentAsActive() {
		activePhase = currPhase;
		activeTurn = currTurn;
		PluginManager.changePhase(currTurn, currPhase, chars);
		updatePhaseDisplay();
	}

	protected void restoreCurrentToActive() {
		currPhase = activePhase;
		currTurn = activeTurn;
		PluginManager.changePhase(currTurn, currPhase, chars);
		updatePhaseDisplay();
	}

	protected void nextPhase() {
		boolean isActivePhase = (currPhase == activePhase) && (currTurn == activeTurn);

		if (isActivePhase) {
			endingPhase(currPhase);
		}
		currPhase = (currPhase % 12) + 1;
		if (currPhase == 12) {
			currTurn++;
		}
		if (isActivePhase) {
			startingPhase(currPhase);
		}

		// Do we need to skip segments?
		while (skipEmptySegments && isEmptySegment(currPhase) && chars.size() > 0) {
			currPhase = (currPhase % 12) + 1;
			if (currPhase == 12) {
				currTurn++;
			}
			if (isActivePhase) {
				startingPhase(currPhase);
			}
		}

		if (isActivePhase) {
			activePhase = currPhase;
			activeTurn = currTurn;
		}

		updatePhaseDisplay();
	}

	protected void prevPhase() {
		if (currPhase != 12 || currTurn != 1) {
			// endingPhase(currPhase);

			currPhase = currPhase - 1;
			if (currPhase == 0) {
				currPhase = 12;
			} else if (currPhase == 11) {
				currTurn--;
			}
			updatePhaseDisplay();
		}
	}

	protected void sortChars() {
		Collections.sort(this.chars, new Comparator<Person>() {
			public int compare(Person o1, Person o2) {
				// The person with the higher dex is actually lower
				// (i.e., first) in the list
				return o2.getDex() - o1.getDex();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.culpan.herosim.PersonManager#personIterator()
	 */
	public Iterator<Person> personIterator() {
		return chars.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.culpan.herosim.PersonManager#add(org.culpan.herosim.Person)
	 */
	public void add(Person p) {
		if (!chars.contains(p)) {
			chars.add(p);
			sortChars();

			updatePhaseDisplay();
			PluginManager.personAdded(p);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.culpan.herosim.PersonManager#remove(org.culpan.herosim.Person)
	 */
	public void remove(Person p) {
		if (chars.contains(p)) {
			chars.remove(p);
			sortChars();

			refreshViews();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.culpan.herosim.PersonManager#endingPhase(int)
	 */
	@SuppressWarnings("unchecked")
	public void endingPhase(int phase) {
		for (Iterator i = personIterator(); i.hasNext();) {
			Person p = (Person) i.next();
			if (p.actsInPhase(phase)) {
				logger.debug("Calling endingPhase for " + p.getDisplayName());
				p.endingPhase(phase);
			} else if (phase == 12) {
				p.recoveryPhase();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void startingPhase(int phase) {
		for (Iterator i = personIterator(); i.hasNext();) {
			Person p = (Person) i.next();

			if (p.isFlashed()) {
				p.setFlashed(p.getFlashed() - 1);
			}

			if (p.actsInPhase(phase)) {
				if (p.isStunned() || p.isUnconscious()) {
					p.setActed(true);
				} else if (!p.hasActed()) {
					JOptionPane.showMessageDialog(frame, p.getDisplayName()
							+ " has a held action and is about to lose it in phase " + phase, "Held Action",
							JOptionPane.WARNING_MESSAGE);
					p.setAborted(false);
					p.setActed(false);
				} else if (p.hasAborted()) {
					p.setAborted(false);
					p.setActed(true);
				} else {
					p.setActed(false);
				}
			}
		}
	}
	
	public void displayInfo(Person p) {
		PersonInfoDialog d = new PersonInfoDialog(frame);
		d.setVisible(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.culpan.herosim.PersonManager#isActingInCurrentPhase(org.culpan.herosim.Person)
	 */
	public boolean isActingInCurrentPhase(Person p) {
		logger.debug("Checking if " + p.getName() + " acts in phase " + Integer.toString(currPhase));
		return p.actsInPhase(currPhase);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent arg0) {
		close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	public void windowDeactivated(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	public void windowDeiconified(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent arg0) {
	}
}