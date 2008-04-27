package net.gamalocus.sgs.adminclient.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.logging.Logger;

import net.gamalocus.sgs.adminclient.Assembler;
import net.gamalocus.sgs.adminclient.connection.AdminClientConnection;

public class AdminClientAssembler<T extends Serializable> extends Assembler<T>
{
	private static final long serialVersionUID = 6958154813355166515L;
	final static Logger logger = Logger.getLogger(AdminClientAssembler.class.getName());
	
	protected Hashtable<String, Class<?>> classReplacements = new Hashtable<String, Class<?>>();
	protected transient ClassLoader replacementClassLoader = null;
	protected AdminClientConnection connection;
	
	public AdminClientAssembler(ClassLoader replacementClassLoader, AdminClientConnection connection)
	{
		super();
		this.replacementClassLoader = replacementClassLoader;
		this.connection = connection;
	}
	
	public Class<?> addClassReplacement(String fromClass, Class<?> toClass) {
		return classReplacements.put(fromClass, toClass);
	}
	
	public Class<?> removeClassReplacement(String fromClass) {
		return classReplacements.remove(fromClass);
	}
	
	
	@Override
	protected ObjectInputStream getObjectInputStream(InputStream in) throws IOException
	{
		return new ClassLoaderOverridingObjectInputStream(in, replacementClassLoader, classReplacements, connection);
	}
}
