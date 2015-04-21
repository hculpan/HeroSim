/*
 * Created on Dec 10, 2004
 *  
 */
package org.culpan.herosim.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.culpan.herosim.Hero;
import org.culpan.herosim.Person;
import org.culpan.herosim.PersonManager;
import org.culpan.herosim.Utils;
import org.culpan.herosim.Villain;
import org.jdom.Element;

class CharListPanel extends Box {
    protected final static Logger logger = Logger.getLogger(CharListPanel.class);

    protected PersonManager personManager;

    protected DefaultListModel heroes = new DefaultListModel();
    protected DefaultListModel villains = new DefaultListModel();

    class CharCellRenderer extends JLabel implements ListCellRenderer {
        // This is the only method defined by ListCellRenderer.
        // We just reconfigure the JLabel each time we're called.

        public Component getListCellRendererComponent(JList list, Object value, //value
                // to
                // display
                int index, // cell index
                boolean isSelected, // is the cell selected
                boolean cellHasFocus) // the list and the cell have the focus
        {
            if (value instanceof Person) {
                Person p = (Person) value;
                setText(p.getDisplayName());
            } else {
                String s = value.toString();
                setText(s);
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

    class CharList extends JPanel {
        @SuppressWarnings("unchecked")
		protected Class personClass;
        protected DefaultListModel listModel;
        protected JList list;

        protected Person currentPerson;

        @SuppressWarnings("unchecked")
		public CharList(ListModel model, Class c) {
            setLayout(new BorderLayout());
            personClass = c;
            listModel = (DefaultListModel) model;

            list = new JList(model);
            list.setFont(list.getFont().deriveFont(16f));
            list.setCellRenderer(new CharCellRenderer());
            list.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                }

                public void mouseEntered(MouseEvent e) {
                }

                public void mouseExited(MouseEvent e) {
                }

                public void mousePressed(MouseEvent e) {
                }

                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        JList list = (JList) e.getComponent();
                        int i = list.locationToIndex(e.getPoint());
                        if (i > -1) {
                            currentPerson = (Person) list.getModel().getElementAt(i);
                            getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            });
            JScrollPane sp = new JScrollPane(list);

            JButton add = new JButton(new AbstractAction() {
                {
                    putValue(Action.NAME, "+");
                }

                public void actionPerformed(ActionEvent e) {
                    openFile(listModel, personClass);
                }
            });

            JButton del = new JButton(new AbstractAction() {
                {
                    putValue(Action.NAME, "-");
                }

                public void actionPerformed(ActionEvent e) {
                    if (list.getSelectedIndex() > -1) {
                        removePerson((Person) listModel.getElementAt(list.getSelectedIndex()), listModel);
                    }
                }
            });

            JPanel buttonPanel = new JPanel();

            buttonPanel.add(add);
            buttonPanel.add(del);

            add(sp, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        public JPopupMenu getPopupMenu() {
            JPopupMenu result = new JPopupMenu();
            result.add(new JMenuItem(new AbstractAction() {
                {
                    putValue(Action.NAME, "Stunned");
                }

                public void actionPerformed(ActionEvent e) {
                    if (personManager.isActingInCurrentPhase(currentPerson)) {
                        int selection = JOptionPane.showConfirmDialog(null, "Has " + currentPerson.getDisplayName()
                                + " already acted in this round?", "Stunned", JOptionPane.YES_NO_CANCEL_OPTION);
                        switch (selection) {
                        case JOptionPane.YES_OPTION:
                            currentPerson.setStunnedPhases(2);
                            break;
                        case JOptionPane.NO_OPTION:
                            currentPerson.setStunnedPhases(1);
                            break;
                        default:
                            logger.debug("Canceled : Not stunned");
                            break;
                        }
                    } else {
                        currentPerson.setStunnedPhases(1);
                    }

                    personManager.revalidate();
                }
            }));

            if (currentPerson != null) {
                if (!currentPerson.isUnconscious()) {
                    result.add(new JMenuItem(new AbstractAction() {
                        {
                            putValue(Action.NAME, "Unconscious");
                        }

                        public void actionPerformed(ActionEvent e) {
                            if (currentPerson != null) {
//                                currentPerson.setUnconscious(true);
                                personManager.revalidate();
                            }
                        }
                    }));
                } else {
                    result.add(new JMenuItem(new AbstractAction() {
                        {
                            putValue(Action.NAME, "Conscious");
                        }

                        public void actionPerformed(ActionEvent e) {
                            if (currentPerson != null) {
//                                currentPerson.setUnconscious(false);
                                personManager.revalidate();
                            }
                        }
                    }));
                }
            }

            return result;
        }
    }

    public CharListPanel(PersonManager personManager) {
        super(BoxLayout.Y_AXIS);

        this.personManager = personManager;

        add(new CharList(heroes, Hero.class));
        add(new CharList(villains, Villain.class));
    }

    protected void removePerson(Person p, DefaultListModel model) {
        model.removeElement(p);
        personManager.remove(p);

        getParent().invalidate();
        getParent().repaint();
    }

    @SuppressWarnings("unchecked")
	protected void openFile(DefaultListModel model, Class c) {
        try {
            JFileChooser chooser = new JFileChooser();
            //            chooser.setCurrentDirectory(new
            // File(System.getProperty("user.dir")));
            chooser.addChoosableFileFilter(new FileFilter() {
                public boolean accept(File file) {
                    return (file.isDirectory() || (file.getName().toLowerCase().endsWith(".xml")));
                }

                public String getDescription() {
                    return "XML Files";
                }
            });

            chooser.showOpenDialog(this);
            if (chooser.getSelectedFile() != null) {
                Element root = Utils.loadXml(chooser.getSelectedFile());

                Person hero = (Person) c.newInstance();
                hero.initFromXml(root);
                add(hero);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void add(Person p) {
        if (p instanceof Hero) {
            heroes.addElement(p);
        } else {
            villains.addElement(p);
        }
        personManager.add(p);

        invalidate();
        repaint();
    }
}