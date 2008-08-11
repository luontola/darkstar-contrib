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

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.darkstar.integration.util.TimedInterrupt;
import net.orfjackal.darkstar.rpc.*;
import net.orfjackal.darkstar.rpc.core.futures.ClientFutureManager;
import org.junit.runner.RunWith;

import java.util.Set;

/**
 * @author Esko Luontola
 * @since 14.6.2008
 */
@RunWith(JDaveRunner.class)
public class ServiceLocatorSpec extends Specification<Object> {

    private static final int TIMEOUT = 1000;

    private MockNetwork network = new MockNetwork();
    private Thread testTimeout;

    private RpcServiceRegistry backend;
    private RpcServiceInvoker frontend;
    private RpcProxyFactory factory;

    public void create() {
        testTimeout = TimedInterrupt.startOnCurrentThread(TIMEOUT);
        backend = new RpcServiceRegistryImpl(network.getServerToClient());
        frontend = new RpcServiceInvokerImpl(network.getClientToServer(), new ClientFutureManager());
        factory = new RpcProxyFactory(frontend);
    }

    public void destroy() {
        testTimeout.interrupt();
        network.shutdown();
    }


    public class AServiceLocator {

        private ServiceReference<ServiceLocator> locatorRef;
        private ServiceLocator locator;
        private ServiceReference<Foo> foo1Ref;
        private ServiceReference<Foo> foo2Ref;
        private ServiceReference<Bar> bar3Ref;

        public Object create() {
            locatorRef = frontend.getServiceLocator();
            locator = factory.create(locatorRef);
            foo1Ref = backend.registerService(Foo.class, dummy(Foo.class, "foo1"));
            foo2Ref = backend.registerService(Foo.class, dummy(Foo.class, "foo2"));
            bar3Ref = backend.registerService(Bar.class, dummy(Bar.class, "bar3"));
            return null;
        }

        public void canBeRetrievedFromTheFrontend() {
            specify(locatorRef, isNotNull());
            specify(locator, isNotNull());
        }

        public void findsAllServices() throws Exception {
            Set<ServiceReference<?>> services = locator.findAll().get();
            specify(services, should.containExactly(locatorRef, foo1Ref, foo2Ref, bar3Ref));
        }

        public void findsServicesByType() throws Exception {
            Set<ServiceReference<Foo>> foos = locator.findByType(Foo.class).get();
            Set<ServiceReference<Bar>> bars = locator.findByType(Bar.class).get();
            specify(foos, should.containExactly(foo1Ref, foo2Ref));
            specify(bars, should.containExactly(bar3Ref));
        }
    }


    public interface Foo {
    }

    public interface Bar {
    }
}
