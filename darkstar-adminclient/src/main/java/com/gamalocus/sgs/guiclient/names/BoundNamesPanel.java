package com.gamalocus.sgs.guiclient.names;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;

import com.gamalocus.sgs.adminclient.messages.GetNameBindings;
import com.gamalocus.sgs.guiclient.GuiAdminClient;

public class BoundNamesPanel extends JPanel {
	
	private JPanel boundNameList;
	//private DefaultMutableTreeNode rootNode;
	

	public BoundNamesPanel() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(new JButton(new AbstractAction("Refresh")
		{
			public void actionPerformed(ActionEvent e) {
				doRefreshList();
			}
		}));
		
		
		//add(boundNameList = new JList());
		//rootNode = new DefaultMutableTreeNode("Bound Names (Press refresh)");
		add(new JScrollPane(boundNameList = new JPanel()));
		boundNameList.setLayout(new BoxLayout(boundNameList, BoxLayout.Y_AXIS));
		//PopupMenu popup;
		//boundNameList.setRootVisible(false);
		//boundNameList.expandRow(0);
		//boundNameList.add(popup = new PopupMenu());
		//popup.add(new MenuItem("Super"));
		//boundNameList.
	}

	protected void doRefreshList() {
		//final ArrayList<String> allBoundNames = new ArrayList<String>();
		//rootNode.removeAllChildren();
		//rootNode.setUserObject("Fetching...");
		//rootNode.d
		//boundNameList.expandRow(1);
		boundNameList.removeAll();
		try {
			int getAmount = 10;
			ArrayList<String> res = null;
			String lastBinding = null;
			int total = 0;
			do
			{
				//System.out.println("----- Get the next "+getAmount+" -----");
				res = GuiAdminClient.getInstance().getConnection().sendSync(new GetNameBindings(lastBinding, getAmount));
				for(String bn : res)
				{
					//rootNode.add(new BoundNameTreeItem(bn));
					boundNameList.add(new BoundNameTreeItem(bn));
					lastBinding = bn;
					total++;
				}
				//rootNode.setUserObject("Bound Names ("+total+" total)");
			}
			while(res.size() == getAmount);
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		//boundNameList.treeDidChange();
		boundNameList.invalidate();
	}
}
