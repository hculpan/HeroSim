package org.culpan.herosim.gui;

import org.culpan.herosim.Person;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;

/**
 * Created by USUCUHA on 10/28/2016.
 */
public class DamageDialog extends JDialog {

    protected Integer body;

    protected Integer stun;

    protected Action okAction = new AbstractAction() {
        {
            putValue(Action.NAME, "Ok");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
        }

        @SuppressWarnings("unchecked")
        public void actionPerformed(ActionEvent e) {
            body = new Integer(10);
            stun = new Integer(100);
            setVisible(false);
        }
    };

    protected Action cancelAction = new AbstractAction() {
        {
            putValue(Action.NAME, "Cancel");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        }

        public void actionPerformed(ActionEvent e) {
            body = null;
            stun = null;
            setVisible(false);
        }
    };

    public DamageDialog(JFrame owner, String displayName) {
        super(owner, "Damage " + displayName, true);

        setSize(500, 400);
        setLocationRelativeTo(owner);

        getContentPane().setLayout(new BorderLayout());

        JPanel btnPanel = new JPanel();
        btnPanel.add(new JButton(okAction));
        btnPanel.add(new JButton(cancelAction));
        getContentPane().add(btnPanel, BorderLayout.SOUTH);

        getContentPane().add(buildTopPanel(), BorderLayout.CENTER);

    }

    protected JPanel buildTopPanel() {
        return new JPanel();
    }
}
