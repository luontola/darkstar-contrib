package net.gamalocus.sgs.adminclient.messages;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import net.gamalocus.sgs.adminclient.connection.AdminSessionListener;

import com.gamalocus.sgs.services.datainspector.DataInspectorManager;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

/**
 * Message that calls a given method on the given object, returning the result.
 * 
 * Caveat: Matches by method name only, so behavior when calling overloaded 
 * methods is undefined.
 * 
 * @author jorgen
 */
public class CallMethodOnManagedObjectAdminMessage extends AbstractAdminMessage<Serializable>
{
	private static final long serialVersionUID = -5735228841980731774L;
	private final static Logger logger = 
		Logger.getLogger(CallMethodOnManagedObjectAdminMessage.class.getName());
	
	private BigInteger reference_id;
	private String methodName;
	private Object[] parameterValues;
	
	public CallMethodOnManagedObjectAdminMessage(ManagedReference reference, 
			String methodName,
			Serializable... parameterValues) {
		this(reference.getId(), methodName, parameterValues);
	}
	
	public CallMethodOnManagedObjectAdminMessage(BigInteger reference_id, 
			String methodName, 
			Serializable... parameterValues) {
		
		this.reference_id = reference_id;
		this.methodName = methodName;
		this.parameterValues = 
			Arrays.copyOf(parameterValues, parameterValues.length, 
					Serializable[].class);
	}
	
	@Override
	public Serializable executeOnServer(AdminSessionListener connection, ManagedReference server) 
		throws NoSuchMethodException, IllegalAccessException, 
		SecurityException, IllegalArgumentException, 
		NoSuchMethodException, InvocationTargetException
	{
		ManagedObject target = 
			AppContext.getManager(DataInspectorManager.class).getObject(reference_id);
		
		for (Method method : target.getClass().getMethods()) {
			if (method.getName().equals(methodName)) {
				return (Serializable)method.invoke(target, parameterValues);
			}
		}
		
		throw new NoSuchElementException("No method named " + methodName + 
				" found in class " + target.getClass().getName());
	}

}
