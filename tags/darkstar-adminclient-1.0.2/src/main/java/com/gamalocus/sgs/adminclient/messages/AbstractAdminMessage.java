package com.gamalocus.sgs.adminclient.messages;

import java.io.Serializable;
import java.util.EnumSet;


import com.gamalocus.sgs.adminclient.connection.AdminLevel;
import com.gamalocus.sgs.adminclient.connection.AdminSessionListener;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ManagedReference;

/**
 * Superclass of messages used by the admin client for telling the server 
 * things and stuff.
 * 
 * Make sure the message class exists on both ends before sending! :)
 *   
 * @author j0rg3n
 *
 */
public abstract class AbstractAdminMessage<RETURN_VALUE extends Serializable> implements Serializable
{
	/**
	 * Used for pairing request and response.
	 */
	protected long requestId = -1;
	
	public void setRequestId(long requestId)
	{
		this.requestId = requestId;
	}
	
	public long getRequestId()
	{
		return requestId;
	}

	public abstract RETURN_VALUE executeOnServer(AdminSessionListener connection, ManagedReference<AppListener> server) throws Throwable;
	
	/**
	 * Return a set of admin-levels that are allowed to execute this action.
	 * 
	 * Examples:
	 * <code>
	 * return EnumSet.of(AdminLevel.ROOT, AdminLevel.ADMIN);
	 * return EnumSet.of(AdminLevel.USER, AdminLevel.SUPER_USER);
	 * return EnumSet.allOf(AdminLevel.class);
	 * </code>
	 * 
	 * @return The admin-levels allowed to execute this message, the empty set makes it impossible to execute.
	 */
	public EnumSet<AdminLevel> getAdminLevels()
	{
		// The default behavior is to only allow execution by ROOT.
		return EnumSet.of(AdminLevel.ROOT);
	}
}
