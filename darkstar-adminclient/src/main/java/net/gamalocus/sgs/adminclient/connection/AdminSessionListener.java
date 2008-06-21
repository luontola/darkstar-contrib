/**
 * Created on Feb 23, 2005
 *
 * @author emanuel
 */
package net.gamalocus.sgs.adminclient.connection;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gamalocus.sgs.adminclient.messages.AbstractAdminMessage;
import net.gamalocus.sgs.adminclient.messages.ReturnValueContainer;

import com.gamalocus.sgs.adminclient.Assembler;
import com.gamalocus.sgs.adminclient.Packetizer;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.ExceptionRetryStatus;
import com.sun.sgs.app.ManagedReference;

/**
 * Server side of the admin client-server connection.
 * 
 * @author emanuel
 */
public class AdminSessionListener implements ClientSessionListener, Serializable
{
	/**
	 * Maximum packet size.
	 * Currently very small to exercise the packetizing code properly.
	 * TODO Adjust to 64k
	 */
	public static final int MAX_PACKET_SIZE = 1024 * 1;

	protected static final Logger log = 
		Logger.getLogger(AdminSessionListener.class.getName()); 
	
	private static final long serialVersionUID = 3422808586756879949L;

	/**
	 * The server (AppListener) that we are running under.
	 */
	private ManagedReference<AppListener> server;
	/**
	 * The client-session.
	 */
	private ManagedReference<ClientSession> session_ref;
	/**
	 * The level of authentication achieved until now.
	 */
	EnumSet<AdminLevel> admin_levels = EnumSet.of(AdminLevel.GUEST);
	/**
	 * A package assembler used for large packages.
	 */
	Assembler<AbstractAdminMessage<?>> packet_assembler = 
		new Assembler<AbstractAdminMessage<?>>();
	
	public AdminSessionListener(AppListener server, ClientSession session)
	{
		this.server = AppContext.getDataManager().createReference(server);
		this.session_ref = AppContext.getDataManager().createReference(session);
	}

	public void disconnected(boolean graceful)
	{
		log.warning("Disconnected " + 
				(graceful ? "gracefully." : "not so gracefully."));
	}

	/**
	 * Send given object on the wire.
	 * 
	 * @param <T> Type of object to send.
	 * @param message Object to send.
	 * @return <code>false</code> on send failure.
	 */
	protected <T extends Serializable> boolean send(T message) {
		
		try {
			Packetizer<T> packetizer = new Packetizer<T>(message, MAX_PACKET_SIZE);
			for (ByteBuffer part : packetizer) {
				getClientSession().send(part);
			}
			return true;
		} catch (IOException ioe) {
			log.log(Level.WARNING, "Send data failed.", ioe);
			return false;
		}
	}

	private ClientSession getClientSession()
	{
		return session_ref.get();
	}

	public void receivedMessage(ByteBuffer message)
	{
		byte[] tmp = new byte[message.capacity()];
		message.get(tmp);
		packet_assembler.append(tmp);
		if (packet_assembler.isComplete()) {
			AbstractAdminMessage<?> clientMessage = null;
			ReturnValueContainer<?> returnValue = null; 
			try {
				clientMessage = packet_assembler.poll();
				log.info("Received admin message: " + clientMessage.getClass());

				try {
					if(hasAdminLevel(clientMessage.getAdminLevels()))
					{
						returnValue = new ReturnValueContainer<Serializable>(
								clientMessage.executeOnServer(this, server), 
								clientMessage.getRequestId());
					}
					else
					{
						throw new RuntimeException("You did not have any of the required AdminLevels: "+clientMessage.getAdminLevels());
					}
				} catch (Throwable t) {
					if (t instanceof ExceptionRetryStatus) {
						// This is a retryable exception; let SGS try again.
						throw (RuntimeException)t;
					}
					returnValue = new ReturnValueContainer<Serializable>(
							t, clientMessage.getRequestId());
				}
			} catch (IOException ioe) {
				log.log(Level.WARNING, "Received broken packetized object.", ioe);
				returnValue = new ReturnValueContainer<Serializable>(ioe, 
						clientMessage != null ? clientMessage.getRequestId() : -1);
			} catch (ClassNotFoundException cnfe) {
				log.log(Level.WARNING, "Received packetized object of unknown type.", cnfe);
				returnValue = new ReturnValueContainer<Serializable>(cnfe, 
						clientMessage != null ? clientMessage.getRequestId() : -1);
			}
			
			send(returnValue);
		}
	}
	
	public EnumSet<AdminLevel> getAdminLevels()
	{
		return admin_levels;
	}

	public void addAdminLevels(EnumSet<AdminLevel> new_levels)
	{
		admin_levels.addAll(new_levels);
	}

	/**
	 * Check if we have any of the levels specified in 'levels'.
	 * @param levels
	 * @return
	 */
	public boolean hasAdminLevel(EnumSet<AdminLevel> levels)
	{
		EnumSet<AdminLevel> tmp = EnumSet.copyOf(admin_levels);
		tmp.retainAll(levels); // return the intersection
		return tmp.size() > 0;
	}
}
