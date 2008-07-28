package com.gamalocus.sgs.services.profile;

import java.util.Hashtable;
import java.util.logging.Logger;

import com.sun.sgs.profile.ProfileConsumer;
import com.sun.sgs.profile.ProfileOperation;
import com.sun.sgs.profile.ProfileProducer;
import com.sun.sgs.profile.ProfileRegistrar;

/**
 * The manager for MySQL lazy synchronization.
 * 
 * @author Emanuel Greisen
 * 
 */
public class AppProfilingManager implements ProfileProducer
{
	private final static Logger logger = Logger.getLogger(AppProfilingManager.class.getName());
	public static boolean is_enabled = false;
	/**
	 * The service backing this manager.
	 */
	private AppProfilingService service;
	/**
	 * An array of operations.
	 */
	private static Hashtable<String, ProfileOperation> knownOpNames = new Hashtable<String, ProfileOperation>();
	private static ProfileConsumer consumer;

	/**
	 * This is the constructor of the manager, called by SGS.
	 * 
	 * @param service
	 */
	public AppProfilingManager(AppProfilingService service)
	{
		this.service = service;
	}
	
	
	/**
	 * Report some operation.
	 * 
	 * @param opName
	 */
	public static void reportOperation(String opName)
	{
		ProfileOperation op = knownOpNames.get(opName);
		if(op == null)
		{
			if (consumer != null)
			{
				op = consumer.registerOperation(opName);
				knownOpNames.put(opName, op);
			}
		}
		if(op != null)
		{
			op.report();			
		}
		else
		{
			//Obviously not using profiling, log to fine
			logger.fine("reportOperation("+opName+")");
		}
	}


	public void setProfileRegistrar(ProfileRegistrar profileRegistrar)
	{
		consumer =
            profileRegistrar.registerProfileProducer(this);

		/*
		if (consumer != null)
		{
			for(String opName : service.opNames)
			{
				knownOpNames.put(opName, consumer.registerOperation(opName));
			}
		}
		*/
	}


	public static void startReport(String report_identifier)
	{
		// TODO: we hope it could be done in a smarter way
		reportOperation("startReport:"+report_identifier);
	}


	public void endReport()
	{
		// Currenlty do nothing.
	}
}

