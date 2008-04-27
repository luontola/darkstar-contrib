package net.gamalocus.sgs.adminclient.messages;

import java.io.Serializable;

import com.sun.sgs.app.ManagedObject;


/**
 * This is used to wrap a managed object fetched from the server.
 * @author emanuel
 *
 */
public class ManagedObjectCapsule implements Serializable
{
	private static final long serialVersionUID = 6411488225621348780L;
	public ManagedObject object;
}
