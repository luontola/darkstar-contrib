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

package net.orfjackal.darkstar.rpc.comm;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.darkstar.rpc.MockNetwork;
import net.orfjackal.darkstar.rpc.ServiceHelper;
import net.orfjackal.darkstar.rpc.ServiceReference;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Esko Luontola
 * @since 15.6.2008
 */
@RunWith(JDaveRunner.class)
public class RpcGatewaySpec extends Specification<Object> {

    private MockNetwork toMaster = new MockNetwork();
    private MockNetwork toSlave = new MockNetwork();

    private void shutdownNetwork() {
        toMaster.shutdownAndWait();
        toSlave.shutdownAndWait();
    }


    public class ARpcGateway {

        private RpcGateway slaveGateway;
        private RpcGateway masterGateway;
        private Foo fooOnSlave;
        private Foo fooOnMaster;
        private Bar barOnMaster;
        private ServiceReference<Foo> fooOnSlaveRef;

        public Object create() {
            slaveGateway = new RpcGateway(toMaster.getClientToServer(), toSlave.getServerToClient(), 100);
            masterGateway = new RpcGateway(toSlave.getClientToServer(), toMaster.getServerToClient(), 100);
            fooOnSlave = mock(Foo.class, "fooOnSlave");
            fooOnMaster = mock(Foo.class, "fooOnMaster");
            barOnMaster = mock(Bar.class, "barOnMaster");
            fooOnSlaveRef = slaveGateway.registerService(Foo.class, fooOnSlave);
            masterGateway.registerService(Foo.class, fooOnMaster);
            masterGateway.registerService(Bar.class, barOnMaster);
            return null;
        }

        public void destroy() {
            shutdownNetwork();
        }

        public void servicesCanBeAddedLocally() {
            int before = slaveGateway.registeredServices().size();
            slaveGateway.registerService(Bar.class, dummy(Bar.class));
            specify(slaveGateway.registeredServices().size(), should.equal(before + 1));
        }

        public void servicesCanBeRemovedLocally() {
            int before = slaveGateway.registeredServices().size();
            slaveGateway.unregisterService(fooOnSlaveRef);
            specify(slaveGateway.registeredServices().size(), should.equal(before - 1));
        }

        public void slaveCanFindAllServicesOnMaster() {
            Set<?> onMaster = slaveGateway.remoteFindAll();
            specify(onMaster.size(), should.equal(3));
        }

        public void masterCanFindAllServicesOnSlave() {
            Set<?> onSlave = masterGateway.remoteFindAll();
            specify(onSlave.size(), should.equal(2));
        }

        public void slaveCanFindServicesOnMasterByType() {
            Set<Foo> foosOnMaster = slaveGateway.remoteFindByType(Foo.class);
            specify(foosOnMaster.size(), should.equal(1));
            Set<Bar> barsOnMaster = slaveGateway.remoteFindByType(Bar.class);
            specify(barsOnMaster.size(), should.equal(1));
        }

        public void masterCanFindServicesOnSlaveByType() {
            Set<Foo> foosOnSlave = masterGateway.remoteFindByType(Foo.class);
            specify(foosOnSlave.size(), should.equal(1));
            Set<Bar> barsOnSlave = masterGateway.remoteFindByType(Bar.class);
            specify(barsOnSlave.size(), should.equal(0));
        }

        public void slaveCanCallServiceMethodsOnMaster() {
            checking(new Expectations() {{
                one(fooOnMaster).serviceMethod();
            }});
            Set<Foo> foos = slaveGateway.remoteFindByType(Foo.class);
            Foo foo = foos.iterator().next();
            foo.serviceMethod();
            shutdownNetwork();
        }

        public void masterCanCallServiceMethodsOnSlave() {
            checking(new Expectations() {{
                one(fooOnSlave).serviceMethod();
            }});
            Set<Foo> foos = masterGateway.remoteFindByType(Foo.class);
            Foo foo = foos.iterator().next();
            foo.serviceMethod();
            shutdownNetwork();
        }

        public void slaveGetsResponsesFromMaster() throws ExecutionException, TimeoutException, InterruptedException {
            checking(new Expectations() {{
                one(fooOnMaster).hello("ping?"); will(returnValue(ServiceHelper.wrap("pong!")));
            }});
            Set<Foo> foos = slaveGateway.remoteFindByType(Foo.class);
            Foo foo = foos.iterator().next();
            Future<String> future = foo.hello("ping?");
            specify(future.get(100, TimeUnit.MILLISECONDS), should.equal("pong!"));
        }

        public void masterGetsResponsesFromSlave() throws ExecutionException, TimeoutException, InterruptedException {
            checking(new Expectations() {{
                one(fooOnSlave).hello("ping?"); will(returnValue(ServiceHelper.wrap("pong!")));
            }});
            Set<Foo> foos = masterGateway.remoteFindByType(Foo.class);
            Foo foo = foos.iterator().next();
            Future<String> future = foo.hello("ping?");
            specify(future.get(100, TimeUnit.MILLISECONDS), should.equal("pong!"));
        }
    }


    public interface Foo {

        void serviceMethod();

        Future<String> hello(String s);
    }

    public interface Bar {
    }
}
