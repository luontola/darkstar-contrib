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
import net.orfjackal.darkstar.rpc.*;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Esko Luontola
 * @since 10.6.2008
 */
@RunWith(JDaveRunner.class)
public class EndToEndSpec extends Specification<Object> {

    private MockNetwork network = new MockNetwork();

    private RpcServer server;
    private RpcClient client;

    private Foo fooService;
    private ServiceReference<Foo> fooServiceRef;
    private Foo fooProxy;

    public EndToEndSpec() {
        server = new RpcServerImpl(network.getServerToClient());
        client = new RpcClientImpl(network.getClientToServer());

        // initialize Foo on server
        fooService = mock(Foo.class);
        fooServiceRef = server.registerService(Foo.class, fooService);

        // initialize Foo on client
        RpcProxyFactory factory = new RpcProxyFactory(client);
        fooProxy = factory.create(fooServiceRef);
    }

    private void destroyParent() {
        network.shutdown();
    }


    public class WhenAMethodIsCalledOnTheProxy {

        public Object create() {
            return null;
        }

        public void destroy() {
            destroyParent();
        }

        public void theServiceMethodOnTheServerSideIsAlsoCalled() throws InterruptedException {
            checking(new Expectations() {{
                one(fooService).voidMethod();
            }});
            specify(client.waitingForResponse(), should.equal(0));
            fooProxy.voidMethod();
            specify(client.waitingForResponse(), should.equal(1));
            network.shutdownAndWait();
            specify(client.waitingForResponse(), should.equal(1));
        }

        public void aFutureWillProvideTheReturnValue() throws ExecutionException, InterruptedException, TimeoutException {
            checking(new Expectations() {{
                one(fooService).ping("ping?"); will(returnValue(ServiceHelper.wrap("pong!")));
            }});
            Future<String> f = fooProxy.ping("ping?");
            String value = f.get(100, TimeUnit.MILLISECONDS);
            specify(value, should.equal("pong!"));
        }

        public void aFutureWillProvideTheException() throws TimeoutException, InterruptedException {
            checking(new Expectations() {{
                one(fooService).ping("ping?"); will(throwException(new IllegalStateException("exception message")));
            }});
            Future<String> f = fooProxy.ping("ping?");
            try {
                f.get(100, TimeUnit.MILLISECONDS);
                specify(false);
            } catch (ExecutionException e) {
                specify(e.getCause().getClass(), should.equal(IllegalStateException.class));
                specify(e.getCause().getMessage(), should.equal("exception message"));
            }
        }
    }


    public interface Foo {

        void voidMethod();

        Future<String> ping(String s);
    }
}

// TODO: method callbacks (from the server side, call the method of an anonymous inner class given as a parameter on the client side)

/*
TODO: synchronous and asynchronous versions of the same interface (or rely on Future.get() only?)

See also http://en.wikipedia.org/wiki/RPyC
Allows for synchronous and asynchronous operation:
  * Synchronous operations return a NetProxy (see below)
  * Asynchronous operations return an AsyncResult, which is like promise objects
  * AsyncResults can be used as events

Idea:
If service method is void, it is asynchronous and does *not* give a response.
If service method returns a Future, it is asynchronous and gives a response.
If service method returns anything else, it is *synchronous* and gives a response.

*/
