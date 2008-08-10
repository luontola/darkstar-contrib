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

package net.orfjackal.darkstar.rpc.core.futures;

import jdave.Block;
import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.darkstar.rpc.core.Request;
import net.orfjackal.darkstar.rpc.core.Response;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Esko Luontola
 * @since 10.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ClientFutureManagerSpec extends Specification<Object> {

    private static final int SERVICE_ID = 42;

    private FutureManager manager;
    private Request request;
    private Request request2;
    private Response responseVal;
    private Response responseExp;
    private Response unexpectedResponse;

    private Logger log;
    private Filter logFilter;

    public void create() throws Exception {
        log = Logger.getLogger(ClientFutureManager.class.getName());
        logFilter = mock(Filter.class);
        log.setFilter(logFilter);

        manager = new ClientFutureManager();
        request = new Request(1, SERVICE_ID, "foo", new Class<?>[0], new Object[0]);
        request2 = new Request(2, SERVICE_ID, "foo", new Class<?>[0], new Object[0]);
        responseVal = Response.valueReturned(1, "foo!");
        responseExp = Response.exceptionThrown(1, new IllegalArgumentException());
        unexpectedResponse = Response.valueReturned(3, "bar!");
    }

    public void destroy() throws Exception {
        log.setFilter(null);
    }

    public class WhenNoRequestsHaveBeenMade {

        public Object create() {
            return null;
        }

        public void managerIsNotWaitingForResponses() {
            specify(manager.waitingForResponse(), should.equal(0));
        }
    }

    public class WhenARequestIsMade {

        private Future<String> future;

        public Object create() {
            future = manager.waitForResponseTo(request);
            return null;
        }

        public void managerWaitsForAResponse() {
            specify(manager.waitingForResponse(), should.equal(1));
        }

        public void futureIsProvided() {
            specify(future, should.not().equal(null));
        }

        public void futureDoesNotYetHaveAReturnValue() {
            specify(!future.isDone());
            specify(new Block() {
                public void run() throws Throwable {
                    future.get(0, TimeUnit.MILLISECONDS);
                }
            }, should.raise(TimeoutException.class));
        }

        public void futureMayBeCancelled() {
            specify(!future.isCancelled());
            specify(future.cancel(true));
            specify(future.isCancelled());
            specify(manager.waitingForResponse(), should.equal(0));
        }
    }

    public class WhenTargetReturnsAValue {

        private Future<String> future;

        public Object create() {
            future = manager.waitForResponseTo(request);
            manager.recievedResponse(responseVal);
            return null;
        }

        public void managerStopsWaitingForTheResponse() {
            specify(manager.waitingForResponse(), should.equal(0));
        }

        public void futureProvidesTheReturnValue() throws Exception {
            specify(future.isDone());
            specify(future.get(), should.equal("foo!"));
        }

        public void futureCanNotBeCancelled() {
            specify(!future.isCancelled());
            specify(!future.cancel(true));
            specify(!future.isCancelled());
        }
    }

    public class WhenTargetThrowsAnException {

        private Future<String> future;

        public Object create() {
            future = manager.waitForResponseTo(request);
            manager.recievedResponse(responseExp);
            return null;
        }

        public void managerStopsWaitingForTheResponse() {
            specify(manager.waitingForResponse(), should.equal(0));
        }

        public void futureProvidesTheException() throws Exception {
            specify(future.isDone());
            specify(new Block() {
                public void run() throws Throwable {
                    future.get();
                }
            }, should.raise(ExecutionException.class, "java.lang.IllegalArgumentException"));
        }

        public void futureCanNotBeCancelled() {
            specify(!future.isCancelled());
            specify(!future.cancel(true));
            specify(!future.isCancelled());
        }
    }

    public class WhenThereAreManyConcurrentRequests {

        private Future<String> future1;
        private Future<String> future2;

        public Object create() {
            future1 = manager.waitForResponseTo(request);
            future2 = manager.waitForResponseTo(request2);
            return null;
        }

        public void managerWaitsForResponsesToAllOfThem() {
            specify(manager.waitingForResponse(), should.equal(2));
        }

        public void responsesDoNotAffectUnrelatedFutures() {
            manager.recievedResponse(responseVal);
            specify(future1.isDone());
            specify(!future2.isDone());
            specify(manager.waitingForResponse(), should.equal(1));
        }

        public void unexpectedResponsesAreSilentlyIgnoredAndLogged() {
            checking(new Expectations() {{
                one(logFilter).isLoggable(with(any(LogRecord.class))); will(returnValue(false));
            }});
            manager.recievedResponse(unexpectedResponse);
            specify(!future1.isDone());
            specify(!future2.isDone());
            specify(manager.waitingForResponse(), should.equal(2));
        }
    }
}
