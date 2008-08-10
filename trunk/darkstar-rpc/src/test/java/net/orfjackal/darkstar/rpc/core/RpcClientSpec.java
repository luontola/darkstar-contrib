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
import net.orfjackal.darkstar.integration.util.TimedInterrupt;
import net.orfjackal.darkstar.rpc.DummySender;
import net.orfjackal.darkstar.rpc.MessageReciever;
import net.orfjackal.darkstar.rpc.RpcClient;
import net.orfjackal.darkstar.rpc.core.futures.ClientFutureManager;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * @author Esko Luontola
 * @since 9.6.2008
 */
@RunWith(JDaveRunner.class)
public class RpcClientSpec extends Specification<Object> {

    private static final int TIMEOUT = 1000;

    private DummySender server;
    private RpcClient client;
    private Thread testTimeout;

    public void create() {
        server = new DummySender();
        client = new RpcClientImpl(server, new ClientFutureManager());
        testTimeout = TimedInterrupt.startOnCurrentThread(TIMEOUT);
    }

    public void destroy() throws Exception {
        testTimeout.interrupt();
    }


    public class WhenAnInvocationIsRecieved {

        private Future<String> future;

        public Object create() {
            future = client.remoteInvoke(42L, "foo", new Class<?>[]{String.class, String.class}, new Object[]{"param1", "param2"});
            return null;
        }

        public void aRequestWillBeSentToTheServer() throws Exception {
            Request rq = Request.fromBytes(server.messages.get(0));
            specify(rq.requestId, should.equal(1L));
            specify(rq.serviceId, should.equal(42L));
            specify(rq.methodName, should.equal("foo"));
            specify(rq.paramTypes, should.containInOrder(String.class, String.class));
            specify(rq.parameters, should.containInOrder("param1", "param2"));
        }

        public void aFutureWillProvideTheReturnValueAsynchronously() throws Exception {
            specify(client.waitingForResponse(), should.equal(1));
            specify(!future.isDone());

            Response rsp = Response.valueReturned(1L, "returnvalue");
            server.callback.receivedMessage(rsp.toBytes());

            specify(client.waitingForResponse(), should.equal(0));
            specify(future.isDone());
            specify(future.get(), should.equal("returnvalue"));
        }
    }

    public class WhenMoreThanOneInvocationIsRecieved {

        private Future<String> future1;
        private Future<String> future2;

        public Object create() {
            future1 = client.remoteInvoke(42L, "foo", new Class<?>[0], null);
            future2 = client.remoteInvoke(42L, "foo", new Class<?>[0], null);
            return null;
        }

        public void eachRequestWillHaveItsOwnRequestId() {
            Request rq1 = Request.fromBytes(server.messages.get(0));
            Request rq2 = Request.fromBytes(server.messages.get(1));
            specify(rq1.requestId, should.equal(1L));
            specify(rq2.requestId, should.equal(2L));
        }

        public void eachFutureWillProvideItsOwnReturnValue() throws Exception {
            specify(client.waitingForResponse(), should.equal(2));
            server.callback.receivedMessage(Response.valueReturned(1L, "r1").toBytes());
            specify(client.waitingForResponse(), should.equal(1));
            server.callback.receivedMessage(Response.valueReturned(2L, "r2").toBytes());
            specify(client.waitingForResponse(), should.equal(0));
            specify(future1.get(), should.equal("r1"));
            specify(future2.get(), should.equal("r2"));
        }
    }

    public class WhenAVoidMethodIsInvoked {

        public Object create() {
            client.remoteInvokeNoResponse(123L, "voidMethod", new Class<?>[]{String.class}, new Object[]{"hello"});
            return null;
        }

        public void aRequestWillBeSentToTheServer() {
            Request rq = Request.fromBytes(server.messages.get(0));
            specify(rq.requestId, should.equal(1L));
            specify(rq.serviceId, should.equal(123L));
            specify(rq.methodName, should.equal("voidMethod"));
            specify(rq.paramTypes, should.containInOrder(String.class));
            specify(rq.parameters, should.containInOrder("hello"));
        }

        public void theClientWillNotWaitForAResponse() {
            specify(client.waitingForResponse(), should.equal(0));
        }
    }

    public class IfACommunicationErrorHappens {

        public Object create() throws IOException {
            server = mock(DummySender.class);
            checking(new Expectations() {{
                one(server).send(with(any(byte[].class))); will(throwException(new IOException()));
                allowing(server).setCallback(with(any(MessageReciever.class)));
            }});
            client = new RpcClientImpl(server, new ClientFutureManager());
            specify(new Block() {
                public void run() throws Throwable {
                    client.remoteInvoke(42L, "foo", new Class<?>[0], null);
                }
            }, should.raise(RuntimeException.class));
            return null;
        }

        public void theClientWillNotWaitForAResponse() {
            specify(client.waitingForResponse(), should.equal(0));
        }
    }
}
