package com.gamalocus.sgs.guiclient.topmenu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.gamalocus.sgs.adminclient.messages.GetManagedObjectFromOID;
import com.gamalocus.sgs.adminclient.messages.ManagedReferenceCapsule;
import com.gamalocus.sgs.guiclient.GuiAdminClient;
import com.gamalocus.sgs.guiclient.windows.ObjectInspectorWindow;
import com.sun.sgs.app.ManagedObject;

public class GetObjectFromOIDMenuItem extends JMenuItem {
	private static final long serialVersionUID = -3616050871828636510L;
	private static String reply = "42";

	GetObjectFromOIDMenuItem()
	{
		super(new AbstractAction("Fetch Object By OID"){
			private static final long serialVersionUID = -8147742068317481932L;

			public void actionPerformed(ActionEvent e) {
				while(true)
				{
					reply = JOptionPane.showInputDialog("What is the OID of the managed object", reply);
					if(reply != null)
					{
						try
						{
							String parts[] = reply.replaceAll("\n", ",").replaceAll(" ", ",").split(",");
							for(String part : parts)
							{
								//System.out.println("part:"+part);
								long oid = Long.parseLong(part);
								try
								{
									ManagedReferenceCapsule<ManagedObject> ref = GuiAdminClient.getInstance().getConnection().sendSync(new GetManagedObjectFromOID(oid));
									if(ref != null && ref.reference != null)
									{
										new ObjectInspectorWindow<ManagedObject>(ref.reference.get()).setVisible(true);
									}
									else
									{
										throw new RuntimeException("Capsule or Reference was null");
									}
								}
								catch(Throwable t)
								{
									JOptionPane.showMessageDialog(null, t.getMessage());
								}
							}
							return;
						}
						catch(NumberFormatException nfe)
						{
							// Try again
						}
					}
				}
				
			}});
	}
}
