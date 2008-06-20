package net.gamalocus.sgs.adminclient.messages;

import java.util.EnumSet;

import net.gamalocus.sgs.adminclient.connection.AdminLevel;
import net.gamalocus.sgs.adminclient.connection.AdminSessionAuthenticator;
import net.gamalocus.sgs.adminclient.connection.AdminSessionListener;

import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ManagedReference;


/**
 * This class is used to expand your admin-levels.
 * 
 * Note. You can make other actions like this that take other
 *    authentication tokens than username/password.
 * 
 * @author emanuel
 */
public class AuthenticateWithServer extends AbstractAdminMessage<EnumSet<AdminLevel>>
{
	private static final long serialVersionUID = -2007525762718361156L;
	
	String username;
	String password;
	
	/**
	 * Client side constructor.
	 * 
	 * @param username
	 * @param password
	 */
	public AuthenticateWithServer(String username, String password)
	{
		this.username = username;
		this.password = password;
	}

	/**
	 * This should expand the users admin-levels, or throw an exception.
	 */
	@Override
	public EnumSet<AdminLevel> executeOnServer(AdminSessionListener connection, ManagedReference<AppListener> server_ref) throws Throwable
	{
		AdminSessionAuthenticator authenticator = null;
		AppListener server = server_ref.get();
		if (connection instanceof AdminSessionAuthenticator)
		{
			authenticator = (AdminSessionAuthenticator) connection;
		}
		else if (server instanceof AdminSessionAuthenticator)
		{
			authenticator = (AdminSessionAuthenticator) server;
		}
		if(authenticator != null)
		{
			EnumSet<AdminLevel> new_levels = authenticator.getAdminLevel(username, password);
			connection.addAdminLevels(new_levels);
			return connection.getAdminLevels();
		}
		else
		{
			throw new RuntimeException("Neither the AdminSessionListener["+connection.getClass().getSimpleName()+"] nor the AppListener["+server.getClass().getSimpleName()+"] implemented the AdminClientAuthenticator interface, we cannot authenticate if that is the case.");
		}
	}
	
	/**
	 * Anyone can try to expand their AdminLevels.
	 */
	@Override
	public EnumSet<AdminLevel> getAdminLevels()
	{
		return EnumSet.allOf(AdminLevel.class);
	}
}
