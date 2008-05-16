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
import net.gamalocus.sgs.adminclient.connection.ResponseListener;
import net.gamalocus.sgs.adminclient.messages.GetManagedObjectFromReference;
import net.gamalocus.sgs.adminclient.messages.ManagedObjectCapsule;

import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

/**
 * This class is used by the class-loader in the admin-client to hack around the SGS implementation.
 * 
 * @author emanuel
 *
 */
public class ManagedReferenceImpl<T> implements ManagedReference<T>, Serializable
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
    	return getConnection().readResolveReference(getId(), this);
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
	public static ManagedReferenceImpl getInstance(AdminClientConnection connection, long oid)
	{
		ManagedReferenceImpl ref = new ManagedReferenceImpl(oid);
		return connection.readResolveReference(ref.id, ref);
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
    
    /**
     * TODO: fetch the object.
     */
	@SuppressWarnings("unchecked")
	public T get() {
		
		// Fetch it?
		synchronized (this)
		{
			if(object == null)
			{
				// Only send ONE request.
				if(status == Status.NOT_FETCHED)
				{
					status = Status.FETCHING;
					try
					{
						object = getConnection().sendSync(
							new GetManagedObjectFromReference(this)).object;
						status = Status.SUCCEEDED;
					}
					catch (Throwable e)
					{
						status = Status.FAILED;
						logger.log(Level.WARNING, "Could not fetch object["+id+"]", e);
					}
				}
			}
		}
		return (T) object;
	}

	private AdminClientConnection getConnection()
	{
		/*
		if(connection == null)
		{
			connection = AdminClientConnection.getAdminClientConnectionPool().getConnection(connection_host, connection_port);
		}
		*/
		return connectionFactory.getConnection();
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
