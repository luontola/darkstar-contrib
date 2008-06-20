package net.gamalocus.sgs.services.datainspector.tasks;

import java.io.Serializable;

import com.sun.sgs.app.ManagedObject;

/**
 * This filter is used to determine if an other little doctor should be spawned for
 * some other ManagedObject reachable via a MenagedReference from the object about
 * to be removed.
 * 
 * @author Emanuel Greisen
 */
public interface RemovalSpreadFilter extends Serializable
{
	boolean removeObject(ManagedObject object);
}
