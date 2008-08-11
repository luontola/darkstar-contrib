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

import jdave.Block;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.darkstar.rpc.DummySender;
import net.orfjackal.darkstar.rpc.RpcServiceRegistry;
import net.orfjackal.darkstar.rpc.ServiceHelper;
import net.orfjackal.darkstar.rpc.ServiceReference;
import net.orfjackal.darkstar.rpc.core.protocol.Request;
import net.orfjackal.darkstar.rpc.core.protocol.Response;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.util.concurrent.Future;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Esko Luontola
 * @since 10.6.2008
 */
@RunWith(JDaveRunner.class)
public class RpcServiceRegistrySpec extends Specification<Object> {

    private RpcServiceRegistry backend;
    private DummySender frontend;

    private Logger log;
    private Filter filter;

    public void create() {
        frontend = new DummySender();
        backend = new RpcServiceRegistryImpl(frontend);

        log = Logger.getLogger(RpcServiceRegistryImpl.class.getName());
        filter = mock(Filter.class);
        log.setFilter(filter);
    }

    public void destroy() {
        log.setFilter(null);
    }

    private Expectations aMessageIsLogged() {
        return new Expectations() {{
            one(filter).isLoggable(with(any(LogRecord.class))); will(returnValue(false));
        }};
    }


    public class WhenNoServicesAreRegistered {

        public Object create() {
            return null;
        }

        public void noServicesMayBeInvoked() {
            checking(aMessageIsLogged());
            specify(new Block() {
                public void run() throws Throwable {
                    frontend.callback.receivedMessage(new Request(1L, 1L, "foo", new Class<?>[0], null).toBytes());
                }
            }, should.raise(RuntimeException.class));
        }
    }

    public class WhenAServiceIsRegistered {

        private FooServiceImpl service;
        private ServiceReference<FooService> serviceRef;

        public Object create() {
            service = new FooServiceImpl();
            serviceRef = backend.registerService(FooService.class, service);
            return null;
        }

        public void serviceMethodsMayBeInvoked() {
            frontend.callback.receivedMessage(new Request(1L, serviceRef.getServiceId(),
                    "foo", new Class<?>[0], null).toBytes());
            specify(service.fooCalled, should.equal(1));
        }

        public void serviceMethodsMayBeInvokedWithParameters() {
            frontend.callback.receivedMessage(new Request(1L, serviceRef.getServiceId(),
                    "foo", new Class<?>[]{String.class}, new Object[]{"hello"}).toBytes());
            specify(service.fooParam, should.equal("hello"));
        }

        public void valuesReturnedByTheServiceMethodAreProvided() {
            frontend.callback.receivedMessage(new Request(1L, serviceRef.getServiceId(),
                    "foo", new Class<?>[]{String.class}, new Object[]{"hello"}).toBytes());
            Response response = Response.fromBytes(frontend.messages.get(0));
            specify(response.requestId, should.equal(1L));
            specify(response.value, should.equal("hello"));
        }

        public void exceptionsThrownByTheServiceMethodAreProvided() {
            frontend.callback.receivedMessage(new Request(1L, serviceRef.getServiceId(),
                    "exceptionThrower", new Class<?>[0], null).toBytes());
            Response response = Response.fromBytes(frontend.messages.get(0));
            specify(response.requestId, should.equal(1L));
            specify(response.exception.getClass(), should.equal(IllegalStateException.class));
            specify(response.exception.getMessage(), should.equal("exception message"));
        }

        public void voidServiceMethodsWillNotSendAResponse() {
            frontend.callback.receivedMessage(new Request(1L, serviceRef.getServiceId(),
                    "voidMethod", new Class<?>[0], null).toBytes());
            specify(service.voidMethodCalled, should.equal(1));
            specify(frontend.messages.size(), should.equal(0));
        }

        public void allNonVoidServiceMethodsMustReturnAFuture() {
            checking(aMessageIsLogged());
            specify(new Block() {
                public void run() throws Throwable {
                    frontend.callback.receivedMessage(new Request(1L, serviceRef.getServiceId(),
                            "invalidServiceMethod", new Class<?>[0], null).toBytes());
                }
            }, should.raise(RuntimeException.class));
            specify(service.invalidServiceMethodCalled, should.equal(0));
            specify(frontend.messages.size(), should.equal(0));
        }

