package net.gamalocus.sgs.services.identity;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import com.sun.sgs.auth.Identity;
import com.sun.sgs.impl.sharedutil.PropertiesWrapper;
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
public class IdentityService implements Service
{
	/** The name of this class. */
	private static final String CLASSNAME = IdentityService.class.getName();
	/** the logger. */
	private final static Logger logger = Logger.getLogger(IdentityService.class.getName());
	
    // a proxy providing access to the transaction state
    TransactionProxy transactionProxy = null;

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
	public IdentityService(Properties properties, ComponentRegistry componentRegistry, TransactionProxy transProxy) throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		// Get the ResourceCoordinator
		transactionProxy = transProxy;
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
	}

	/**
	 * Here we terminate our worker thread, and close our mysql connection.
	 */
	public boolean shutdown()
	{
		return true;
	}
	

	public Identity getIdentity()
	{
		return transactionProxy.getCurrentOwner();
	}
}
