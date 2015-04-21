package org.culpan.herosim.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;
import org.culpan.herosim.Person;

abstract class StringTransferHandler extends TransferHandler {

	protected abstract String exportString(JComponent c);

	protected abstract void importString(JComponent c, String str);

	protected abstract void cleanup(JComponent c, boolean remove);

	protected Transferable createTransferable(JComponent c) {
		return new StringSelection(exportString(c));
	}

	public int getSourceActions(JComponent c) {
		return COPY;
	}

	public boolean importData(JComponent c, Transferable t) {
		if (canImport(c, t.getTransferDataFlavors())) {
			try {
				String str = (String) t.getTransferData(DataFlavor.stringFlavor);
				importString(c, str);
				return true;
			} catch (UnsupportedFlavorException ufe) {
			} catch (IOException ioe) {
			}
		}

		return false;
	}

	protected void exportDone(JComponent c, Transferable data, int action) {
		cleanup(c, action == MOVE);
	}

	public boolean canImport(JComponent c, DataFlavor[] flavors) {
		for (int i = 0; i < flavors.length; i++) {
			if (DataFlavor.stringFlavor.equals(flavors[i])) {
				return true;
			}
		}
		return false;
	}
}

public class TrackerTargetTransferHandler extends StringTransferHandler {
	private final static Logger logger = Logger.getLogger(TrackerTargetTransferHandler.class);
	
	private int[] indices = null;

	private int addIndex = -1; // Location where items were added

	private int addCount = 0; // Number of items added.
	
	private JComponent source = null;

	// Bundle up the selected items in the list
	// as a single string, for export.
	protected String exportString(JComponent c) {
		logger.debug("Exporting data...");
		JTable list = (JTable) c;
		indices = list.getSelectedRows();
		Object[] values = new Object[indices.length];
		for (int i = 0; i < values.length; i++) {
			logger.debug("Adding " + list.getValueAt(indices[i], 0).toString().trim() + " to data.");
			values[i] = list.getValueAt(indices[i], 0).toString().trim();
		}

		StringBuffer buff = new StringBuffer();

		for (int i = 0; i < values.length; i++) {
			Object val = values[i];
			buff.append(val == null ? "" : val.toString());
			if (i != values.length - 1) {
				buff.append("\n");
			}
		}
		
		source = null;

		return buff.toString();
	}

	// Take the incoming string and wherever there is a
	// newline, break it into a separate item in the list.
	protected void importString(JComponent c, String str) {
		if (c == source) {
			return;
		}
		
		JList target = (JList) c;
		DefaultListModel listModel = (DefaultListModel) target.getModel();
		int index = target.getSelectedIndex();
		Person p = (Person)target.getSelectedValue();
		if (p != null) {
//			p.setTarget(target);
		}

		// Prevent the user from dropping data back on itself.
		// For example, if the user is moving items #4,#5,#6 and #7 and
		// attempts to insert the items after item #5, this would
		// be problematic when removing the original items.
		// So this is not allowed.
		if (indices != null && index >= indices[0] - 1 && index <= indices[indices.length - 1]) {
			indices = null;
			return;
		}

		int max = listModel.getSize();
		if (index < 0) {
			index = max;
		} else {
			index++;
			if (index > max) {
				index = max;
			}
		}
		addIndex = index;
		String[] values = str.split("\n");
		addCount = values.length;
		for (int i = 0; i < values.length; i++) {
			listModel.add(index++, values[i]);
		}
	}

	// If the remove argument is true, the drop has been
	// successful and it's time to remove the selected items
	// from the list. If the remove argument is false, it
	// was a Copy operation and the original list is left
	// intact.
	protected void cleanup(JComponent c, boolean remove) {
		if (remove && indices != null) {
			JList source = (JList) c;
			DefaultListModel model = (DefaultListModel) source.getModel();
			// If we are moving items around in the same list, we
			// need to adjust the indices accordingly, since those
			// after the insertion point have moved.
			if (addCount > 0) {
				for (int i = 0; i < indices.length; i++) {
					if (indices[i] > addIndex) {
						indices[i] += addCount;
					}
				}
			}
			for (int i = indices.length - 1; i >= 0; i--) {
				model.remove(indices[i]);
			}
		}
		indices = null;
		addCount = 0;
		addIndex = -1;
	}
}
