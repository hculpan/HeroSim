/*
 * Created on Mar 24, 2005
 *
 */
package org.culpan.herosim.gui.dice;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;

/**
 * @author culpanh
 *
 */
public abstract class DiceDialog extends JDialog {
    protected final Action closeAction = new AbstractAction() {
        {
            putValue(Action.MNEMONIC_KEY, new Integer('C'));
            putValue(Action.NAME, "Close");
        }

        public void actionPerformed(ActionEvent e) {
        	setVisible(false);
        }
    };
    
    public DiceDialog(Frame parent, String title) {
        super(parent, title, true);
        setSize(300, 400);
        setResizable(false);
        setLocationRelativeTo(parent);
        initUi();
    }
    
    protected abstract void initUi();
}
