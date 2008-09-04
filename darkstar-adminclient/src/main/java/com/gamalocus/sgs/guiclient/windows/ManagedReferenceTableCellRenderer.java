package com.gamalocus.sgs.guiclient.windows;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.sun.sgs.app.ManagedReference;

public class ManagedReferenceTableCellRenderer implements TableCellRenderer {
	private static final long serialVersionUID = 140676322662397474L;

	
    public Component getTableCellRendererComponent(
            JTable table, Object referenceObj,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
    	final ManagedReference<?> reference = (ManagedReference<?>) referenceObj;
    	return new JButton(new AbstractAction("Ref:"+reference.getId())
    	{

			public void actionPerformed(ActionEvent e) {
				new ObjectInspectorWindow(reference.get()).setVisible(true);
			}
    	});
    }
}
