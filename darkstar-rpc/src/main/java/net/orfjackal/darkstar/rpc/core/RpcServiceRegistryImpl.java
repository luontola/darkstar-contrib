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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Esko Luontola
 * @since 10.6.2008
 */
public class RpcServiceRegistryImpl implements RpcServiceRegistry, MessageReciever, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RpcServiceRegistryImpl.class);

    private static final long FIRST_SERVICE_ID = 1L;

    private final Map<Long, ServiceHolder<?>> services;
    private final MessageSender responseSender;
    private long nextServiceId = FIRST_SERVICE_ID;

    public RpcServiceRegistryImpl(MessageSender responseSender) {
        this(responseSender, new ConcurrentHashMap<Long, ServiceHolder<?>>());
    }

    public RpcServiceRegistryImpl(MessageSender responseSender, Map<Long, ServiceHolder<?>> backingMap) {
        assert backingMap.size() == 0;
        responseSender.setCallback(this);
        this.services = backingMap;
        this.responseSender = responseSender;
        registerDefaultServices();
    }

    private synchronized long nextServiceId() {
        return nextServiceId++;
    }

    private void registerDefaultServices() {
        registerServiceById(ServiceLocator.class, new ServiceLocatorImpl(this), ServiceLocator.SERVICE_ID);
    }

    public <T> ServiceReference<T> registerService(Class<T> serviceInterface, T service) {
        return registerServiceById(serviceInterface, service, nextServiceId());
    }

    private <T> ServiceReference<T> registerServiceById(Class<T> serviceInterface, T service, long serviceId) {
        assert !services.containsKey(serviceId);
        ServiceReference<T> ref = new ServiceReference<T>(serviceInterface, serviceId);
        services.put(serviceId, new ServiceHolder<T>(service, ref));
        return ref;
    }

    public void unregisterService(ServiceReference<?> serviceRef) {
        services.remove(serviceRef.getServiceId());
    }

    public Map<ServiceReference<?>, Object> registeredServices() {
        Map<ServiceReference<?>, Object> map = new HashMap<ServiceReference<?>, Object>();
        for (ServiceHolder<?> holder : services.values()) {
            map.put(holder.getReference(), holder.getService());
        }
        return Collections.unmodifiableMap(map);
    }

    public void receivedMessage(byte[] message) {
        Request rq = Request.fromBytes(message);
        Response rsp = invokeService(rq);
        try {
            if (rsp != null) {
                responseSender.send(rsp.toBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Response invokeService(Request rq) {
        ServiceHolder<?> service = services.get(rq.serviceId);
        try {
            Future<?> future = service.invoke(rq.methodName, rq.paramTypes, rq.parameters);
            if (future == null) {
                return null;
            }
            Object value = future.get(0, TimeUnit.MILLISECONDS);
            return Response.valueReturned(rq.requestId, value);

        } catch (InvocationTargetException e) {
            return Response.exceptionThrown(rq.requestId, e.getTargetException());

        } catch (Exception e) {
            logger.warn("Exception in handling request: " + rq, e);
            throw new RuntimeException(e);
        }
    }
}
