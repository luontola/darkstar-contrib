package net.gamalocus.sgs.adminclient.connection;

import java.io.Serializable;

/**
 * Every AdminClient connection must have a factory that is serializable such that 
 * if a reference needs to be serialized it can re-obtain the connection or at least
 * create a new one.
 * 
 * @author emanuel
 */
public interface AdminClientConnectionFactory extends Serializable
{
	AdminClientConnection getConnection();
}
