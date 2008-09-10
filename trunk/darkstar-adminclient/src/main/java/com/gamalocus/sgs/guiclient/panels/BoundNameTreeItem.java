package com.gamalocus.sgs.guiclient.panels;

import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.gamalocus.sgs.guiclient.windows.ObjectInspectorWindow;

public class BoundNameTreeItem extends JButton
{
	boolean hasFetched = false;
	
	public BoundNameTreeItem(String bn) {
		super(new AbstractAction(bn)
		{

			public void actionPerformed(ActionEvent e) {
				System.out.println("Fetch: "+e.getActionCommand());
				new ObjectInspectorWindow<Object>(e.getActionCommand()).setVisible(true);
			}
			
		});
	}	
}