package net.gamalocus.sgs.adminclient.serialization;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gamalocus.sgs.adminclient.connection.AdminClientConnection;
import net.gamalocus.sgs.adminclient.connection.AdminClientConnectionFactory;
import net.gamalocus.sgs.adminclient.messages.GetManagedObjectFromReference;

import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

/**
 * This class is used by the class-loader in the admin-client to hack around the SGS implementation.
 * 
 * @author emanuel
 *
 */
public class ManagedReferenceImpl<T extends ManagedObject> implements ManagedReference<T>, Serializable
{
    /** The version of the serialized form. */
    private static final long serialVersionUID = 1;
    private final static Logger logger = Logger.getLogger(ManagedReferenceImpl.class.getName());

    /**
     * Status of a reference.
     * 
     * @author emanuel
     */
	enum Status
	{
		NOT_FETCHED,
		FETCHING,
		FAILED,
		SUCCEEDED
	}
	transient Status status = Status.NOT_FETCHED;

    /**
     * The object ID.
     *
     * @serial
     */
    final long oid;
    
    /**
     * Just a big-integer representation of the oid.
     */
    transient BigInteger id;
    
    /**
     * The local version of the object (fetched by someone).
     */
    transient ManagedObject object;

    /**
     * The connection from which we stem.
     */
	AdminClientConnectionFactory connectionFactory;

	/**
	 * The host the connection is to.
	private String connection_host;
	 */
	
	/**
	 * The port the connection is to.
	private int connection_port;
	 */
	
	/**
	 *  
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
    @SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	// Read the normal data
    	in.defaultReadObject();
    	status = Status.NOT_FETCHED;
    	
    	// Get our connection
    	if (in instanceof AdminClientConnectionObjectInputStream)
		{
			AdminClientConnectionObjectInputStream clooi = (AdminClientConnectionObjectInputStream) in;
	    	connectionFactory = clooi.getConnection().getFactory();
		}
    }
    
    /** Replaces this instance with a canonical instance. */
    private Object readResolve() throws ObjectStreamException {
    	AdminClientConnection connection = getConnection();
    	if (connection != null)
    	{
    		return connection.readResolveReference(getId(), this);
    	}
    	else
    	{
    		logger.warning(String.format("No connection available to resolve reference %d. " +
    				"Reference equality may be violated.", getId().longValue()));
    		return this;
    	}
    }
    
	/**
	 * For debugging purposes where you need to create new references on the admin-client side.
	 * 
	 * Use with care.
	 * 
	 * @param key
	 * @param oid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ManagedObject> ManagedReferenceImpl<T> 
		getInstance(AdminClientConnection connection, long oid)
	{
		ManagedReferenceImpl<T> ref = new ManagedReferenceImpl<T>(oid);
		return connection != null ? (ManagedReferenceImpl<T>)connection.readResolveReference(ref.id, ref) : ref;
	}
	
	/**
	 * Used for prefetching groups of objects.
	 */
	public void setObject(ManagedObject object)
	{
		status = Status.SUCCEEDED;
		this.object = object;
	}
	
	/**
	 * Used for prefetching groups of objects.
	 */
	public boolean isCached()
	{
		return object != null;
	}
	
	/**
	 * For Hibernate only.
	 */
	@SuppressWarnings("unused")
	private ManagedReferenceImpl() 
	{ 
		oid = -1; 
	}

    ManagedReferenceImpl(long oid) {
    	this.oid = oid;
    	this.id = BigInteger.valueOf(oid);
    }

    /**
     * Will "flush" the local java-reference, forcing a new fetch when the reference is de-referenced.
     */
    public void flush()
    {
		synchronized (this)
		{
	    	if(status != Status.FETCHING)
	    	{
	    		object = null;
	    		status = Status.NOT_FETCHED;
	    		return;
	    	}
		}
		logger.log(Level.WARNING, "tried to flush while were fetching", new Throwable());
    }
    
	@SuppressWarnings("unchecked")
	public T get() {
		
		// Fetch it?
		synchronized (this)
		{
			if(object == null)
			{
				// Only send ONE request.
				if(status == Status.NOT_FETCHED || status == Status.FAILED)
				{
					status = Status.FETCHING;
					logger.log(Level.WARNING, 
							String.format("Fetch triggered by ManagedObject.get(), id: %d.", id),
							new Throwable());
					try
					{
						object = getConnection().sendSync(
							new GetManagedObjectFromReference(this)).objects.values().iterator().next();
						status = Status.SUCCEEDED;
					}
					catch (Throwable e)
					{
						status = Status.FAILED;
						final String msg = "Could not fetch object[" + id + "]";
						logger.log(Level.WARNING, msg, e);
						throw new NullPointerException(msg);
					}
				}
			}
		}
		return (T) object;
	}
	
	/**
	 * @deprecated This method exists solely for backwards 
	 * compatibility with game snapshots pre-0.9.6 versions of SGS.
	 */
	public <T2> T2 get(Class<T2> targetClass)
	{
		return (T2)get();
	}

	public AdminClientConnection getConnection()
	{
		return connectionFactory != null ? connectionFactory.getConnection() : null;
	}

	/**
	 * No difference between for update and normal get on client.
	 */
	public T getForUpdate() {
		return get();
	}

	public BigInteger getId() {
		if(id == null) {
			id = BigInteger.valueOf(oid);
		}
		return id;
	}
}
