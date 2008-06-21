package com.gamalocus.sgs.services.datainspector;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Vector;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;

/**
 * The manager for MySQL lazy synchronization.
 * 
 * @author Emanuel Greisen
 * 
 */
public class DataInspectorManager
{
	/**
	 * The service backing this manager.
	 */
	private DataInspectorService service;

	/**
	 * This is the constructor of the manager, called by SGS.
	 * 
	 * @param service
	 */
	public DataInspectorManager(DataInspectorService service)
	{
		this.service = service;
	}
	
	public ManagedObject getObject(BigInteger object_id)
	{
		return service.getObject(object_id);
	}
	
	public ArrayList<String> getBoundNames(String start_name, int max_count)
	{
		ArrayList<String> names = new ArrayList<String>();
		do
		{
			start_name = AppContext.getDataManager().nextBoundName(start_name);
			if(start_name == null)
				break;
			names.add(start_name);
			max_count--;
		}
		while(max_count > 0);
		
		return names;
	}
	
	public Vector<BigInteger> getBoundIds(BigInteger start_id, int max_count)
	{
		return service.getBoundIds(start_id, max_count);
	}
}

