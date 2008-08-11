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

import com.sun.sgs.app.ManagedObject;
import net.orfjackal.darkstar.rpc.ServiceReference;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * @author Esko Luontola
 * @since 11.8.2008
 */
public class ManagedRpcGateway implements RpcGateway, ManagedObject, Serializable {
    private static final long serialVersionUID = 1L;

    private final RpcGateway gateway;

    public ManagedRpcGateway(RpcGateway gateway) {
        this.gateway = gateway;
    }

    public <T> ServiceReference<T> registerService(Class<T> serviceInterface, T service) {
        return gateway.registerService(serviceInterface, service);
    }

    public void unregisterService(ServiceReference<?> serviceRef) {
        gateway.unregisterService(serviceRef);
    }

    public Map<ServiceReference<?>, Object> registeredServices() {
        return gateway.registeredServices();
    }

    public <T> Future<Set<T>> remoteFindByType(Class<T> serviceInterface) {
        return gateway.remoteFindByType(serviceInterface);
    }

    public Future<Set<?>> remoteFindAll() {
        return gateway.remoteFindAll();
    }
}
