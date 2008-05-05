package net.gamalocus.sgs.adminclient.connection;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.PasswordAuthentication;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gamalocus.sgs.adminclient.Packetizer;
import net.gamalocus.sgs.adminclient.messages.AbstractAdminMessage;
import net.gamalocus.sgs.adminclient.messages.AuthenticateWithServer;
import net.gamalocus.sgs.adminclient.messages.CallMethodOnManagedObjectAdminMessage;
import net.gamalocus.sgs.adminclient.messages.ReturnValueContainer;
import net.gamalocus.sgs.adminclient.serialization.AdminClientAssembler;
import net.gamalocus.sgs.adminclient.serialization.ManagedReferenceImpl;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;

/**
 * Admin client side of the admin client-server connection.
 *  
 * @author j0rg3n
 *
 */
public class AdminClientConnection implements SimpleClientListener, Serializable
{
	private static final long serialVersionUID = 8126759736004631186L;

	/**
	 * Convenience class when implementing {@link ResponseListener} anonymously.
	 * @author j0rg3n
	 * @param <RETURN_VALUE>
	 */
	public static class ResponseAdapter<RETURN_VALUE extends Serializable> 
		implements ResponseListener<RETURN_VALUE> {
			
		public void remoteSuccess(final RETURN_VALUE returnValue) 
		{ 
			logger.log(Level.INFO, "Remote call succeeded.", returnValue);
		}
		
		public void remoteFailure(final Throwable throwable) 
		{ 
			logger.log(Level.WARNING, "Remote call failed.", throwable);
		}
	}
	
	/**
	 * Maximum packet size.
	 * Currently very small to exercise the packetizing code properly.
	 * TODO Adjust to 64k
	 */
	public static final int MAX_PACKET_SIZE = 1024 * 1;
	
	protected static final Logger logger = 
		Logger.getLogger(AdminClientConnection.class.getName());
	
	/**
	 * The factory that created this connection.
	 */
	private final AdminClientConnectionFactory factory;
	
	/**
	 * Running counter for request ID generation.
	 */
	long requestIdCounter = 0;
	transient SimpleClient simple_client;
	Properties connection_props = new Properties();
	
	Map<Long, ResponseListener<?>> onReceiveHandlers = new HashMap<Long, ResponseListener<?>>();
	
	AdminClientAssembler<ReturnValueContainer<Serializable>> packet_assembler = null;

    /**
     * A list to avoid duplicates.
     */
    private HashMap<BigInteger, ManagedReferenceImpl> reference_map = new HashMap<BigInteger, ManagedReferenceImpl>();

	private Runnable loggedInHandler;

	private Runnable loginFailedHandler;

	private Runnable disconnectedHandler;

	/**
	 * The current admin-levels on the server.
	 */
	private EnumSet<AdminLevel> admin_levels = EnumSet.noneOf(AdminLevel.class);
    
	public AdminClientConnection(AdminClientConnectionFactory factory, ClassLoader classLoader, String host, int port)
	{
		this.factory = factory;
		packet_assembler = 
			new AdminClientAssembler<ReturnValueContainer<Serializable>>(classLoader, this);

		setHostAndPort(host, port);
		
		// Put in some class-swaps
		//packet_assembler.addClassReplacement("com.sun.sgs.impl.service.data.ManagedReferenceImpl", "net.gamalocus.cotwl2.hallserver.adminmessages.ManagedReferenceImpl");
		packet_assembler.addClassReplacement("com.sun.sgs.impl.service.data.ManagedReferenceImpl", 
				ManagedReferenceImpl.class);
	}
	
	
	public AdminClientConnectionFactory getFactory()
	{
		return factory;
	}
	
	public Class<?> addClassReplacement(String fromClass, Class<?> toClass)
	{
		return packet_assembler.addClassReplacement(fromClass, toClass);
	}

	public void setHostAndPort(String host, int port)
	{
		connection_props.setProperty("host", host);
		connection_props.setProperty("port", ""+port);
	}
	
	public PasswordAuthentication getPasswordAuthentication()
	{
		return new PasswordAuthentication("admin", new char[]{ 'n', 'o' });
	}

	/**
	 * Used by {@link ManagedReferenceImpl#readResolve} to replace it self with 
	 * an existing reference during deserialization.
	 * @param key
	 * @param ref
	 * @return
	 */
	public ManagedReferenceImpl readResolveReference(BigInteger key, ManagedReferenceImpl ref)
	{
		synchronized (reference_map)
		{
	        ManagedReferenceImpl tmp = reference_map.get(key);
	        if(tmp == null)
	        {
	        	reference_map.put(key, ref);
	        	return ref;
			}
			return tmp;
		}
	}

