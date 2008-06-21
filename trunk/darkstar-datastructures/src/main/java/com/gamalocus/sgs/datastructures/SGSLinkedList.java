package com.gamalocus.sgs.datastructures;

import java.io.Serializable;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

/**
 * This linked list has the the advantage that you can put new elements in the head while removing from the tail
 * without getting a point of contention.
 * 
 * @author Emanuel Greisen
 *
 */
public class SGSLinkedList<T extends Serializable> implements Serializable, ManagedObject
{
	private static final long serialVersionUID = 3750527458067109503L;
	private ManagedReferenceReference<ListElement<T>> head;
	private ManagedReferenceReference<ListElement<T>> tail;
	private boolean owner;
	
	/**
	 * Construct an empty linked list.
	 * @param owner - indicates if the list owns the elements, hence they are deleted as they are moved from the front.
	 */
	public SGSLinkedList(boolean owner)
	{
		this.owner = owner;
		head = new ManagedReferenceReference<ListElement<T>>();
		tail = new ManagedReferenceReference<ListElement<T>>();
	}
	
	/**
	 * Add an element to the back of the list.
	 * @param object
	 */
	public void add(T object)
	{
		// The very first element is a special case.
		ListElement<T> element = tail.get();
		if(element == null)
		{
			element = new ListElement<T>(object);
			head.set(element);
			tail.set(element);
		}
		else
		{
			ListElement<T> new_tail = new ListElement<T>(object);
			element.setNext(new_tail);
			tail.set(new_tail);
		}
	}
	
	/**
	 * This will remove the front element if the list is not empty and return it. If the list is set to own the elements
	 * then the element will also be removed from the DataStore.
	 * @return
	 */
	public T removeFront()
	{
		ListElement<T> element = head.get();
		if(element == null)
		{
			return null;
		}
		else
		{
			T res = element.getElement();
			ListElement<T> next = element.getNext();
			
			// If these are equal there was only one element in the list.
			if(next == null)
			{
				head.set(null);
				tail.set(null);
			}
			else
			{
				head.set(next);
			}
			
			// Remove the managed element object
			AppContext.getDataManager().removeObject(element);
			
			// If we own the actual list element values we destroy it from the data-store before returning it.
			if(owner && res instanceof ManagedObject)
			{
				AppContext.getDataManager().removeObject(res);
			}
			
			// Finally return the result.
			return res;
		}
	}
}

/**
 * This is an element in the list, holding a managed reference to the actual value of the element, and a
 * reference to the next list element in the list.
 * 
 * @author emanuel
 *
 * @param <T>
 */
class ListElement<T extends Serializable> implements Serializable, ManagedObject
{
	private static final long serialVersionUID = -1468269894845172962L;
	T object;
	ManagedReference<T> object_ref;
	ManagedReference<ListElement<T>> next;
	
	public ListElement(T object)
	{
		if(object instanceof ManagedReference)
		{
			this.object_ref = AppContext.getDataManager().createReference(object);
		}
		else
		{
			this.object = object;
		}
	}

	public ListElement<T> getNext()
	{
		return next != null ? next.get() : null;
	}

	public T getElement()
	{
		return object != null ? object : object_ref.get();
	}

	public void setNext(ListElement<T> next)
	{
		this.next = next != null ? AppContext.getDataManager().createReference(next) : null;
	}
}