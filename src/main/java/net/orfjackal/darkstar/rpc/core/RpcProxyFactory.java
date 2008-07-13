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

import net.orfjackal.darkstar.rpc.RpcClient;
import net.orfjackal.darkstar.rpc.ServiceReference;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Esko Luontola
 * @since 8.6.2008
 */
public class RpcProxyFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    private final RpcClient connection;

    public RpcProxyFactory(RpcClient connection) {
        this.connection = connection;
    }

    public <T> T create(ServiceReference<T> ref) {
        Class<T> type = ref.getServiceInterface();
        Object proxy = Proxy.newProxyInstance(type.getClassLoader(),
                new Class<?>[]{type}, new RpcInvocationHandler(connection, ref));
        return type.cast(proxy);
    }

    private static class RpcInvocationHandler implements InvocationHandler, Serializable {

        private static final long serialVersionUID = 1L;

        private final RpcClient connection;
        private final ServiceReference<?> reference;

        public RpcInvocationHandler(RpcClient connection, ServiceReference<?> reference) {
            this.connection = connection;
            this.reference = reference;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass().equals(Object.class)) {
                return method.invoke(this, args);
            }
            return connection.remoteInvoke(reference.getServiceId(), method.getName(), method.getParameterTypes(), args);
        }

        public boolean equals(Object obj) {
            if (obj instanceof Proxy) {
                InvocationHandler handler = Proxy.getInvocationHandler(obj);
                if (handler instanceof RpcInvocationHandler) {
                    RpcInvocationHandler other = (RpcInvocationHandler) handler;
                    return this.reference.equals(other.reference) && this.connection.equals(other.connection);
                }
            }
            return false;
        }

        public int hashCode() {
            return connection.hashCode() + reference.hashCode();
        }

        public String toString() {
            return "RpcProxy[" + connection + "," + reference + "]";
        }
    }
}
