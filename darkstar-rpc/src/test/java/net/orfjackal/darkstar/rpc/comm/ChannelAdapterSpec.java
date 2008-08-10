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

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.ServerSessionListener;
import jdave.Block;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.darkstar.exp.mocks.MockAppContext;
import net.orfjackal.darkstar.integration.util.TimedInterrupt;
import net.orfjackal.darkstar.rpc.MockChannel;
import net.orfjackal.darkstar.rpc.ServiceLocator;
import net.orfjackal.darkstar.rpc.ServiceReference;
import org.junit.runner.RunWith;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Esko Luontola
 * @since 10.6.2008
 */
@RunWith(JDaveRunner.class)
public class ChannelAdapterSpec extends Specification<Object> {

    private static final int TIMEOUT = 1000;

    private Thread testTimeout;

    public void create() {
        testTimeout = TimedInterrupt.startOnCurrentThread(TIMEOUT);
        MockAppContext.install();
    }

    public void destroy() {
        testTimeout.interrupt();
        MockAppContext.uninstall();
    }


    public class WhenThereIsAChannelForRpcBetweenOneClient {

        private MockChannel mockChannel;
        private RpcGateway gatewayOnServer;
        private RpcGateway gatewayOnClient;

        public Object create() {

            // initialization on server
            ServerChannelAdapter adapterOnServer = new ServerChannelAdapter();
            gatewayOnServer = adapterOnServer.getGateway();
            mockChannel = new MockChannel(adapterOnServer);
            adapterOnServer.setChannel(mockChannel.getChannel());

            // initialization on client
            final ClientChannelAdapter adapterOnClient = new ClientChannelAdapter();
            gatewayOnClient = adapterOnClient.getGateway();
            ServerSessionListener client = new NullServerSessionListener() {
                public ClientChannelListener joinedChannel(ClientChannel channel) {
                    return adapterOnClient.joinedChannel(channel);
                }
            };

            // server makes the client join the channel
            mockChannel.joinChannel(client);

            return null;
        }

        public void destroy() {
            mockChannel.shutdown();
        }

        public void clientCanUseServicesOnServer() throws Exception {
            Set<?> services = gatewayOnClient.remoteFindAll().get();
            specify(services.size(), should.equal(1));
        }

        public void serverCanUseServicesOnClient() throws Exception {
            Set<?> services = gatewayOnServer.remoteFindAll().get();
            specify(services.size(), should.equal(1));
        }

        public void ifTheClientLeavesTheChannelAllCommunicationsWillBeCut() throws Exception {
            final ServiceLocator locatorOnClient = gatewayOnClient.remoteFindByType(ServiceLocator.class).get().iterator().next();
            final ServiceLocator locatorOnServer = gatewayOnServer.remoteFindByType(ServiceLocator.class).get().iterator().next();
            mockChannel.leaveAll();
            specify(new Block() {
                public void run() throws Throwable {
                    locatorOnClient.findAll();
                }
            }, should.raise(IllegalStateException.class));
            specify(new Block() {
                public void run() throws Throwable {
                    Future<Set<ServiceReference<?>>> f = locatorOnServer.findAll();
                    f.get(50, TimeUnit.MILLISECONDS);
                }
            }, should.raise(TimeoutException.class));
        }
    }


    private static class NullServerSessionListener implements ServerSessionListener {

        public ClientChannelListener joinedChannel(ClientChannel channel) {
            return null;
        }

        public void receivedMessage(ByteBuffer message) {
        }

        public void reconnecting() {
        }

        public void reconnected() {
        }

        public void disconnected(boolean graceful, String reason) {
        }
    }
}
