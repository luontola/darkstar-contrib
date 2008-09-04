package com.gamalocus.sgs.guiclient.topmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

import com.gamalocus.sgs.guiclient.GuiAdminClient;

public class ConnectDisconnectMenuItem extends JMenuItem {
	private AbstractAction connectAction;
	private AbstractAction disconnectAction;

	public ConnectDisconnectMenuItem()
	{
		super("Connect");
		connectAction = new AbstractAction("Connect") {

			public void actionPerformed(ActionEvent e) {
				GuiAdminClient.getInstance().connect();
			}
		};
		disconnectAction = new AbstractAction("Disconnect") {

			public void actionPerformed(ActionEvent e) {
				GuiAdminClient.getInstance().disconnect();
			}
		};
		addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e) {
				getAction().actionPerformed(e);
			}
			
		});
	}
	
	@Override
	public Action getAction() {
		return GuiAdminClient.getInstance().isConnected() ? disconnectAction : connectAction;
	}
	
	@Override
	public String getText() {
		return GuiAdminClient.getInstance().isConnected() ? "Disconnect" : "Connect";
	}
}
