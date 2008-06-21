package net.gamalocus.sgs.adminclient.messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import net.gamalocus.sgs.adminclient.connection.AdminSessionListener;

import com.gamalocus.sgs.services.datainspector.DataInspectorManager;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;

public class GetNameBindings extends AbstractAdminMessage<ArrayList<String>>
{
	private static final long serialVersionUID = -46637251279964318L;
	private final static Logger logger = 
		Logger.getLogger(GetNameBindings.class.getName());
	private String start_name;
	private int max_count;
	
	public GetNameBindings(String start_name, int max_count) {
		this.start_name = start_name;
		this.max_count = max_count;
	}
	
	@Override
	public ArrayList<String> executeOnServer(AdminSessionListener connection, ManagedReference server) 
		throws IOException, NoSuchFieldException, IllegalAccessException
	{
		return AppContext.getManager(DataInspectorManager.class).getBoundNames(start_name, max_count);
	}

}
