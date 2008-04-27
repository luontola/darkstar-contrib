package net.gamalocus.sgs.adminclient.messages;

import java.io.Serializable;

import com.sun.sgs.app.ManagedReference;


/**
 * This is used to wrap a managed object fetched from the server.
 * @author emanuel
 *
 */
public class ManagedReferenceCapsule implements Serializable
{
	private static final long serialVersionUID = -3878595522574620947L;
	public ManagedReference reference;
}