        public void methodsNotPartOfTheServiceInterfaceMayNotBeCalled() {
            checking(aMessageIsLogged());
            specify(new Block() {
                public void run() throws Throwable {
                    frontend.callback.receivedMessage(new Request(1L, serviceRef.getServiceId(),
                            "notInInterface", new Class<?>[0], null).toBytes());
                }
            }, should.raise(RuntimeException.class));
            specify(service.notInInterfaceCalled, should.equal(0));
            specify(frontend.messages.size(), should.equal(0));
        }

        public void methodsInheritedFromJavaLangObjectMayNotBeCalled() {
            checking(aMessageIsLogged());
            specify(new Block() {
                public void run() throws Throwable {
                    frontend.callback.receivedMessage(new Request(1L, serviceRef.getServiceId(),
                            "toString", new Class<?>[0], null).toBytes());
                }
            }, should.raise(RuntimeException.class));
            specify(frontend.messages.size(), should.equal(0));
        }
    }

    public class WhenManyServicesAreRegistered {

        private FooServiceImpl service1;
        private FooServiceImpl service2;
        private ServiceReference<FooService> serviceRef1;
        private ServiceReference<FooService> serviceRef2;

        public Object create() {
            service1 = new FooServiceImpl();
            service2 = new FooServiceImpl();
            serviceRef1 = backend.registerService(FooService.class, service1);
            serviceRef2 = backend.registerService(FooService.class, service2);
            return null;
        }

        public void allTheServicesAreRegistered() {
            specify(backend.registeredServices().values(), should.containAll(service1, service2));
        }

        public void eachServiceWillHaveItsOwnServiceId() {
            specify(serviceRef1.getServiceId(), should.not().equal(serviceRef2.getServiceId()));
        }

        public void eachServiceIdWillCallOnlyThatServiceWhoseIdItIs() {
            frontend.callback.receivedMessage(
                    new Request(1L, serviceRef1.getServiceId(), "foo", new Class<?>[0], null).toBytes());
            specify(service1.fooCalled, should.equal(1));
            specify(service2.fooCalled, should.equal(0));
            frontend.callback.receivedMessage(
                    new Request(1L, serviceRef2.getServiceId(), "foo", new Class<?>[0], null).toBytes());
            specify(service1.fooCalled, should.equal(1));
            specify(service2.fooCalled, should.equal(1));
        }

        public void unregisteringAServiceWillRemoveThatService() {
            backend.unregisterService(serviceRef1);
            checking(aMessageIsLogged());
            specify(new Block() {
                public void run() throws Throwable {
                    frontend.callback.receivedMessage(
                            new Request(1L, serviceRef1.getServiceId(), "foo", new Class<?>[0], null).toBytes());
                }
            }, should.raise(RuntimeException.class));
            specify(service1.fooCalled, should.equal(0));
        }

        public void unregisteringAServiceShouldNotAffectOtherServices() {
            backend.unregisterService(serviceRef1);
            frontend.callback.receivedMessage(
                    new Request(1L, serviceRef2.getServiceId(), "foo", new Class<?>[0], null).toBytes());
            specify(service2.fooCalled, should.equal(1));
        }
    }


    private interface FooService {

        Future<String> foo();

        Future<String> foo(String hello);

        Future<String> exceptionThrower();

        void voidMethod();

        Object invalidServiceMethod();
    }

    private static class FooServiceImpl implements FooService {

        int fooCalled = 0;
        String fooParam;
        int voidMethodCalled = 0;
        int invalidServiceMethodCalled = 0;
        int notInInterfaceCalled = 0;

        public Future<String> foo() {
            fooCalled++;
            return ServiceHelper.wrap(null);
        }

        public Future<String> foo(String s) {
            fooParam = s;
            return ServiceHelper.wrap(s);
        }

        public Future<String> exceptionThrower() {
            throw new IllegalStateException("exception message");
        }

        public void voidMethod() {
            voidMethodCalled++;
        }

        public Object invalidServiceMethod() {
            invalidServiceMethodCalled++;
            return ServiceHelper.wrap(null);
        }

        public void notInInterface() {
            notInInterfaceCalled++;
        }
    }
}
