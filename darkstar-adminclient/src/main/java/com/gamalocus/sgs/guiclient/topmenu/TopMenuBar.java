package com.gamalocus.sgs.guiclient.topmenu;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.gamalocus.sgs.guiclient.GuiAdminClient;

public class TopMenuBar extends JMenuBar{

	private JMenu server_menu;

	public TopMenuBar()
	{
		add(server_menu = new JMenu("Server"));
		server_menu.add(new ConnectDisconnectMenuItem());
	}
}
