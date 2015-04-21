package org.culpan.herosim.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;
import org.culpan.herosim.Person;
import org.culpan.herosim.Utils;

class PersonTransferable implements Transferable {
	private final static Logger logger = Logger
			.getLogger(PersonTransferable.class);

	public final static String mimeType = DataFlavor.javaJVMLocalObjectMimeType
			+ ";class=org.culpan.herosim.Person";

	private final static DataFlavor[] dataFlavors = new DataFlavor[1];

	private Person person;

	private JComponent source;

	static {
		try {
			dataFlavors[0] = new DataFlavor(mimeType);
		} catch (Exception e) {
			Utils.notifyError(e);
		}
	}

	public PersonTransferable(Person p, JComponent source) {
		person = p;
		this.source = source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 */
	public Object getTransferData(DataFlavor arg0)
			throws UnsupportedFlavorException, IOException {
		if (arg0.equals(dataFlavors[0])) {
			logger.debug("Got data : " + person.getDisplayName());
			return person;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return dataFlavors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor arg0) {
		return arg0.equals(dataFlavors[0]);
	}

	/**
	 * @return the source
	 */
	public JComponent getSource() {
		return source;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(JComponent source) {
		this.source = source;
	}

}

public class PersonTransferHandler extends TransferHandler {
	private final static Logger logger = Logger
			.getLogger(PersonTransferHandler.class);

	DataFlavor dataFlavor;

	PhaseTrackerPanel trackerPanel;

	public int getSourceActions(JComponent c) {
		return COPY;
	}

	public PersonTransferHandler(PhaseTrackerPanel trackerPanel) {
		try {
			dataFlavor = new DataFlavor(PersonTransferable.mimeType);
			this.trackerPanel = trackerPanel;
		} catch (Exception e) {
			Utils.notifyError(e);
		}
	}

	public boolean importData(JComponent c, Transferable t) {
		logger.debug("importData called");
		if (canImport(c, t.getTransferDataFlavors())) {
			try {
				Person p = (Person) t.getTransferData(dataFlavor);
				importTarget(c, p);
				trackerPanel.updatePhaseDisplay();
				return true;
			} catch (UnsupportedFlavorException ufe) {
				Utils.notifyError(ufe);
			} catch (IOException ioe) {
				Utils.notifyError(ioe);
			}
		}

		return false;
	}

	protected Transferable createTransferable(JComponent c) {
		logger.debug("createTransferable called");
		Transferable result = null;

		if (c instanceof JTable) {
			JTable table = (JTable) c;
			if (table.getSelectedRow() > -1) {
				String name = (String) table.getValueAt(table.getSelectedRow(),
						0);
				if (name != null) {
					logger.debug("Got name " + name);
					Person p = trackerPanel.findPerson(name);
					if (p != null) {
						logger.debug("Creating PersonTransferable for "
								+ p.getDisplayName());
						result = new PersonTransferable(p, c);
					}
				}
			}
		} else if (c instanceof JList) {
			JList list = (JList) c;
			Person p = (Person) list.getSelectedValue();
			if (p != null) {
				logger.debug("Creating PersonTransferable for "
						+ p.getDisplayName());
				result = new PersonTransferable(p, c);
			}
		}

		return result;
	}

	public boolean canImport(JComponent c, DataFlavor[] flavors) {
		logger.debug("canImport called");

		if (flavors == null) {
			return false;
		}

		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(dataFlavor)) {
				return true;
			}
		}

		return false;
	}

	protected void importTarget(JComponent c, Person defender) {
		/*
		 * if (c.equals(c)) { return; }
		 */

		if (c instanceof JList) {
			JList target = (JList) c;
			Person p = (Person) target.getSelectedValue();
			if (p != null && !p.equals(defender)) {
				p.setTarget(defender);
			}
		} else if (c instanceof JTable) {
			JTable table = (JTable) c;
			String name = (String) table.getValueAt(table.getSelectedRow(), 0);
			if (name != null) {
				logger.debug("Got name " + name);
				Person p = trackerPanel.findPerson(name);
				if (p != null && !p.equals(defender)) {
					defender.setTarget(p);
				}
			}
		}
	}
}
