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
	
	private final ClassLoader classLoader;
	private Map<String, ObjectStreamClass> classReplacements;

	public CustomClassLoaderObjectInputStream(InputStream in, ClassLoader classLoader,
			Map<String, ObjectStreamClass> classReplacements) throws IOException
	{
		super(in);
		this.classLoader = classLoader;
		this.classReplacements = classReplacements;
	}

	public CustomClassLoaderObjectInputStream(InputStream in,  
			Map<String, ObjectStreamClass> classReplacements) throws IOException
	{
		this(in, null, classReplacements);
	}

	public CustomClassLoaderObjectInputStream(InputStream in,  
			ClassLoader classLoader) throws IOException
	{
		this(in, classLoader, NO_REPLACEMENTS);
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
		
		// Try to get it first by the default resolve method.
		// This is important, as this is where the commonly accessible classes will come from,
		// such as java.lang.*. If we get these from the custom class loader, we can end up
		// with very odd class cast exceptions.
		try
		{
			return super.resolveClass(desc);
		}
		catch (ClassNotFoundException cnfe)
		{
			if (classLoader != null)
			{
				logger.finest(String.format("Default implementation unable to find class %s.",
						desc.getName()));
			}
			else
			{
				throw cnfe;
			}
		}
		
		// Finally, use the supplied custom class loader
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
			logger.log(Level.FINE, 
					String.format("Unable to verify serialVersionUID of class %s.", desc.getName()), e);
		}
			
		return class_;
	}
}