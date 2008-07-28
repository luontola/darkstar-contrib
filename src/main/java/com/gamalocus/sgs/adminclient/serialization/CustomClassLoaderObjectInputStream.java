package com.gamalocus.sgs.adminclient.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Note: This class is related to {@link com.gamalocus.sgs.adminclient.serialization.ClassLoaderOverridingObjectInputStream}.
 * 
 * {@link com.gamalocus.sgs.adminclient.serialization.ClassLoaderOverridingObjectInputStream} cannot be used
 * instead because it is more specialized.
 * 
 * {@link com.gamalocus.sgs.adminclient.serialization.ClassLoaderOverridingObjectInputStream} cannot be derived
 * from this class because of dependency issues.
 * 
 * This is why we have to have these two, very similar classes.
 * 
 * @author jorgen
 */
public class CustomClassLoaderObjectInputStream extends ObjectInputStream
{
	/**
	 * Workaround for lack of cause parameter in pre-Java 6 versions of IOException.
	 * 
	 * @author j0rg3n
	 */
	public static class IOExceptionWithCause extends IOException
	{
		private static final long serialVersionUID = 6712468549430960247L;

		public IOExceptionWithCause(String message, Throwable cause)
		{
			super(message);
			initCause(cause);
		}
	}

	private final static Logger logger = 
		Logger.getLogger(CustomClassLoaderObjectInputStream.class.getName());

	private static final Map<String, ObjectStreamClass> NO_REPLACEMENTS = Collections.emptyMap();
	
	private final ClassLoader[] classLoaders;
	private Map<String, ObjectStreamClass> classReplacements;

	/**
	 * You can supply <code>null</code> as class loader to resolve class 
	 * by the default resolve method.
	 * 
	 * It is important to list this first if the next class loader does not have 
	 * the default loader as its parent, as this is where the commonly accessible 
	 * classes will come from, such as java.lang.*. 
	 * 
	 * If we get these from the custom class loader, we can end up with very odd 
	 * class cast exceptions.
	 */
	public CustomClassLoaderObjectInputStream(InputStream in, 
			Map<String, ObjectStreamClass> classReplacements,
			ClassLoader... classLoaders) throws IOException
	{
		super(in);
		this.classLoaders = classLoaders;
		this.classReplacements = classReplacements;
		
		if (classLoaders.length == 0)
		{
			throw new IllegalArgumentException("You must supply at least one class loader.");
		}
	}

	public CustomClassLoaderObjectInputStream(InputStream in,  
			ClassLoader... classLoaders) throws IOException
	{
		this(in, NO_REPLACEMENTS, classLoaders);
	}
	
	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
	{
		// First, check if the class should be replaced with a different class.
		ObjectStreamClass replacementDesc = classReplacements.get(desc.getName());
		if(replacementDesc != null) 
		{
			logger.finer(String.format("Replacing stream class %s with %s.",
					desc.getName(), replacementDesc.getName()));
			desc = replacementDesc;
		}
		
		for (int i = 0; i < classLoaders.length; ++i)
		{
			try
			{
				if (classLoaders[i] == null)
				{
					return super.resolveClass(desc);
				}
				else
				{
					return customResolveClass(desc, classLoaders[i]);
				}
			}
			catch (ClassNotFoundException e)
			{
				if (i + 1 < classLoaders.length)
				{
					if (logger.isLoggable(Level.FINEST))
					{
						// Just log; there are more opportunities.
						logger.log(Level.FINEST, 
								String.format("%s resolution failed for class %s.", 
								classLoaders[i] != null ? classLoaders[i].getClass().getName() : "Default", 
								desc.getName()), e);
					}
				}
				else
				{
					throw e;
				}
			}
		}
		
		// We should never arrive here.
		throw new AssertionError();
	}

	private Class<?> customResolveClass(ObjectStreamClass desc, ClassLoader classLoader) throws ClassNotFoundException, IOExceptionWithCause
	{
		// Note: We do not use loadClass directly, as it does not handle array classes
		// well on JDK6.
		Class<?> class_ = Class.forName(desc.getName(), false, classLoader);
		try
		{
			Field serialVersionUIDField = class_.getDeclaredField("serialVersionUID");
			serialVersionUIDField.setAccessible(true);
			long serialVersionUID = ((Long) serialVersionUIDField.get(null)).longValue();
			
			if (serialVersionUID != desc.getSerialVersionUID())
			{
				throw new ClassNotFoundException(String.format("Mismatch in " +
						"%s.serialVersionUID. Expected %d, got %d.", 
						class_.getName(), 
						desc.getSerialVersionUID(), 
						serialVersionUID));
			}
		}
		catch (IllegalAccessException e)
		{
			throw new IOExceptionWithCause(String.format("Field %s.serialVersionUID not accessible.", class_.getName()), e);
		}
		catch (NoSuchFieldException e)
		{
			// Do nothing; we cannot verify serialVersionUID if not specified.
			// FIXME Do this the way that the java serialization does it.
			if (logger.isLoggable(Level.WARNING))
			{
				logger.log(Level.WARNING, String.format("Unable to verify serialVersionUID " +
						"of class %s.", desc.getName()), e);
			}
		}
		return class_;
	}
}