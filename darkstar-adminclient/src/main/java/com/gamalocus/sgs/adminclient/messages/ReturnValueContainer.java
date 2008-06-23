package com.gamalocus.sgs.adminclient.messages;

import java.io.Serializable;

/**
 * Container for return values from admin messages. Used to pair requests with responses,
 * and to separate exceptions from regular return values.
 * 
 * @author j0rg3n
 */
public class ReturnValueContainer<RETURN_VALUE extends Serializable> implements Serializable
{
	private static final long serialVersionUID = 8628370764658109839L;
	
	private Throwable t = null;
	private RETURN_VALUE r = null;
	private long requestId;

	public ReturnValueContainer(RETURN_VALUE r, long requestId)
	{
		this.r = r;
		this.requestId = requestId;
	}

	public ReturnValueContainer(Throwable t, long requestId)
	{
		this.t = t;
		this.requestId = requestId;
	}

	public Throwable getThrowable()
	{
		return t;
	}

	public RETURN_VALUE getReturnValue()
	{
		return r;
	}

	public long getRequestId()
	{
		return requestId;
	}

	/**
	 * Did the call fail?
	 * @return
	 */
	public boolean isThrowable()
	{
		return t != null;
	}
}
