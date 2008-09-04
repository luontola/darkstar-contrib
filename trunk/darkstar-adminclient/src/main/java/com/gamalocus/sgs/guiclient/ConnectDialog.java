package com.gamalocus.sgs.guiclient;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ConnectDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 3606151570549486525L;
	private JButton yesButton;
	private JButton noButton;
	private JTextField hostField, portField, userField, passField;

	public ConnectDialog(GuiAdminClient guiAdminClient) {
		super(guiAdminClient, "Connect", true);
		setSize(300, 200);
		
		getContentPane().setLayout(new GridLayout(5, 2));
		
		// Host
		getContentPane().add(new JLabel("Host:"));
		getContentPane().add(hostField = new JTextField(AdminClientPersistedSettings.getInstance().getProperty("host")));
		
		// Port
		getContentPane().add(new JLabel("Port:"));
		getContentPane().add(portField = new JTextField(AdminClientPersistedSettings.getInstance().getProperty("port")));
		
		// user
		getContentPane().add(new JLabel("User:"));
		getContentPane().add(userField = new JTextField(AdminClientPersistedSettings.getInstance().getProperty("user")));
		
		// pass
		getContentPane().add(new JLabel("Pass:"));
		getContentPane().add(passField = new JTextField(AdminClientPersistedSettings.getInstance().getProperty("pass")));
		
        yesButton = new JButton("Connect");
        yesButton.addActionListener(this);
        getContentPane().add(yesButton);        
        noButton = new JButton("Cancel");
        noButton.addActionListener(this);
        getContentPane().add(noButton);
        pack();
        setLocationRelativeTo(guiAdminClient);
	}

	public void actionPerformed(ActionEvent e) {
		System.out.println("Action:"+e.getActionCommand());
		if("Connect".equals(e.getActionCommand()))
		{
			GuiAdminClient.getInstance().doConnect(
					hostField.getText(), 
					Integer.parseInt(portField.getText()),
					userField.getText(),
					passField.getText());
		}
		setVisible(false);
		dispose();
	}
}
