package net.gamalocus.sgs.services.hotbackup;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import com.sun.sgs.impl.sharedutil.PropertiesWrapper;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.kernel.Manageable;
import com.sun.sgs.kernel.ResourceCoordinator;
import com.sun.sgs.service.DataService;
import com.sun.sgs.service.Service;
import com.sun.sgs.service.TransactionProxy;

/**
 * The MySQLService.
 * 
 * @author Emanuel Greisen
 * 
 */
public class HotBackupService implements Service, Manageable
{
	/** The name of this class. */
	private static final String CLASSNAME = HotBackupService.class.getName();
	/** the logger. */
	private final static Logger logger = Logger.getLogger(HotBackupService.class.getName());
	/** Enabled property */
	private final static String PROP_ENABLED = "net.gamalocus.sgs.services.HotBackup.enabled";
	/** Interval property */
	private final static String PROP_INTERVAL = "net.gamalocus.sgs.services.HotBackup.interval";
	
	
	// An SGS-instance that we need to spin off our own thread.
	private ResourceCoordinator resource_coordinator;
	private boolean enabled;
	private boolean worker_still_running;
	private int interval;
	private Thread worker_thread;
	private String data_root;
	private String last_backup_to;

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
	public HotBackupService(Properties properties, ComponentRegistry componentRegistry, TransactionProxy transProxy) throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		/* just for searching.
		for(Entry<Object, Object> e : properties.entrySet())
		{
			System.out.println("Prop: "+e.getKey()+":"+e.getValue());
		}
		*/
		
		// Read properties
		PropertiesWrapper wrappedProps = new PropertiesWrapper(properties);
		enabled  = wrappedProps.getBooleanProperty(PROP_ENABLED, false);
		interval = wrappedProps.getIntProperty(PROP_INTERVAL, 3600); // 1 hour minuttes default
		data_root = wrappedProps.getProperty("com.sun.sgs.app.root");
		
		// Get the ResourceCoordinator
		resource_coordinator = componentRegistry.getComponent(ResourceCoordinator.class);
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
		if(enabled)
		{
			worker_thread = new Thread()
			{
				@Override
				public void run()
				{
					String backup_from = data_root+"/dsdb";
					while(enabled)
					{
						synchronized (HotBackupService.this)
						{
							try
							{
								HotBackupService.this.wait(interval * 1000);
								if(enabled)
								{
									DateFormat format = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
									String backup_to = data_root+"/dsdb_"+format.format(new Date());
									File f = new File(backup_to);
									if(f.mkdirs())
									{
										runBackup(backup_from, backup_to);
									}
								}
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						}
					}
					worker_still_running = false;
				}
			};
			worker_still_running = true;
			worker_thread.start();
		}
	}

	protected void runBackup(String backup_from, String backup_to)
	{
		logger.info("About to backup "+backup_from+" to "+backup_to);
		String cmdarray[] = {
				"db4.5_hotbackup",
				"-h",
				backup_from+"/",
				"-d",
				backup_from+"/",
				"-b",
				backup_to+"/"
		};
		
		if(tryExecute(cmdarray))
		{
			if(last_backup_to != null)
			{
				logger.info("About to TGZ "+last_backup_to);
				if(tryExecute(new String[] { "tar", "czf",last_backup_to+".tgz",last_backup_to+"/" }))
				{
					logger.info("Remove directory "+last_backup_to);
					tryExecute(new String[] { "rm", "-fr", last_backup_to+"/" });
				}
			}
			last_backup_to = backup_to;
		}
	}

	private boolean tryExecute(String[] cmdarray)
	{
		//System.out.println("Execute: "+cmdarray[0]);
		try
		{
			Process p = Runtime.getRuntime().exec(cmdarray);
			int ret = p.waitFor();
			return ret == 0;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Here we terminate our worker thread, and close our mysql connection.
	 */
	public boolean shutdown()
	{
		if(enabled)
		{
			enabled = false;
			synchronized (this)
			{
				this.notify(); // Wake up our worker (TODO: block here untill he is gone)
				while(worker_still_running)
				{
					logger.info("Waiting for HotBackupWorker to complete");
					try
					{
						this.wait(25);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		return true; // ok, we are down
	}
}
