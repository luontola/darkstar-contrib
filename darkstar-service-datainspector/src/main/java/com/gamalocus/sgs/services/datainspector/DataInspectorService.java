package com.gamalocus.sgs.services.datainspector;

import java.math.BigInteger;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ObjectNotFoundException;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.service.DataService;
import com.sun.sgs.service.Service;
import com.sun.sgs.service.TransactionProxy;

/**
 * The MySQLService.
 * 
 * @author Emanuel Greisen
 * 
 */
public class DataInspectorService implements Service
{
	/** The name of this class. */
	private static final String CLASSNAME = DataInspectorService.class.getName();
	/** the logger. */
	private final static Logger logger = Logger.getLogger(DataInspectorService.class.getName());

    // a proxy providing access to the transaction state
    static TransactionProxy transactionProxy = null;

    // the data service used in the same context
    static DataService dataService;

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
	public DataInspectorService(Properties properties, ComponentRegistry componentRegistry, TransactionProxy transProxy) throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		// Read properties
		/*
		PropertiesWrapper wrappedProps = new PropertiesWrapper(properties);
		dbhost = wrappedProps.getProperty(DBHOST_PROP, "localhost");
		dbname = wrappedProps.getProperty(DBNAME_PROP, "sgs-server");
		dbuser = wrappedProps.getProperty(DBUSER_PROP, "sgs-user");
		dbpass = wrappedProps.getProperty(DBPASS_PROP, ":-)");
		*/
		
		// Get the ResourceCoordinator
		transactionProxy = transProxy;
		dataService = transProxy.getService(DataService.class);
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
	 * Create our worker thread.
	 */
	public void ready() throws Exception
	{
		// ok, we are on
	}

	/**
	 * Here we terminate our worker thread, and close our mysql connection.
	 */
	public boolean shutdown()
	{
		return true; // ok, we are down
	}
	
	public ManagedObject getObject(BigInteger object_id)
	{
		try
		{
			return (ManagedObject)dataService.createReferenceForId(object_id).get();
		}
		catch(ObjectNotFoundException e)
		{
			return null;
		}
	}

	public Vector<BigInteger> getBoundIds(BigInteger start_id, int max_count)
	{
		Vector<BigInteger> names = new Vector<BigInteger>();
		do
		{
			start_id = dataService.nextObjectId(start_id);
			if(start_id == null)
				break;
			names.add(start_id);
			max_count--;
		}
		while(max_count > 0);
		
		return names;
	}
}
