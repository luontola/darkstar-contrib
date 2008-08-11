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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Esko Luontola
 * @since 10.8.2008
 */
public class ProxyGeneratingFuture<T> implements Future<Set<T>>, Serializable {
    private static final long serialVersionUID = 1L;

    private final Future<Set<ServiceReference<T>>> refs;
    private final RpcProxyFactory proxyFactory;

    public ProxyGeneratingFuture(Future<Set<ServiceReference<T>>> refs, RpcProxyFactory proxyFactory) {
        this.refs = refs;
        this.proxyFactory = proxyFactory;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return refs.cancel(mayInterruptIfRunning);
    }

    public boolean isCancelled() {
        return refs.isCancelled();
    }

    public boolean isDone() {
        return refs.isDone();
    }

    public Set<T> get() throws InterruptedException, ExecutionException {
        return asProxies(refs.get());
    }

    public Set<T> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return asProxies(refs.get(timeout, unit));
    }

    private Set<T> asProxies(Set<ServiceReference<T>> refs) {
        Set<T> proxies = new HashSet<T>();
        for (ServiceReference<T> ref : refs) {
            proxies.add(proxyFactory.create(ref));
        }
        return Collections.unmodifiableSet(proxies);
    }
}
