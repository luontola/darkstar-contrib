package com.gamalocus.sgs.services.hotbackup;


/**
 * The manager for MySQL lazy synchronization.
 * 
 * @author Emanuel Greisen
 * 
 */
public class HotBackupManager
{
	/**
	 * The service backing this manager.
	 */
	private HotBackupService service;

	/**
	 * This is the constructor of the manager, called by SGS.
	 * 
	 * @param service
	 */
	public HotBackupManager(HotBackupService service)
	{
		this.service = service;
	}
}

