package org.culpan.herosim.gui;

import org.culpan.herosim.gui.dice.DiceParseException;
import org.culpan.herosim.gui.dice.DiceRoller;
import org.culpan.herosim.gui.dice.NormalDamageDiceRoller;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

public class DamagePersonDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField stunField;
    private JTextField bodyField;
    private JRadioButton PDRadioButton;
    private JRadioButton halfEDRadioButton1;
    private JRadioButton halfPDRadioButton;
    private JRadioButton noDefensesRadioButton;
    private JRadioButton EDRadioButton1;
    private JLabel damageOutField;
    private JButton a12d6Button;
    private JButton a16d6Button;
    private JButton a14d6Button;
    private JButton a18d6Button;
    private JButton a20d6Button;
    private JLabel knockbackOutputField;
    private JTextField diceTextField;
    private JButton rollButton;

    public Integer body;

    public Integer stun;

    public int pd;

    public int ed;

    public DamagePersonDialog(JFrame owner, String displayName) {
        setTitle("Damage " + displayName);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        stunField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                changeDamageMessage();
            }
            public void removeUpdate(DocumentEvent e) {
                changeDamageMessage();
            }
            public void insertUpdate(DocumentEvent e) {
                changeDamageMessage();
            }
        });

        setSize(new Dimension((int)getPreferredSize().getWidth() + 100, (int)getPreferredSize().getHeight() + 50));
//        getContentPane().setSize(getSize());
        setResizable(false);
        setLocationRelativeTo(owner);

        EDRadioButton1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                changeDamageMessage();
            }
        });

        PDRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                changeDamageMessage();
            }
        });

        halfEDRadioButton1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                changeDamageMessage();
            }
        });

        halfPDRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                changeDamageMessage();
            }
        });

        noDefensesRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                changeDamageMessage();
            }
        });

        a12d6Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rollDamageDice("12n");
            }
        });
        a14d6Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rollDamageDice("14n");
            }
        });
        a16d6Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rollDamageDice("16n");
            }
        });
        a18d6Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rollDamageDice("18n");
            }
        });
        a20d6Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rollDamageDice("20n");
            }
        });
        rollButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String diceText = diceTextField.getText();
                if (diceText != null && !diceText.trim().isEmpty()) {
                    rollDamageDice(diceText);
                }
            }
        });
    }

    protected void rollDamageDice(String text) {
        NormalDamageDiceRoller diceRoller = new NormalDamageDiceRoller();
        try {
            DiceRoller.DamageResult damageResult = diceRoller.rollNormalDamage(text);
            if (damageResult != null) {
                stunField.setText(Integer.toString(damageResult.stun));
                bodyField.setText(Integer.toString(damageResult.body));
                if (damageResult.knockback > 0 || damageResult.knockbackResisted > 0) {
                    knockbackOutputField.setText(
                            Integer.toString(damageResult.knockback) + "' KB (" +
                                    damageResult.knockbackResisted + " rolled)");
                } else {
                    knockbackOutputField.setText("");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Error : '" + text + "' is not a valid dice roller text", "Dice Roller", JOptionPane.ERROR_MESSAGE);
            }
        } catch (DiceParseException e) {
            JOptionPane.showMessageDialog(null, "Error : '" + text + "' is not a valid dice roller text", "Dice Roller", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changeDamageMessage() {
        Integer stunDamage = getStunValue();
        if (stunDamage == null) {
            damageOutField.setText("");
        } else {
            damageOutField.setText("Damage = " + getStunDamage());
        }
    }

    private Integer getStunDamage() {
        if (getStunValue() == null) {
            return null;
        } else {
            return (getStunValue() - getDefenseValue() < 0 ? 0 : getStunValue() - getDefenseValue());
        }
    }

    private Integer getStunValue() {
        if (stunField.getText().isEmpty() || !stunField.getText().matches("\\d+")) {
            return null;
        } else {
            return Integer.parseInt(stunField.getText());
        }
    }

    private int getDefenseValue() {
        if (PDRadioButton.isSelected()) {
            return pd;
        } else if (EDRadioButton1.isSelected()) {
            return ed;
        } else if (halfEDRadioButton1.isSelected()) {
            return (int)Math.round((ed/2.0) + 0.1);
        } else if (halfPDRadioButton.isSelected()) {
            return (int)Math.round((pd/2.0) + 0.1);
        } else {
            return 0;
        }
    }

    private void onOK() {
        if (!stunField.getText().isEmpty() && stunField.getText().matches("\\d+") &&
                !bodyField.getText().isEmpty() && bodyField.getText().matches("\\d+")) {
            stun = getStunDamage();
            body = Integer.parseInt(bodyField.getText());
            dispose();
        }
    }

    private void onCancel() {
        stun = null;
        body = null;
        dispose();
    }

    @Override
    public void setVisible(boolean b) {
        PDRadioButton.setText("PD: " + Integer.toString(pd));
        EDRadioButton1.setText("ED: " + Integer.toString(ed));
        halfEDRadioButton1.setText("Half ED: " + Long.toString(Math.round((ed/2.0) + 0.1)));
        halfPDRadioButton.setText("Half PD: " + Long.toString(Math.round((pd/2.0) + 0.1)));

        super.setVisible(b);
    }
}
