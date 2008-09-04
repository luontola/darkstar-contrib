package com.gamalocus.sgs.simpleclient;

import java.util.ArrayList;

import com.gamalocus.sgs.adminclient.connection.AdminClientConnection;
import com.gamalocus.sgs.adminclient.connection.AdminClientConnectionFactory;
import com.gamalocus.sgs.adminclient.connection.AdminSessionListener;
import com.gamalocus.sgs.adminclient.messages.AbstractAdminMessage;
import com.gamalocus.sgs.adminclient.messages.GetNameBindings;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ManagedReference;


/**
 * This little client just connects to the given server/port with the given username/password
 * and prints out the first 100 name-bindings.
 * 
 * @author emanuel
 *
 */
public class SimpleAdminClient implements AdminClientConnectionFactory {
	private static final long serialVersionUID = 6364433107705744258L;
	private AdminClientConnection con;

	public SimpleAdminClient(String host, int port, String username, String password) {
		con = new AdminClientConnection(this, this.getClass().getClassLoader(), host, port);
		con.connectBlocking(new Runnable()
		{

			public void run() {
				System.out.println("We have been disconnected!");
			}
		
		});
		if(con.authenticate(username, password))
		{
			try {
				int getAmount = 5;
				ArrayList<String> res = null;
				String lastBinding = null;
				do
				{
					System.out.println("----- Get the next "+getAmount+" -----");
					res = con.sendSync(new GetNameBindings(lastBinding, getAmount));
					for(String nameBinding : res)
					{
						System.out.println("nameBinding:"+nameBinding);
						lastBinding = nameBinding;
					}
				}
				while(res.size() == getAmount);
				
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		con.disconnect("Goodbye");
	}

	public static void main(String[] args) {
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		String username = args[2];
		String password = args[3];
		
		SimpleAdminClient client = new SimpleAdminClient(host, port, username, password);
	}

	public AdminClientConnection getConnection() {
		return con;
	}

}
