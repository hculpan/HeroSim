/*
 * Created on Feb 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.culpan.herosim.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.culpan.herosim.Person;
import org.culpan.herosim.gui.ClientCharCellListRenderer;

/**
 * @author harry
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class PlayerViewerPlugin implements HeroSimPlugin {
	protected JFrame frame;

    protected DefaultListModel listModel;
    protected JList list;
    protected JLabel phaseLabel;
    protected JLabel turnLabel;

    /*
     * (non-Javadoc)
     * 
     * @see org.culpan.herosim.plugin.HeroSimPlugin#initialize()
     */
    public void initialize() {
        frame = new JFrame();
        frame.setTitle("Hero Simulator Player View");
        frame.setSize(new Dimension(400, 600));

        initializeUi();

        frame.setVisible(true);
    }

    protected void initializeUi() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        phaseLabel = new JLabel("Phase 12");
        phaseLabel.setFont(phaseLabel.getFont().deriveFont(32f));
        phaseLabel.setForeground(Color.BLUE);
        phaseLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(phaseLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel();
        list = new JList(listModel);
        list.setCellRenderer(new ClientCharCellListRenderer());
        mainPanel.add(new JScrollPane(list), BorderLayout.CENTER);
        
        turnLabel = new JLabel("Turn 1");
        turnLabel.setFont(phaseLabel.getFont());
        turnLabel.setForeground(Color.BLUE);
        turnLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(turnLabel, BorderLayout.SOUTH);

        frame.getContentPane().add(mainPanel);
    }

    protected void refresh() {
        frame.invalidate();
        frame.repaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.culpan.herosim.plugin.HeroSimPlugin#terminate()
     */
    public void terminate() {
    	if (frame != null) {
    		frame.setVisible(false);
    		frame = null;
    	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.culpan.herosim.plugin.HeroSimPlugin#personAdded(org.culpan.herosim.Person)
     */
    public void personAdded(Person person) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.culpan.herosim.plugin.HeroSimPlugin#personRemoved(org.culpan.herosim.Person)
     */
    public void personRemoved(Person person) {
    }
    
    protected void setTurnPhase(int turn, int phase) {
        turnLabel.setText("Turn " + Integer.toString(turn));
        phaseLabel.setText("Phase " + Integer.toString(phase));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.culpan.herosim.plugin.HeroSimPlugin#changePhase(int, int,
     *      java.util.List)
     */
    public void changePhase(int turn, int phase, List<Person> chars) {
        listModel.clear();
        
        setTurnPhase(turn, phase);

        for (Person p : chars) {
            listModel.addElement(p);
        }

        refresh();
    }

    /* (non-Javadoc)
	 * @see org.culpan.herosim.plugin.HeroSimPlugin#event(java.lang.String, java.lang.Object)
	 */
	public void event(String eventName, Object data) {
		if (eventName.equalsIgnoreCase(CHANGE_PHASE)) {
			PluginManager.PhaseReport pr = (PluginManager.PhaseReport)data;
			changePhase(pr.turn, pr.phase, pr.chars);
		}
	}

}