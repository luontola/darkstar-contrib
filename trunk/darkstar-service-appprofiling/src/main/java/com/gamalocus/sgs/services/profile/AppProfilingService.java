package com.gamalocus.sgs.services.profile;

import java.util.Properties;
import java.util.logging.Logger;

import com.sun.sgs.impl.sharedutil.PropertiesWrapper;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.profile.ProfileRegistrar;
import com.sun.sgs.service.DataService;
import com.sun.sgs.service.Service;
import com.sun.sgs.service.TransactionProxy;

/**
 * The MySQLService.
 * 
 * @author Emanuel Greisen
 * 
 */
public class AppProfilingService implements Service
{
	/** The name of this class. */
	static final String CLASSNAME = AppProfilingService.class.getName();
	/** the logger. */
	private final static Logger logger = Logger.getLogger(AppProfilingService.class.getName());
	ProfileRegistrar registrar;
	
	/**
	 * The constructor as it is called from SGS.
	 * 
	 * @param properties
	 * @param componentRegistry
	 * @param transProxy
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public AppProfilingService(Properties properties, ComponentRegistry componentRegistry, TransactionProxy transProxy)
	{
		// Read properties
		PropertiesWrapper wrappedProps = new PropertiesWrapper(properties);
		registrar = 
			componentRegistry.getComponent(ProfileRegistrar.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName()
	{
		return toString();
	}

	@Override
	public String toString()
	{
		return CLASSNAME;
	}

	/**
	 * do nothing.
	 */
	public void ready() throws Exception
	{
	}

	/**
	 * Here we terminate our worker thread, and close our mysql connection.
	 */
	public boolean shutdown()
	{
		return true; // ok, we are down
	}
}
