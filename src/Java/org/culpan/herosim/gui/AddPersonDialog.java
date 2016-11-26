package org.culpan.herosim.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.culpan.herosim.Hero;
import org.culpan.herosim.Person;
import org.culpan.herosim.Utils;
import org.culpan.herosim.Villain;

public class AddPersonDialog extends JDialog {
	public Person result = null;
	public int resultCount = 1;

	public int width = 350;

	public int height = 430;

	protected JTextField nameField = new JTextField(10);

	protected JTextField spdField = new JTextField(10);

	protected JTextField conField = new JTextField(10);
	
	protected JTextField dexField = new JTextField(10);

	protected JTextField recField = new JTextField(10);
	
	protected JTextField bodyField = new JTextField(10);

	protected JTextField stunField = new JTextField(10);

	protected JTextField pdField = new JTextField(10);

	protected JTextField edField = new JTextField(10);

	protected JTextField dcvField = new JTextField(10);

	protected JCheckBox player = new JCheckBox();

	protected JTextField countField = new JTextField(10);

	protected Container addEntry(String text, JTextField textField) {
		JPanel result = new JPanel();
		result.setAlignmentX(JPanel.LEFT_ALIGNMENT);

		JLabel l = new JLabel(text);
		l.setHorizontalAlignment(JLabel.RIGHT);
		result.add(l);

		result.add(textField);

		return result;
	}

	protected Container addEntry(String text, JCheckBox cBox) {
		JPanel result = new JPanel();
		result.setAlignmentX(JPanel.LEFT_ALIGNMENT);

		JLabel l = new JLabel(text);
		l.setHorizontalAlignment(JLabel.RIGHT);
		result.add(l);

		result.add(cBox);

		return result;
	}

	public AddPersonDialog(Frame parent, Person p) {
		this(parent, p, 350, 430);
	}

	public AddPersonDialog(Frame parent, Person p, int width, int height) {
		result = p;

		setLayout(new BorderLayout());

		setSize(width, height);
		setModal(true);
//		setResizable(false);
		setTitle("Add Person");
		setLocationRelativeTo(parent);

		final AddPersonDialog addPersonDialog = this;

		this.addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent e) {
				addPersonDialog.width = ((JDialog)e.getSource()).getWidth();
				addPersonDialog.height = ((JDialog)e.getSource()).getHeight();
			}

			@Override
			public void componentMoved(ComponentEvent e) {

			}

			@Override
			public void componentShown(ComponentEvent e) {

			}

			@Override
			public void componentHidden(ComponentEvent e) {

			}
		});

		Box b = Box.createVerticalBox();

		b.add(addEntry("Name ", nameField));
		b.add(addEntry("SPD ", spdField));
		b.add(addEntry("DEX ", dexField));
		b.add(addEntry("CON ", conField));
		b.add(addEntry("REC ", recField));
		b.add(addEntry("BODY ", bodyField));
		b.add(addEntry("STUN ", stunField));
		b.add(addEntry("PD ", pdField));
		b.add(addEntry("ED ", edField));
		b.add(addEntry("DCV ", dcvField));
		b.add(addEntry("Player", player));
		b.add(addEntry("Count", countField));
		countField.setText("1");

		if (result != null) {
			setTitle("Edit " + result.getDisplayName());

			nameField.setText(result.getName());
			spdField.setText(Integer.toString(result.getSpeed()));
			dexField.setText(Integer.toString(result.getDex()));
			conField.setText(Integer.toString(result.getCon()));
			recField.setText(Integer.toString(result.getRec()));
			bodyField.setText(Integer.toString(result.getBody()));
			stunField.setText(Integer.toString(result.getStun()));
			pdField.setText(Integer.toString(result.getPd()));
			edField.setText(Integer.toString(result.getEd()));
			dcvField.setText(Integer.toString(result.getDcv()));
			player.setSelected(result instanceof Hero);
		}

		add(b, BorderLayout.CENTER);

		JPanel btm = new JPanel();
		JButton defButton = new JButton(new AbstractAction() {
			{
				putValue(Action.MNEMONIC_KEY, new Integer('O'));
				putValue(Action.NAME, "Ok");
			}

			public void actionPerformed(ActionEvent e) {
				if (nameField.getText().trim().length() == 0) {
					JOptionPane.showMessageDialog(null, "Missing name", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (spdField.getText().trim().length() == 0) {
					JOptionPane.showMessageDialog(null, "Missing SPD", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (dexField.getText().trim().length() == 0) {
					JOptionPane.showMessageDialog(null, "Missing DEX", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (conField.getText().trim().length() == 0) {
					JOptionPane.showMessageDialog(null, "Missing CON", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (recField.getText().trim().length() == 0) {
					JOptionPane.showMessageDialog(null, "Missing REC", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (bodyField.getText().trim().length() == 0) {
					JOptionPane.showMessageDialog(null, "Missing BODY", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (stunField.getText().trim().length() == 0) {
					JOptionPane.showMessageDialog(null, "Missing STUN", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (pdField.getText().trim().length() == 0) {
					JOptionPane.showMessageDialog(null, "Missing PD", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (edField.getText().trim().length() == 0) {
					JOptionPane.showMessageDialog(null, "Missing ED", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (dcvField.getText().trim().length() == 0) {
					JOptionPane.showMessageDialog(null, "Missing DCV", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (countField.getText().trim().length() == 0) {
					JOptionPane.showMessageDialog(null, "Missing Count", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (player.isSelected()) {
					result = new Hero(nameField.getText(), Integer.parseInt(spdField.getText()), Integer
							.parseInt(dexField.getText()));
					result.setBody(Integer.parseInt(bodyField.getText()));
					result.setCurrentBody(Integer.parseInt(bodyField.getText()));
					result.setStun(Integer.parseInt(stunField.getText()));
					result.setCurrentStun(Integer.parseInt(stunField.getText()));
					result.setCon(Integer.parseInt(conField.getText()));
					result.setPd(Integer.parseInt(pdField.getText()));
					result.setEd(Integer.parseInt(edField.getText()));
					result.setDcv(Integer.parseInt(dcvField.getText()));
					result.setRec(Integer.parseInt(recField.getText()));
					resultCount = Utils.parseInt(countField.getText(), 1);
					setVisible(false);
				} else {
					result = new Villain(nameField.getText(), Integer.parseInt(spdField.getText()), Integer
							.parseInt(dexField.getText()));
					result.setBody(Integer.parseInt(bodyField.getText()));
					result.setCurrentBody(Integer.parseInt(bodyField.getText()));
					result.setStun(Integer.parseInt(stunField.getText()));
					result.setCurrentStun(Integer.parseInt(stunField.getText()));
					result.setCon(Integer.parseInt(conField.getText()));
					result.setPd(Integer.parseInt(pdField.getText()));
					result.setEd(Integer.parseInt(edField.getText()));
					result.setDcv(Integer.parseInt(dcvField.getText()));
					result.setRec(Integer.parseInt(recField.getText()));
					resultCount = Utils.parseInt(countField.getText(), 1);
					setVisible(false);
				}
			}
		});
		getRootPane().setDefaultButton(defButton);
		btm.add(defButton);

		btm.add(new JButton(new AbstractAction() {
			{
				putValue(Action.MNEMONIC_KEY, new Integer('C'));
				putValue(Action.NAME, "Cancel");
			}

			public void actionPerformed(ActionEvent e) {
				result = null;
				setVisible(false);
			}
		}));

		add(btm, BorderLayout.SOUTH);
	}

	public AddPersonDialog(Frame parent) {
		this(parent, null);
	}
}
