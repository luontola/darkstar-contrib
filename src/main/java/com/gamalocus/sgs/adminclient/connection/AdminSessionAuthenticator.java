package com.gamalocus.sgs.adminclient.connection;

import java.util.EnumSet;

/**
 * This interface must be implemented by your AppListener.
 * 
 * @author emanuel
 */
public interface AdminSessionAuthenticator
{
	EnumSet<AdminLevel> getAdminLevel(String username, String password) throws AdminSessionAuthenticationException;
}
