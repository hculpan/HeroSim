/*
 * Created on Mar 25, 2005
 *
 */
package org.culpan.herosim.gui.dice;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JSpinner;

/**
 * @author culpanh
 *  
 */
public class NormalDamageDialog extends DiceDialog {
//    private static final Logger logger = Logger.getLogger(NormalDamageDialog.class);
    
    protected JSpinner numberOfDice;
    protected JSpinner modifier = new JSpinner();
    
    /**
     * @param parent
     * @param title
     */
    public NormalDamageDialog(JFrame parent) {
        super(parent, "Normal Damage");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.culpan.herosim.gui.dice.DiceDialog#initUi()
     */
    protected void initUi() {
        getContentPane().setLayout(new BorderLayout());
    }
}