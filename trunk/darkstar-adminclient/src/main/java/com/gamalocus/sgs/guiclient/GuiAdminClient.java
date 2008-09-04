package com.gamalocus.sgs.guiclient;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import com.gamalocus.sgs.adminclient.connection.AdminClientConnection;
import com.gamalocus.sgs.adminclient.connection.AdminClientConnectionFactory;
import com.gamalocus.sgs.adminclient.connection.AdminSessionListener;
import com.gamalocus.sgs.adminclient.messages.AbstractAdminMessage;
import com.gamalocus.sgs.adminclient.messages.GetNameBindings;
import com.gamalocus.sgs.guiclient.names.BoundNamesPanel;
import com.gamalocus.sgs.guiclient.topmenu.TopMenuBar;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ManagedReference;


/**
 * This GUI client allows you to connect to a server and inspect things in the data-store.
 * 
 * @author emanuel
 *
 */
public class GuiAdminClient extends JFrame implements AdminClientConnectionFactory {
	private static final long serialVersionUID = 6364433107705744258L;
	private static GuiAdminClient instance;
	private AdminClientConnection con;
	private TopMenuBar menubar;
	private JTabbedPane mainpanel;
	private ConnectDialog connectDialog;
	private BoundNamesPanel boundNames;
	private JTextArea miscOutput;

	private GuiAdminClient() {
		super("GUI Admin Client");
		
		if(instance != null)
			throw new RuntimeException("There can be only one!");
		instance = this;
		
		setSize(1024, 768);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Create the top-menu
		setJMenuBar(menubar = new TopMenuBar());
		
		// Create a tabbed-pane for stuff
		add(mainpanel = new JTabbedPane());
		
		mainpanel.addTab("Bound Names", boundNames = new BoundNamesPanel());
		mainpanel.addTab("Output", new JScrollPane(miscOutput = new JTextArea()));

		/*
		// Show our-selves.
		setVisible(true);
		
		// Initiate the connection-dialog
		connect();
		*/
	}

	public static void main(String[] args) {
		GuiAdminClient client = getInstance();
		client.setVisible(true);
		client.connect();
	}

	public AdminClientConnection getConnection() {
		return con;
	}

	public static GuiAdminClient getInstance() {
		if(instance == null)
		{
			instance = new GuiAdminClient();
		}
		return instance;
	}

	public void connect() {
		if(connectDialog == null) {
			connectDialog = new ConnectDialog(this);
		}
		connectDialog.setVisible(true);
	}

	public void doConnect(String host, int port, String username, String password) {
		
		if(con != null && con.isConnected())
		{
			con.disconnect("New Connection");
		}
		
		con = new AdminClientConnection(this, this.getClass().getClassLoader(), host, port);
		con.connectBlocking(new Runnable()
		{
			public void run() {
				onDisconnected();
			}
		});
		
		if(con.isConnected() && con.authenticate(username, password))
		{
			// Save values
			AdminClientPersistedSettings.getInstance().put("host", host);
			AdminClientPersistedSettings.getInstance().put("port", ""+port);
			AdminClientPersistedSettings.getInstance().put("user", username);
			AdminClientPersistedSettings.getInstance().put("pass", password);
			AdminClientPersistedSettings.getInstance().save();
			
			// TODO: figure out what to do once connected.
		}
		else
		{
			try
			{
				con.disconnect("Failed to authenticate");
			}
			catch(Throwable e)
			{
				
			}
			finally
			{
				con = null;
			}
		}
	}

	protected void onDisconnected() {
		System.out.println("We have been disconnected");
		
	}

	public void disconnect() {
		if(con != null && con.isConnected())
		{
			con.disconnect("Just disconnect");
		}
		con = null;
	}
	
	public boolean isConnected()
	{
		return con != null && con.isConnected();
	}

}
