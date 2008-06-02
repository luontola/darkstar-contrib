package net.gamalocus.sgs.services.identity;

import com.sun.sgs.auth.Identity;


/**
 * The manager for MySQL lazy synchronization.
 * 
 * @author Emanuel Greisen
 * 
 */
public class IdentityManager
{
	/**
	 * The service backing this manager.
	 */
	private IdentityService service;

	/**
	 * This is the constructor of the manager, called by SGS.
	 * 
	 * @param service
	 */
	public IdentityManager(IdentityService service)
	{
		this.service = service;
	}
	

	public Identity getIdentity()
	{
		return service.getIdentity();
	}
}

