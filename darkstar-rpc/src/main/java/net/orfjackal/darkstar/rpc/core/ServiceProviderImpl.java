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

import net.orfjackal.darkstar.rpc.RpcServer;
import net.orfjackal.darkstar.rpc.ServiceHelper;
import net.orfjackal.darkstar.rpc.ServiceProvider;
import net.orfjackal.darkstar.rpc.ServiceReference;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * @author Esko Luontola
 * @since 14.6.2008
 */
public class ServiceProviderImpl implements ServiceProvider, Serializable {
    private static final long serialVersionUID = 1L;

    private final RpcServer server;

    public ServiceProviderImpl(RpcServer server) {
        this.server = server;
    }

    public Future<Set<ServiceReference<?>>> findAll() {
        Set<ServiceReference<?>> services = new HashSet<ServiceReference<?>>(server.registeredServices().keySet());
        return ServiceHelper.wrap(services);
    }

    public <T> Future<Set<ServiceReference<T>>> findByType(Class<T> serviceInterface) {
        Set<ServiceReference<T>> services = new HashSet<ServiceReference<T>>();
        for (ServiceReference<?> ref : server.registeredServices().keySet()) {
            if (ref.getServiceInterface().equals(serviceInterface)) {
                services.add((ServiceReference<T>) ref);
            }
        }
        return ServiceHelper.wrap(services);
    }
}
