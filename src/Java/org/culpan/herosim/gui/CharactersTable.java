package org.culpan.herosim.gui;

import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.table.TableModel;

public class CharactersTable extends JTable {
	JToolTip _toolTip;
	
	public CharactersTable(TableModel tableModel) {
		super(tableModel);
		_toolTip = new CustomToolTip();
		_toolTip.setComponent(this);
	}
	
	public JToolTip createToolTip() {
		System.out.println("Returning tool tip object");
		return _toolTip;
	}
}
