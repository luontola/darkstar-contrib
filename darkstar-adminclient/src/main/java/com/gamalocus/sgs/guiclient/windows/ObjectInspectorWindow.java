package com.gamalocus.sgs.guiclient.windows;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import com.gamalocus.reflect.Fields;
import com.gamalocus.sgs.adminclient.messages.GetManagedReferenceFromNameBinding;
import com.gamalocus.sgs.adminclient.messages.ManagedReferenceCapsule;
import com.gamalocus.sgs.guiclient.GuiAdminClient;
import com.sun.sgs.app.ManagedReference;

public class ObjectInspectorWindow<T> extends JFrame {

	private JPanel grid;

	//private JTree tree;
	//private DefaultMutableTreeNode rootNode;
	
	public ObjectInspectorWindow(String boundName) {
		super("Fetching: "+boundName);
		setSize(800, 400);
		
		add(new JScrollPane(grid = new JPanel()));
		
		// Render ManagedReferences
		/*
		{
			ManagedReferenceTableCellRenderer renderer = new ManagedReferenceTableCellRenderer();
			grid.setDefaultRenderer(ManagedReference.class, renderer);
			grid.setDefaultRenderer(com.gamalocus.sgs.adminclient.serialization.ManagedReferenceImpl.class, renderer);
			//grid.set
		}
		*/
		
		
		// Fetch the object
		ManagedReferenceCapsule<T> reply;
		try {
			reply = GuiAdminClient.getInstance().getConnection().sendSync(new GetManagedReferenceFromNameBinding<T>(boundName));
			if(reply != null && reply.reference != null)
			{
				setObject(reply.reference.get());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public ObjectInspectorWindow(T obj)
	{
		super("Show: "+(obj != null ? obj.getClass()+"@"+System.identityHashCode(obj) : "null"));
		setSize(800, 400);
		
		add(new JScrollPane(grid = new JPanel()));

		setObject(obj);
	}

	private void setObject(T obj) {
		//rootNode.setUserObject(obj);
		//addChilden(rootNode, obj);
		Collection<Field> allFields = Fields.getAllFields(obj.getClass());
		int row = 0;
		
		// Clear the old stuff
		grid.removeAll();
		GridLayout layout;
		grid.setLayout(layout = new GridLayout(allFields.size()+1, 2));
		for(String s : new String[]{ "Field", "Value" })
		{
			grid.add(new JLabel(s));
		}

		
		// Add the new stuff
		for(Field f : allFields)
		{
			f.setAccessible(true);
			JLabel label;
			//parentNode.add(new FieldTreeItem(object, f));
			grid.add(label = new JLabel(Modifier.toString(f.getModifiers())+" "+f.getType()+" "+f.getName()));
			label.setPreferredSize(new Dimension(380, -1));
			Object value = null;
			try {
				value = f.get(obj);
			} catch (Exception e) {
				value = "FAILED";
			}
			JComponent editor;
			if(f.getType().isEnum())
			{
				editor = new JComboBox(new Object[]{ "enum1", "enum2" } );
			}
			else if(f.getType().isPrimitive() || f.getType().equals(String.class))
			{
				editor = new JTextField(""+value);
			}
			else if(value != null && value instanceof ManagedReference)
			{
				final ManagedReference<?> reference = (ManagedReference<?>) value;
				editor = new JButton(new AbstractAction("Ref:"+reference.getId())
		    	{

					public void actionPerformed(ActionEvent e) {
						new ObjectInspectorWindow(reference.get()).setVisible(true);
					}
		    	});
			}
			else
			{
				editor = new JLabel(""+value);
			}
			grid.add(editor);
			editor.setPreferredSize(new Dimension(380, -1));
			row ++;
		}
	}

	private static <T> void addChilden(DefaultMutableTreeNode parentNode, T object) {
		Collection<Field> allFields = Fields.getAllFields(object.getClass());
		for(Field f : allFields)
		{
			f.setAccessible(true);
			parentNode.add(new FieldTreeItem(object, f));
		}
	}

}
