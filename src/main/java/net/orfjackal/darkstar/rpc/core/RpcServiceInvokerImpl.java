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
import net.orfjackal.darkstar.rpc.core.futures.FutureManager;
import net.orfjackal.darkstar.rpc.core.protocol.Request;
import net.orfjackal.darkstar.rpc.core.protocol.Response;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Future;

/**
 * @author Esko Luontola
 * @since 9.6.2008
 */
public class RpcServiceInvokerImpl implements RpcServiceInvoker, MessageReciever, Serializable {
    private static final long serialVersionUID = 1L;

    private final MessageSender requestSender;
    private final FutureManager futureManager;
    private long nextRequestId = 1L;

    public RpcServiceInvokerImpl(MessageSender requestSender, FutureManager futureManager) {
        requestSender.setCallback(this);
        this.requestSender = requestSender;
        this.futureManager = futureManager;
    }

    private synchronized long nextRequestId() {
        return nextRequestId++;
    }

    public ServiceReference<ServiceLocator> getServiceLocator() {
        return new ServiceReference<ServiceLocator>(ServiceLocator.class, ServiceLocator.SERVICE_ID);
    }

    public void remoteInvokeNoResponse(long serviceId, String methodName, Class<?>[] paramTypes, Object[] parameters) {
        sendRequest(new Request(nextRequestId(), serviceId, methodName, paramTypes, parameters));
    }

    public <V> Future<V> remoteInvoke(long serviceId, String methodName, Class<?>[] paramTypes, Object[] parameters) {
        Request request = new Request(nextRequestId(), serviceId, methodName, paramTypes, parameters);
        Future<V> future = futureManager.waitForResponseTo(request);
        try {
            // FutureManager must be in a waiting state before the request is actually sent,
            // in order to avoid concurrency problems, if the response arrives before the
            // control returns to this thread. (This may happen especially in unit tests.)
            sendRequest(request);
            return future;
        } catch (RuntimeException e) {
            // Sending message failed. Do not wait for a response - cancel the Future.
            future.cancel(true);
            throw e;
        }
    }

    private void sendRequest(Request request) {
        try {
            requestSender.send(request.toBytes());
        } catch (IOException e) {
            throw new RuntimeException("Unable to invoke method " + request.methodName + " on service " + request.serviceId, e);
        }
    }

    public void receivedMessage(byte[] message) {
        futureManager.recievedResponse(Response.fromBytes(message));
    }

    public int waitingForResponse() {
        return futureManager.waitingForResponse();
    }
}
