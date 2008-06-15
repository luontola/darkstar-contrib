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
import net.orfjackal.darkstar.rpc.RpcClient;
import net.orfjackal.darkstar.rpc.ServiceReference;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Esko Luontola
 * @since 8.6.2008
 */
@RunWith(JDaveRunner.class)
public class RpcProxySpec extends Specification<Object> {

    public class TheProxyWillForwardToTheServer {

        private RpcClient connection;
        private Foo proxy;

        public Object create() {
            connection = mock(RpcClient.class);
            RpcProxyFactory factory = new RpcProxyFactory(connection);
            proxy = factory.create(new ServiceReference<Foo>(Foo.class, 1L));
            return null;
        }

        public void callsToMethodsWithNoParameters() {
            checking(new Expectations() {{
                one(connection).remoteInvoke(1L, "foo", new Class<?>[0], null);
            }});
            proxy.foo();
        }

        public void callsToMethodsWithSomeParameters() {
            checking(new Expectations() {{
                one(connection).remoteInvoke(1L, "foo", new Class<?>[]{String.class}, new Object[]{"param1"});
            }});
            proxy.foo("param1");
        }
    }

    public class TheProxyWillReturnToTheUser {

        private RpcClient connection;
        private Bar proxy;
        private Future<String> dummyFuture;

        public Object create() {
            connection = mock(RpcClient.class);
            RpcProxyFactory factory = new RpcProxyFactory(connection);
            proxy = factory.create(new ServiceReference<Bar>(Bar.class, 2L));
            dummyFuture = new NullFuture<String>() {
                public String get() {
                    return "returnvalue";
                }
            };
            return null;
        }

        public void aFutureContainingTheReturnValue() throws ExecutionException, InterruptedException {
            checking(new Expectations() {{
                one(connection).remoteInvoke(2L, "bar", new Class<?>[0], null); will(returnValue(dummyFuture));
            }});
            Future<String> future = proxy.bar();
            specify(future.get(), should.equal("returnvalue"));
        }
    }

    public class MethodsInheritedFromJavaLangObject {

        private RpcClient connection1;
        private Foo proxy;
        private Foo sameService;
        private Foo differentServiceId;
        private Foo differentConnection;

        public Object create() {
            connection1 = mock(RpcClient.class, "RpcClient1");
            RpcProxyFactory factory1 = new RpcProxyFactory(connection1);
            RpcClient connection2 = mock(RpcClient.class, "RpcClient2");
            RpcProxyFactory factory2 = new RpcProxyFactory(connection2);

            proxy = factory1.create(new ServiceReference<Foo>(Foo.class, 1L));
            sameService = factory1.create(new ServiceReference<Foo>(Foo.class, 1L));
            differentServiceId = factory1.create(new ServiceReference<Foo>(Foo.class, 2L));
            differentConnection = factory2.create(new ServiceReference<Foo>(Foo.class, 1L));
            return null;
        }

        public void callsToTheseMethodsAreNotForwardedToTheServer() {
            checking(new Expectations() {{
                // no calls to server
            }});
            proxy.equals(new Object());
            proxy.getClass();
            proxy.hashCode();
            proxy.toString();
        }

        public void equalsIsDefinedAsPointingToTheSameService() {
            specify(proxy.equals(proxy));
            specify(proxy.equals(sameService));
            specify(!proxy.equals(differentServiceId));
            specify(!proxy.equals(differentConnection));
        }

        public void hashcodeIsDefinedAsPointingToTheSameService() {
            specify(proxy.hashCode(), should.equal(sameService.hashCode()));
            specify(proxy.hashCode(), should.not().equal(differentServiceId.hashCode()));
            specify(proxy.hashCode(), should.not().equal(differentConnection.hashCode()));
        }

        public void tostringDescribesTheService() {
            specify(proxy.toString().startsWith("RpcProxy"));
            specify(proxy.toString().contains(Foo.class.getName()));
            specify(proxy.toString().contains(connection1.toString()));
            specify(proxy.toString().contains("serviceId=1"));
        }
    }


    private interface Foo {

        void foo();

        void foo(String s);
    }

    private interface Bar {

        Future<String> bar();
    }

    private static class NullFuture<V> implements Future<V> {

        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        public boolean isCancelled() {
            return false;
        }

        public boolean isDone() {
            return false;
        }

        public V get() throws InterruptedException, ExecutionException {
            return null;
        }

        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
    }
}
