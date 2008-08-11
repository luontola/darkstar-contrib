/*
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.darkstar.rpc.core.futures;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import net.orfjackal.darkstar.rpc.core.protocol.Request;
import net.orfjackal.darkstar.rpc.core.protocol.Response;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Esko Luontola
 * @since 10.8.2008
 */
public class ServerFuture<V> implements RpcFuture<V>, ManagedObject, Serializable {
    private static final long serialVersionUID = 1L;

    private final Request request;
    private final FutureManager manager;
    private volatile Response response;
    private volatile boolean cancelled = false;

    public ServerFuture(Request request, FutureManager manager) {
        this.request = request;
        this.manager = manager;
    }

    public synchronized void markDone(Response response) {
        assert response.requestId == request.requestId;
        this.response = response;
        notifyAll();
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        if (isDone()) {
            return false;
        }
        manager.cancelWaitingForResponseTo(request);
        cancelled = true;
        AppContext.getDataManager().removeObject(this);
        return true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isDone() {
        return response != null;
    }

    public V get() throws InterruptedException, ExecutionException {
        if (!isDone()) {
            throw new InterruptedException("Not done; Blocking operations are not allowed on Darkstar Server");
        }
        AppContext.getDataManager().removeObject(this);
        if (response.exception != null) {
            throw new ExecutionException(response.exception);
        } else {
            return (V) response.value;
        }
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!isDone()) {
            synchronized (this) {
                unit.timedWait(this, timeout);
            }
        }
        if (!isDone()) {
            throw new TimeoutException();
        }
        return get();
    }
}
