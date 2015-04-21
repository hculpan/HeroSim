package org.culpan.herosim.gui;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

public class PersonInfoDialog extends JDialog {
	public PersonInfoDialog(JFrame parent) {
		super(parent, false);
		setUndecorated(true);
		setSize(300, 200);
//		PointerInfo pi = MouseInfo.getPointerInfo();
		setLocationRelativeTo(parent);
		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createLineBorder(Color.black));
		getContentPane().add(panel);
		
		panel.addMouseListener(new MouseInputAdapter() {
			public void mouseClicked(MouseEvent e) {
				setVisible(false);
			}
		});
				
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
					setVisible(false);
				}
			}
		});
	}
}
