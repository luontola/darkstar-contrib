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

import net.orfjackal.darkstar.rpc.ServiceReference;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

/**
 * @author Esko Luontola
 * @since 10.6.2008
 */
public final class ServiceHolder<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final T service;
    private final ServiceReference<T> reference;

    public ServiceHolder(T service, ServiceReference<T> reference) {
        this.service = service;
        this.reference = reference;
    }

    public T getService() {
        return service;
    }

    public ServiceReference<T> getReference() {
        return reference;
    }

    public Future<?> invoke(String methodName, Class<?>[] paramTypes, Object[] parameters) throws InvocationTargetException {
        Class<T> type = reference.getServiceInterface();
        try {
            Method method = type.getMethod(methodName, paramTypes);
            if (method.getReturnType().equals(Future.class) || method.getReturnType().equals(Void.TYPE)) {
                return (Future<?>) method.invoke(service, parameters);
            } else {
                throw new IllegalArgumentException("Illegal return type: " + method);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Method " + methodName + " not in " + type, e);
        } catch (IllegalAccessException e) {    // should never happen, since the method is defined by an interface
            throw new RuntimeException(e);
        }
    }
}
