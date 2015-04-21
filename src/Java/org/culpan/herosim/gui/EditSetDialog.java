package org.culpan.herosim.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.culpan.herosim.Hero;
import org.culpan.herosim.HeroSimProperties;
import org.culpan.herosim.Person;
import org.culpan.herosim.Utils;
import org.jdom.Element;

public class EditSetDialog extends JDialog implements ListSelectionListener, WindowListener {
	private final static Logger logger = Logger.getLogger(EditSetDialog.class);
	
	protected JTextField nameField;

	protected JTextField spdField;

	protected JTextField dexField;
	
	protected JCheckBox partyCheck;
	
	protected JList list;
	
	protected List<Person> characters = new ArrayList<Person>();

	protected Action cancelAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Cancel");
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
		}

		public void actionPerformed(ActionEvent e) {
			characters.clear();
			setVisible(false);
		}
	};

	protected Action okAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Ok");
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		}

		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			characters.clear();
			for (Enumeration i = listModel.elements(); i.hasMoreElements();) {
				characters.add((Person)i.nextElement());
			}
			setVisible(false);
		}
	};

	public EditSetDialog(JFrame owner) {
		super(owner, "Edit Set", true);

		setSize(500, 400);
		setLocationRelativeTo(owner);

		getContentPane().setLayout(new BorderLayout());
		
		JPanel btnPanel = new JPanel();
		btnPanel.add(new JButton(okAction));
		btnPanel.add(new JButton(cancelAction));
		getContentPane().add(btnPanel, BorderLayout.SOUTH);

		getContentPane().add(buildTopPanel(), BorderLayout.CENTER);
		
		addWindowListener(this);
	}

	protected Action newAction = new AbstractAction() {
		{
			putValue(Action.NAME, "New");
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
		}

		public void actionPerformed(ActionEvent e) {
			listModel.clear();
		}
	};

	protected Action saveAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Save");
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		}

		public void actionPerformed(ActionEvent e) {
			save();
		}
	};

	protected Action addAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Add");
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
		}

		public void actionPerformed(ActionEvent e) {
			if (StringUtils.isEmpty(nameField.getText())) {
				JOptionPane.showMessageDialog(null, "Name is blank.", "Missing Data", JOptionPane.ERROR_MESSAGE);
			} else if (StringUtils.isEmpty(spdField.getText())) {
				JOptionPane.showMessageDialog(null, "SPD is blank.", "Missing Data", JOptionPane.ERROR_MESSAGE);
			} else if (StringUtils.isEmpty(dexField.getText())) {
				JOptionPane.showMessageDialog(null, "DEX is blank.", "Missing Data", JOptionPane.ERROR_MESSAGE);
			} else {
				Person p = new Hero();
				p.setName(nameField.getText());
				p.setSpeed(Integer.parseInt(spdField.getText()));
				p.setDex(Integer.parseInt(dexField.getText()));
				p.setPartyMember(partyCheck.isSelected());
				listModel.addElement(p);
				
				logger.debug("Added " + p.getName() + " to list.");
			}
		}
	};

	protected Action updateAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Update");
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
		}

		public void actionPerformed(ActionEvent e) {
			if (StringUtils.isEmpty(nameField.getText())) {
				JOptionPane.showMessageDialog(null, "Name is blank.", "Missing Data", JOptionPane.ERROR_MESSAGE);
			} else if (StringUtils.isEmpty(spdField.getText())) {
				JOptionPane.showMessageDialog(null, "SPD is blank.", "Missing Data", JOptionPane.ERROR_MESSAGE);
			} else if (StringUtils.isEmpty(dexField.getText())) {
				JOptionPane.showMessageDialog(null, "DEX is blank.", "Missing Data", JOptionPane.ERROR_MESSAGE);
			} else if (list.getSelectedIndex() < 0) {
				JOptionPane.showMessageDialog(null, "No character selected", "Missing Data", JOptionPane.ERROR_MESSAGE);
			} else {
				Person p = (Person)listModel.getElementAt(list.getSelectedIndex());
				p.setName(nameField.getText());
				p.setSpeed(Integer.parseInt(spdField.getText()));
				p.setDex(Integer.parseInt(dexField.getText()));
				p.setPartyMember(partyCheck.isSelected());
				listModel.setElementAt(p, list.getSelectedIndex());
				
				logger.debug("Updated " + p.getName() + ".");
			}
		}
	};

	protected Action deleteAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Delete");
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
		}

		public void actionPerformed(ActionEvent e) {
			if (list.getSelectedIndex() < 0) {
				JOptionPane.showMessageDialog(null, "No character selected", "Missing Data", JOptionPane.ERROR_MESSAGE);
			} else {
				Person p = (Person)listModel.getElementAt(list.getSelectedIndex());
				listModel.removeElementAt(list.getSelectedIndex());
				logger.debug("Removed " + p.getName() + ".");
			}
		}
	};

	protected DefaultListModel listModel = new DefaultListModel(); 
		
	protected Container buildEditBar(String text, JTextField field) {
		Container result = new JPanel();

		JLabel label = new JLabel(text);
		label.setPreferredSize(new Dimension(30, 26));
		label.setHorizontalAlignment(JLabel.RIGHT);

		field.setColumns(10);

		result.add(label);
		result.add(field);

		return result;
	}
	
	protected Container buildCheckBar(String text, JCheckBox checkBox) {
		Container result = new JPanel();

		checkBox.setText(text);
		result.add(checkBox);

		return result;
	}
	
	protected void save() {
		if (listModel.size() > 0) {
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
				if (chooser.getSelectedFile().getName().indexOf('.') == -1) {
					chooser.setSelectedFile(new File(chooser.getSelectedFile().getPath() + ".xml"));
				}
				
				logger.debug("Saveing to " + chooser.getSelectedFile().getPath());
				
				Element characters = new Element("characters");
				for (int i = 0; i < listModel.size(); i++) {
					Person p = (Person)listModel.getElementAt(i);
					Element character = new Element("hero");
					character.setAttribute("party-member", Boolean.toString(p.isPartyMember()));
					character.addContent(new Element("name").setText(p.getName()));
					Element characteristics = new Element("characteristics");
					characteristics.addContent(new Element("dex").addContent(new Element("total").setText(Integer.toString(p.getDex()))));
					characteristics.addContent(new Element("spd").addContent(new Element("total").setText(Integer.toString(p.getSpeed()))));
					character.addContent(characteristics);
					characters.addContent(character);
				}
				
				Utils.saveXml(characters, chooser.getSelectedFile().getAbsolutePath());
			}
		}
	}

	protected Container buildTopPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());

		JPanel btnPanel = new JPanel();
		btnPanel.add(new JButton(newAction));
		btnPanel.add(new JButton(addAction));
		btnPanel.add(new JButton(updateAction));
		btnPanel.add(new JButton(deleteAction));
		btnPanel.add(new JButton(saveAction));
		result.add(btnPanel, BorderLayout.SOUTH);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(1, 2));

		list = new JList(listModel);
		list.addListSelectionListener(this);
		topPanel.add(new JScrollPane(list));

		JPanel sidePanel = new JPanel();
		sidePanel.add(buildEditBar("Name", (nameField = new JTextField())));
		sidePanel.add(buildEditBar("SPD", (spdField = new JTextField())));
		sidePanel.add(buildEditBar("Dex", (dexField = new JTextField())));
		sidePanel.add(buildCheckBar("Party member", (partyCheck = new JCheckBox())));
		topPanel.add(sidePanel);

		result.add(topPanel);

		return result;
	}

	/**
	 * @return Returns the characters.
	 */
	public List<Person> getCharacters() {
		return characters;
	}

	/**
	 * @param characters The characters to set.
	 */
	public void setCharacters(List<Person> characters) {
		this.characters = characters;
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent arg0) {
		if (!arg0.getValueIsAdjusting() && list.getSelectedIndex() > -1) {
			Person p = (Person)listModel.getElementAt(list.getSelectedIndex());
			nameField.setText(p.getName());
			spdField.setText(Integer.toString(p.getSpeed()));
			dexField.setText(Integer.toString(p.getDex()));
			partyCheck.setSelected(p.isPartyMember());
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	public void windowDeactivated(WindowEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	public void windowDeiconified(WindowEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent arg0) {
		for (Person p : characters) {
			listModel.addElement(p);
		}
	}
}
