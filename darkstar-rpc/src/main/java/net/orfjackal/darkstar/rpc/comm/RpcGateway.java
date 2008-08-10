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

package net.orfjackal.darkstar.rpc.comm;

import net.orfjackal.darkstar.rpc.*;
import net.orfjackal.darkstar.rpc.core.RpcClientImpl;
import net.orfjackal.darkstar.rpc.core.RpcProxyFactory;
import net.orfjackal.darkstar.rpc.core.RpcServerImpl;
import net.orfjackal.darkstar.rpc.core.futures.FutureManager;
import net.orfjackal.darkstar.rpc.core.futures.ProxyGeneratingFuture;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * @author Esko Luontola
 * @since 15.6.2008
 */
public class RpcGateway implements RpcServer, Serializable {
    private static final long serialVersionUID = 1L;

    public static final byte REQUEST_TO_MASTER = 0;
    public static final byte RESPONSE_FROM_MASTER = 1;
    public static final byte REQUEST_TO_SLAVE = 2;
    public static final byte RESPONSE_FROM_SLAVE = 3;

    private final RpcServer server;
    private final RpcProxyFactory proxyFactory;
    private final ServiceProvider serviceProvider;

    public RpcGateway(MessageSender requestSender, MessageSender responseSender, FutureManager futureManager) {
        server = new RpcServerImpl(responseSender);
        RpcClient client = new RpcClientImpl(requestSender, futureManager);
        proxyFactory = new RpcProxyFactory(client);
        serviceProvider = proxyFactory.create(client.getServiceProvider());
    }

    public <T> ServiceReference<T> registerService(Class<T> serviceInterface, T service) {
        return server.registerService(serviceInterface, service);
    }

    public void unregisterService(ServiceReference<?> serviceRef) {
        server.unregisterService(serviceRef);
    }

    public Map<ServiceReference<?>, Object> registeredServices() {
        return server.registeredServices();
    }

    public <T> Future<Set<T>> remoteFindByType(Class<T> serviceInterface) {
        return new ProxyGeneratingFuture<T>(serviceProvider.findByType(serviceInterface), proxyFactory);
    }

    public Future<Set<?>> remoteFindAll() {
        return new ProxyGeneratingFuture(serviceProvider.findAll(), proxyFactory);
    }
}
