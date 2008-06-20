package net.gamalocus.sgs.adminclient.messages;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import com.sun.sgs.app.ManagedObject;


/**
 * This is used to wrap a managed object fetched from the server.
 * @author emanuel
 *
 */
public class ManagedObjectCapsule implements Serializable
{
	private static final long serialVersionUID = 6411488225621348780L;
	public final Map<BigInteger, ManagedObject> objects = 
		new HashMap<BigInteger, ManagedObject>();
}
