package net.gamalocus.sgs.adminclient.messages;

import java.io.IOException;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gamalocus.sgs.adminclient.connection.AdminSessionListener;
import net.gamalocus.sgs.services.datainspector.DataInspectorManager;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ObjectNotFoundException;

public class GetManagedObjectFromReference extends AbstractAdminMessage<ManagedObjectCapsule>
{
	private static final long serialVersionUID = -8928477688187069136L;
	private final static Logger logger = 
		Logger.getLogger(GetManagedObjectFromReference.class.getName());
	private BigInteger reference_id;
	
	public GetManagedObjectFromReference(ManagedReference reference) {
		this(reference.getId());
	}
	
	public GetManagedObjectFromReference(BigInteger reference_id) {
		this.reference_id = reference_id;
	}
	
	
	
	public BigInteger getReferenceId()
	{
		return reference_id;
	}

	@Override
	public ManagedObjectCapsule executeOnServer(AdminSessionListener connection, ManagedReference server) 
		throws IOException, NoSuchFieldException, IllegalAccessException
	{
		ManagedObjectCapsule result = new ManagedObjectCapsule();
		try {
			result.object = AppContext.getManager(DataInspectorManager.class).getObject(reference_id);
		} catch(ObjectNotFoundException onfe) {
			logger.log(Level.WARNING, "Tried to get a non-existing object", onfe);
		}
		return result;
	}

}
