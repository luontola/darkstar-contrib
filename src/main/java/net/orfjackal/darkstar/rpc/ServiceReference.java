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

package net.orfjackal.darkstar.rpc;

import java.io.Serializable;

/**
 * Identifies a service within a {@link RpcServer}. {@code ServiceReference}s should never be used
 * with another {@link RpcServer} than the one from which the reference originated.
 *
 * @author Esko Luontola
 * @since 11.6.2008
 */
public final class ServiceReference<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<T> serviceInterface;
    private final long serviceId;

    public ServiceReference(Class<T> serviceInterface, long serviceId) {
        this.serviceInterface = serviceInterface;
        this.serviceId = serviceId;
    }

    public Class<T> getServiceInterface() {
        return serviceInterface;
    }

    public long getServiceId() {
        return serviceId;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ServiceReference) {
            ServiceReference<?> other = (ServiceReference<?>) obj;
            return serviceId == other.serviceId;
        }
        return false;
    }

    public int hashCode() {
        return (int) serviceId;
    }

    public String toString() {
        return "ServiceReference[" + serviceInterface.getName() + ",serviceId=" + serviceId + "]";
    }
}
