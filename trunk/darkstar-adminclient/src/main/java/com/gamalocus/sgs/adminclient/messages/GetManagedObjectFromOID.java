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

public class GetManagedObjectFromOID extends AbstractAdminMessage<ManagedReferenceCapsule<ManagedObject>>
{
	private static final long serialVersionUID = -5710294729939136995L;
	private long oid;
	
	public GetManagedObjectFromOID(long oid)
	{
		this.oid = oid;
	}
	
	@Override
	public ManagedReferenceCapsule<ManagedObject> executeOnServer(AdminSessionListener connection, ManagedReference<AppListener> server)
	{
		ManagedReferenceCapsule<ManagedObject> result = new ManagedReferenceCapsule<ManagedObject>();
		result.reference = AppContext.getDataManager().createReference(AppContext.getManager(DataInspectorManager.class).getObject(BigInteger.valueOf(oid)));
		return result;
	}

}
