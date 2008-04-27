package net.gamalocus.sgs.adminclient.connection;

import java.io.Serializable;

public interface ResponseListener<RETURN_VALUE extends Serializable> {
	public void remoteSuccess(final RETURN_VALUE returnValue);
	public void remoteFailure(final Throwable throwable);
}