package net.gamalocus.sgs.services.datainspector.tasks;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ObjectNotFoundException;
import com.sun.sgs.app.Task;

/**
 * This task will remove data-objects from the data-manager recursively.
 * 
 * @author Emanuel Greisen <sgs@emanuelgreisen.dk>
 */
public class LittleDoctor implements Task, Serializable
{
	private final static Logger logger = Logger.getLogger(LittleDoctor.class.getName());
	private static final long serialVersionUID = -2647620663793391971L;
	private final ManagedReference<ManagedObject> object_ref;
	private final RemovalSpreadFilter filter;
	private final String field_url;
	private final long time_spread;
	private transient int spawned_tasks = 0;
	
	public LittleDoctor(ManagedObject object, RemovalSpreadFilter filter, String field_url)
	{
		this(object, filter, field_url, 2500);
	}
	
	public LittleDoctor(ManagedObject object, RemovalSpreadFilter filter, String field_url, long time_spread)
	{
		object_ref = AppContext.getDataManager().createReference(object);
		this.filter = filter;
		this.field_url = field_url;
		this.time_spread = time_spread;
	}

	public void run() throws Exception
	{
		try
		{
			ManagedObject obj = object_ref.get();
			
			// Should we shedule little doctors for other reachable objects
			if(filter != null)
			{
				scheduleLittleDoctors(obj, new HashSet<Object>(), field_url);
			}
			
			// Finally remove the actual object
			logger.info("LittleDoctor removes "+field_url+" ["+obj.getClass()+"@"+obj.hashCode()+"]");
			AppContext.getDataManager().removeObject(obj);
		}
		catch(ObjectNotFoundException onfe)
		{
			// Perfect, someone did it for us.
		}
	}

	@SuppressWarnings("unchecked")
	private void scheduleLittleDoctors(Object obj, HashSet<Object> handled, String field_url)
	{
		// Ensure we don't do this more than once.
		if(handled.contains(obj))
			return;
		handled.add(obj);
		
		
		// Build a list of "children"
		Hashtable<String, Object> child_objs = new Hashtable<String, Object>();
		Collection<Field> fields = getAllMemberFields(obj);
		for(Field f : fields)
		{
			f.setAccessible(true);
			try
			{
				Object val = f.get(obj);
				if(val != null)
				{
					if(val.getClass().isArray())
					{
						Class<?> comptype = val.getClass().getComponentType();
						if(!comptype.isPrimitive() && !comptype.isEnum())
						{
							Object[] arr = (Object[]) val;
							for(int i = 0; i < arr.length; i++)
							{
								Object o = arr[i];
								if(o != null)
								{
									safePut(child_objs, field_url+"."+f.getName()+"["+i+"]", o);
								}
							}
						}
					}
					else
					{
						safePut(child_objs, field_url+"."+f.getName(), val);
					}
				}
			}
			catch(IllegalAccessException iae)
			{
				// what ever...
			}
		}
		
		// Iterate the "children" to recursively add tasks.
		for(Entry<String, Object> e : child_objs.entrySet())
		{
			Object val = e.getValue();
			if(val instanceof ManagedReference)
			{
				tryScheduleLittleDoctor((ManagedReference<ManagedObject>) val, e.getKey());
			}
			else if(val.getClass().isPrimitive() || val.getClass().isEnum())
			{
				// Skip this primitives and enums
			}
			else if(val instanceof ManagedObject)
			{
				// This must be a transient field holding some tmp java-reference, skip it.
			}
			else
			{
				scheduleLittleDoctors(val, handled, e.getKey());
			}
		}
	}
	
	private static void safePut(Hashtable<String, Object> children, String key, Object child)
	{
		String endKey = key;
		int counter = 0;
		while(children.containsKey(endKey))
		{
			endKey = key+"_"+(++counter);
		}
		children.put(endKey, child);
	}

	private void tryScheduleLittleDoctor(ManagedReference<? extends ManagedObject> other_ref, String child_field_url)
	{
		try
		{
			ManagedObject other = other_ref.get();
			if(filter.removeObject(other))
			{
				AppContext.getTaskManager().scheduleTask(new LittleDoctor(other, filter, child_field_url, time_spread), (++spawned_tasks)*time_spread);
			}
			else
			{
				System.out.println("Skipping: "+child_field_url+"["+other.getClass()+"]");
			}
		}
		catch(ObjectNotFoundException onfe)
		{
			// This is fine with us.
		}
	}

	private static Collection<Field> getAllMemberFields(Object obj)
	{
		Vector<Field> fields = new Vector<Field>();
		Class<?> class1 = obj.getClass();
		while(class1 != null)
		{
			for(Field f : class1.getDeclaredFields())
			{
				// Allow member-fields (also transient to walk through hash-maps and such)
				if(!Modifier.isStatic(f.getModifiers()))
				{
					fields.add(f);
				}
			}
			class1 = class1.getSuperclass();
		}
		return fields;
	}
}
