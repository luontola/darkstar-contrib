/**
 * 
 */
package net.gamalocus.sgs.adminclient.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Map;
import java.util.logging.Logger;

import net.gamalocus.sgs.adminclient.connection.AdminClientConnection;

public class ClassLoaderOverridingObjectInputStream extends ObjectInputStream
{
	private final static Logger logger = Logger.getLogger(ClassLoaderOverridingObjectInputStream.class.getName());
	
	private ClassLoader replacementClassLoader;
	private Map<String, Class<?>> classReplacements;

	private AdminClientConnection connection;
	
	public ClassLoaderOverridingObjectInputStream(InputStream in,
			ClassLoader replacementClassLoader, 
			Map<String, Class<?>> classReplacements,
			AdminClientConnection connection) throws IOException
	{
		super(in);
		this.replacementClassLoader = replacementClassLoader;
		this.classReplacements = classReplacements;
		this.connection = connection;
	}
	
	@Override
	protected Class<?> resolveClass(ObjectStreamClass c) throws IOException, ClassNotFoundException
	{
		// This is needed since we MUST have a classloader to do the replacements with.
		if (replacementClassLoader == null) {
			replacementClassLoader = this.getClass().getClassLoader();
		}
		if (replacementClassLoader != null) {
			try {
				String classname = c.getName();
				Class<?> replacementClass = classReplacements.get(classname);
				if(replacementClass != null) {
					AdminClientAssembler.logger.finest(classname+" => "+replacementClass);
					//return replacementClass;
					classname = replacementClass.getCanonicalName();
				}
				Class<?> class_ = Class.forName(classname, true, 
						replacementClassLoader);
				// TODO Check the serialVersionUID against c.getSerialVersionUID()
				return class_;
			} catch (ClassNotFoundException cnfe) {
				// Ouch.
				cnfe.printStackTrace();
			} catch (NoClassDefFoundError ncdf) {
				// Ouch.
				ncdf.printStackTrace();
			}
		} 
		
		// Fallback
		return super.resolveClass(c);
	}

	public AdminClientConnection getConnection()
	{
		return connection;
	}
}