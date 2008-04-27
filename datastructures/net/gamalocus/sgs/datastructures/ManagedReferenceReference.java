package net.gamalocus.sgs.datastructures;

import java.io.Serializable;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

/**
 * A reference to a reference, making it possible to change a reference on an object without obtaining
 * a write-lock on the object.
 * 
 * @author Emanuel Greisen
 *
 * @param <T>
 */
public class ManagedReferenceReference<T extends Serializable> implements Serializable
{
	private static final long serialVersionUID = 158691317370364659L;
	ManagedReference reference;

	public ManagedReferenceReference()
	{
		reference = AppContext.getDataManager().createReference(new ManagedReferenceHolder<T>());
	}
	
	public T get()
	{
		return getRef().get();
	}

	public void set(T object)
	{
		getRef().set(object);
	}
	
	@SuppressWarnings("unchecked")
	private ManagedReferenceHolder<T> getRef()
	{
		return ((ManagedReferenceHolder<T>)reference.get(Object.class));
	}
	/**
	 * This will destroy the reference reference, and if we own the object destroy it too.
	 */
	public void destroy()
	{
		AppContext.getDataManager().removeObject(getRef());
		reference = null;
	}
}

class ManagedReferenceHolder<T extends Serializable> implements Serializable, ManagedObject
{
	private static final long serialVersionUID = -8356372788817959755L;
	ManagedReference reference;
	
	@SuppressWarnings("unchecked")
	T get()
	{
		return (T) (reference != null ? reference.get(Object.class) : null);
	}

	public void set(T object)
	{
		AppContext.getDataManager().markForUpdate(this);
		reference = object != null ? AppContext.getDataManager().createReference((ManagedObject) object) : null;
	}
}

