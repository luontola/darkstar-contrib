package com.gamalocus.sgs.adminclient.messages;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.gamalocus.sgs.adminclient.connection.AdminSessionListener;
import com.gamalocus.sgs.services.datainspector.DataInspectorManager;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ObjectNotFoundException;

public class GetManagedObjectFromReference extends AbstractAdminMessage<ManagedObjectCapsule>
{
	private static final long serialVersionUID = -8928477688187069136L;
	private final static Logger logger = 
		Logger.getLogger(GetManagedObjectFromReference.class.getName());
	private Collection<BigInteger> referenceIds;
	
	public <T extends ManagedObject> GetManagedObjectFromReference(ManagedReference<T>... refs)
	{
		this(Arrays.asList(refs));
	}
	
	public <T extends ManagedObject> GetManagedObjectFromReference(Collection<ManagedReference<T>> refs) {
		ArrayList<BigInteger> ids = new ArrayList<BigInteger>(refs.size());
		for (ManagedReference<?> ref : refs)
		{
			ids.add(ref.getId());
		}
		referenceIds = ids;
	}

	/*
	public GetManagedObjectFromReference(Collection<BigInteger> reference_id) {
		this.reference_id = reference_id;
	}
	*/

	public Collection<BigInteger> getReferenceIds()
	{
		return referenceIds;
	}

	@Override
	public ManagedObjectCapsule executeOnServer(AdminSessionListener connection, ManagedReference<AppListener> server) 
		throws IOException, NoSuchFieldException, IllegalAccessException
	{
		ManagedObjectCapsule result = new ManagedObjectCapsule();
		for (BigInteger id : referenceIds)
		{
			try 
			{
				result.objects.put(id, AppContext.getManager(DataInspectorManager.class).getObject(id));
			} 
			catch(ObjectNotFoundException onfe) 
			{
				logger.log(Level.WARNING, "Tried to get a non-existing object", onfe);
			}
		}
		return result;
	}

}
