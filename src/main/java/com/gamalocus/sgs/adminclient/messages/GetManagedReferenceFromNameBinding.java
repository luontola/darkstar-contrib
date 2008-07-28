package com.gamalocus.sgs.adminclient.messages;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.gamalocus.sgs.adminclient.connection.AdminSessionListener;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.ObjectNotFoundException;

public class GetManagedReferenceFromNameBinding<T> extends AbstractAdminMessage<ManagedReferenceCapsule<T>>
{
	private static final long serialVersionUID = 47693644440628236L;
	private final static Logger logger = 
		Logger.getLogger(GetManagedReferenceFromNameBinding.class.getName());
	private String bound_name;
	
	public GetManagedReferenceFromNameBinding(String bound_name) {
		this.bound_name = bound_name;
	}
	
	@Override
	public ManagedReferenceCapsule<T> executeOnServer(AdminSessionListener connection, ManagedReference<AppListener> server) 
		throws IOException, NoSuchFieldException, IllegalAccessException
	{
		ManagedReferenceCapsule<T> result = new ManagedReferenceCapsule();
		try {
			T obj = (T)AppContext.getDataManager().getBinding(bound_name);
			result.reference = AppContext.getDataManager().createReference(obj);
		} catch(ObjectNotFoundException onfe) {
			logger.log(Level.WARNING, "Tried to get a non-existing object", onfe);
		} catch(NameNotBoundException nnbe) {
			logger.log(Level.WARNING, "Tried to get a non-existing binding", nnbe);
		}
		return result;
	}

}
