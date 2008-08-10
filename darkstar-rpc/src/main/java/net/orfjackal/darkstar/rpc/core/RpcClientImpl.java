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

package net.orfjackal.darkstar.rpc.core;

import net.orfjackal.darkstar.rpc.*;
import net.orfjackal.darkstar.rpc.core.futures.ClientFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @author Esko Luontola
 * @since 9.6.2008
 */
public class RpcClientImpl implements RpcClient, MessageReciever, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RpcClientImpl.class);

    private final Map<Long, ClientFuture<?>> waitingForResponse = new ConcurrentHashMap<Long, ClientFuture<?>>();
    private final MessageSender requestSender;
    private long nextRequestId = 1L;

    public RpcClientImpl(MessageSender requestSender) {
        requestSender.setCallback(this);
        this.requestSender = requestSender;
    }

    public ServiceReference<ServiceProvider> getServiceProvider() {
        return new ServiceReference<ServiceProvider>(ServiceProvider.class, ServiceProvider.SERVICE_ID);
    }

    public void remoteInvokeNoResponse(long serviceId, String methodName, Class<?>[] paramTypes, Object[] parameters) {
        sendRequest(serviceId, methodName, paramTypes, parameters);
    }

    public <V> Future<V> remoteInvoke(long serviceId, String methodName, Class<?>[] paramTypes, Object[] parameters) {
        Request rq = sendRequest(serviceId, methodName, paramTypes, parameters);
        return waitForResponseTo(rq);
    }

    private Request sendRequest(long serviceId, String methodName, Class<?>[] paramTypes, Object[] parameters) {
        Request rq = new Request(nextRequestId(), serviceId, methodName, paramTypes, parameters);
        try {
            requestSender.send(rq.toBytes());
        } catch (IOException e) {
            throw new RuntimeException("Unable to invoke method " + methodName + " on service " + serviceId, e);
        }
        return rq;
    }

    private <V> ClientFuture<V> waitForResponseTo(Request rq) {
        ClientFuture<V> f = new ClientFuture<V>(rq);
        assert !waitingForResponse.containsKey(rq.requestId);
        waitingForResponse.put(rq.requestId, f);
        return f;
    }

    public void receivedMessage(byte[] message) {
        Response rsp = Response.fromBytes(message);
        ClientFuture<?> f = waitingForResponse.remove(rsp.requestId);
        if (f != null) {
            f.markDone(rsp);
        } else {
            logger.warn("Unexpected response: {}", rsp);
        }
    }

    private synchronized long nextRequestId() {
        return nextRequestId++;
    }

    public int waitingForResponse() {
        return waitingForResponse.size();
    }
}
