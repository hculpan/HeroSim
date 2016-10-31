package org.culpan.herosim.gui.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ActionLogDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JList logList;

    protected List<String> log;

    public ActionLogDialog(JFrame owner, List<String> log) {
        this.log = log;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        DefaultListModel listModel = (DefaultListModel)logList.getModel();
        for (String logEvent : log) {
            listModel.addElement(logEvent);
        }

        setSize(new Dimension((int)logList.getPreferredSize().getWidth() + 100, (int)logList.getPreferredSize().getHeight() + 50));
        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
    }

    private void onOK() {
        // add your code here
        dispose();
    }
}
