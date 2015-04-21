package org.culpan.herosim.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;
import org.culpan.herosim.ImpairmentChart;

public class ImpairmentDialog extends JDialog {
	private static final Logger logger = Logger.getLogger(ImpairmentDialog.class);

	public String result;

	protected JLabel resultLabel = new JLabel();

	protected JTextArea descr = new JTextArea();

	public ImpairmentDialog(Frame parent) {
		super(parent);
		setSize(500, 300);
		setLocationRelativeTo(parent);
		setTitle("Impairment");
		setResizable(false);

		setLayout(new BorderLayout());

		JPanel btnPanel = new JPanel();
		JButton okButton = new JButton(new AbstractAction() {
			{
				putValue(Action.NAME, "Ok");
			}

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		btnPanel.add(okButton);
		add(btnPanel, BorderLayout.SOUTH);

		JPanel midPanel = new JPanel();
		midPanel.setLayout(new BorderLayout());
		JPanel innerMid = new JPanel();
		JComboBox locsList = new JComboBox();
		locsList.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cbox = (JComboBox) e.getSource();
				String item = cbox.getSelectedItem().toString();
				String text = ImpairmentChart.getInstance().getLocationDescr(item).trim();
				String[] lines = text.split("\n");
				text = "";
				for (int i = 0; i < lines.length; i++) {
					text += lines[i].trim() + " ";
				}
				descr.setText(text);
			}
		});
		DefaultComboBoxModel m = new DefaultComboBoxModel();
		for (String l : ImpairmentChart.getInstance().getLocationsSet()) {
			logger.debug("Adding " + l + " to locations dropdown");
			m.addElement(l);
		}
		locsList.setModel(m);
		locsList.setSelectedIndex(0);
		innerMid.add(locsList);
		midPanel.add(innerMid, BorderLayout.NORTH);
		midPanel.add(new JScrollPane(descr, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		descr.setEditable(false);
		descr.setLineWrap(true);
		descr.setWrapStyleWord(true);
		add(midPanel, BorderLayout.CENTER);

		JPanel resultPanel = new JPanel();
		resultPanel.add(resultLabel);
		add(resultPanel, BorderLayout.NORTH);
	}

	public void display() {
		resultLabel.setText(result);
		setVisible(true);
	}
}