	public void loggedIn()
	{
		logger.info("Logged in.");
		if(loggedInHandler != null)
		{
			loggedInHandler.run();
		}
	}

	public void loginFailed(String msg)
	{
		logger.warning("Login failed: " + msg);
		if(loginFailedHandler != null)
		{
			loginFailedHandler.run();
		}
	}

	public void disconnected(boolean graceful, String msg)
	{
		logger.warning("Disconnected: " + msg);
		if("Connection refused".equals(msg))
		{
			if(loginFailedHandler != null)
			{
				loginFailedHandler.run();
			}
		}
		else if(disconnectedHandler != null)
		{
			disconnectedHandler.run();
		}
	}

	public ClientChannelListener joinedChannel(ClientChannel channel)
	{
		return null;
	}

	public void reconnected()
	{
		logger.info("Reconnected.");
	}

	public void reconnecting()
	{
		logger.info("Reconnecting...");
	}

	public boolean isConnected()
	{
		return simple_client != null && simple_client.isConnected();
	}

	public void disconnect(String string)
	{
		if(simple_client != null)
			simple_client.logout(true);
	}

	/**
	 * This will initiate the connection and call the respective handlers asynchronously.
	 * 
	 * @param loggedInHandler
	 * @param loginFailedHandler
	 * @param disconnectedHandler
	 * @return
	 */
	public boolean initiateConnection(Runnable loggedInHandler, Runnable loginFailedHandler, Runnable disconnectedHandler)
	{
		if(isConnected())
		{
			disconnect("New connection is comming");
		}
		simple_client = new SimpleClient(this);
		this.loggedInHandler = loggedInHandler;
		this.loginFailedHandler = loginFailedHandler;
		this.disconnectedHandler = disconnectedHandler;
		try {
			simple_client.login(connection_props);
			return true;
		} catch (IOException ioe) {
			logger.log(Level.WARNING, "Connection failed.", ioe);
			return false;
		}
	}
	
	
	/**
	 * Connect to the server with a blocking operation.
	 * 
	 * @param disconnectedHandler - handle disconnects
	 */
	public void connectBlocking(Runnable disconnectedHandler)
	{
		Object mutex = new Object();
		BlockingRunnable conHandler = new BlockingRunnable(mutex, "Connected!");
		BlockingRunnable failHandler = new BlockingRunnable(mutex, "Connection Failed");
		synchronized (mutex)
		{
			initiateConnection(conHandler, failHandler, disconnectedHandler);
			try
			{
				// TODO: throw an exception if we just time out
				mutex.wait(30000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		// Clear the blocking runnables
		loggedInHandler = null;
		loginFailedHandler = null;
	}


	
	/**
	 * Send given message on the wire.
	 * 
	 * @param <T> Type of message to send.
	 * @param message Message to send.
	 * @param sender Where to send the reply, if any.
	 * @return <code>false</code> on send failure.
	 */
	public <T extends AbstractAdminMessage<U>, U extends Serializable> 
	boolean send(T message, ResponseListener<U> sender) {
		if(!isConnected())
			throw new IllegalStateException("We are not connected");
		
		logger.info("AdminClientConnection.send("+message+", "+sender+")");
		try {
			// TODO Setup a timeout timer thread.
			synchronized (onReceiveHandlers) {
				message.setRequestId(++requestIdCounter);
			
				onReceiveHandlers.put(message.getRequestId(), sender);
			}
			
			// Send the package: (make sure we send all packets as 
			// one sequence using thread synchronization)
			synchronized (simple_client)
			{
				Packetizer<T> packetizer = new Packetizer<T>(message, MAX_PACKET_SIZE);
				for (byte[] part : packetizer) {
					simple_client.send(part);
				}
			}
			
			return true;
		} catch (IOException ioe) {
			logger.log(Level.WARNING, "Send data failed.", ioe);
			return false;
		}
	}
	
	/**
	 * This will send a message and return the result when it is recieved - a blocking operation.
	 * 
	 * @param <T>
	 * @param <U>
	 * @param message
	 * @return
	 * @throws Throwable
	 */
	public <T extends AbstractAdminMessage<U>, U extends Serializable> 
		U sendSync(T message) throws Throwable
	{
		Object mutex = new Object();
		BlockingResponseAdapter<U> listener = new BlockingResponseAdapter<U>(mutex);
		synchronized (mutex)
		{
			send(message, listener);
			try
			{
				mutex.wait(30000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		if(listener.success)
		{
			return listener.returnValue;
		}
		if(listener.throwable == null)
		{
			throw new RuntimeException("sendSync must have timed out (after 30 seconds)");
		}
		throw listener.throwable;		
	}

	public void receivedMessage(byte[] message)
	{
		packet_assembler.append(message);
		if (packet_assembler.isComplete()) {
			try {
				ReturnValueContainer<?> returnValue = packet_assembler.poll();
				
				ResponseListener<?> onReceiveHandler = null;
				synchronized (onReceiveHandlers) {
					onReceiveHandler = onReceiveHandlers.remove(returnValue.getRequestId());
				}
					
				if (onReceiveHandler != null) {
					if (!returnValue.isThrowable()) {
						// Safe: The argument type of objectReceived should fit, as we're pairing on
						// requestId.
						((ResponseListener<Serializable>)onReceiveHandler).remoteSuccess(returnValue.getReturnValue());
					} else {
						onReceiveHandler.remoteFailure(returnValue.getThrowable());
					}
				} else {
					logger.log(Level.WARNING, "Received return value for unknown request id " +
							returnValue.getRequestId() + ".", returnValue);
				}
			} catch (IOException ioe) {
				logger.log(Level.WARNING, "Received broken packetized object.", ioe);
			} catch (ClassNotFoundException cnfe) {
				logger.log(Level.WARNING, "Received packetized object of unknown type.", cnfe);
			}
		}
	}

	/**
	 * TODO Make it possible for the responseadapter to be typed on something other 
	 * than Serializable.
	 * 
	 * @param reference_id
	 * @param listener
	 * @param methodName
	 * @param parameterValues
	 */
	public void call(BigInteger reference_id, ResponseListener<Serializable> listener,
			String methodName, Serializable... parameterValues)
	{
		CallMethodOnManagedObjectAdminMessage msg = 
			new CallMethodOnManagedObjectAdminMessage(reference_id, methodName, parameterValues); 
		send(msg, listener);
	}

	/**
	 * Tries to do a blocking authentication and if that does not fail return true.
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean authenticate(String username, String password)
	{
		try
		{
			admin_levels = sendSync(new AuthenticateWithServer(username, password));
			return true;
		}
		catch(Throwable t)
		{
			logger.log(Level.SEVERE, "Could not authenticate", t);
			return false;
		}
	}

	public EnumSet<AdminLevel> getAdminLevels()
	{
		return admin_levels;
	}


	@Override
	protected void finalize() throws Throwable
	{
		if(isConnected())
		{
			disconnect("Garbage Collector killed us");
		}
		super.finalize();
	}

	public String getHost()
	{
		return connection_props.getProperty("host");
	}

	public int getPort()
	{
		return Integer.parseInt(connection_props.getProperty("port"));
	}
}

/**
 * Convenience class when implementing {@link ResponseListener} anonymously.
 * @author j0rg3n
 * @param <RETURN_VALUE>
 */
class BlockingResponseAdapter<RETURN_VALUE extends Serializable> 
	implements ResponseListener<RETURN_VALUE>, Serializable
{	
	private static final long serialVersionUID = 1L;
	public RETURN_VALUE returnValue;
	public Throwable throwable;
	public boolean success;
	private Object mutex;
	
	public BlockingResponseAdapter(Object mutex)
	{
		this.mutex = mutex;
	}
	
	public void remoteSuccess(final RETURN_VALUE returnValue) 
	{
		synchronized (mutex)
		{
			this.returnValue = returnValue;
			success = true;
			mutex.notify();
		}
	}
	
	public void remoteFailure(final Throwable throwable) 
	{ 
		synchronized (mutex)
		{
			this.throwable = throwable;
			success = false;
			mutex.notify();
		}
	}
}

class BlockingRunnable implements Runnable
{
	private Object mutex;
	private String message;
	
	public BlockingRunnable(Object mutex, String message)
	{
		this.mutex = mutex;
		this.message = message;
	}
	
	public void run()
	{
		synchronized (mutex)
		{
			if(message != null)
			{
				System.out.println(message);
			}
			mutex.notify();
		}		
	}
}
