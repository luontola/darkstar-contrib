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

package net.orfjackal.darkstar.rpc;

import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.ServerSessionListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Esko Luontola
 * @since 15.6.2008
 */
public class MockChannel {

    private final ExecutorService messageQueue = Executors.newCachedThreadPool();

    // on server
    private final Channel channel = new MockChannelImpl();
    private final ChannelListener channelListener;

    // on client
    private final ClientSession clientSession = new MockClientSessionImpl();
    private final ClientChannel clientChannel = new MockClientChannelImpl();
    private final Set<ClientChannelListener> clientChannelListeners = new HashSet<ClientChannelListener>();

    public MockChannel(ChannelListener channelListener) {
        this.channelListener = channelListener;
    }

    public Channel getChannel() {
        return channel;
    }

    public void joinChannel(ServerSessionListener session) {
        clientChannelListeners.add(session.joinedChannel(clientChannel));
    }

    public void leaveAll() {
        for (Iterator<ClientChannelListener> it = clientChannelListeners.iterator(); it.hasNext();) {
            ClientChannelListener listener = it.next();
            it.remove();
            listener.leftChannel(clientChannel);
        }
    }

    public void shutdown() {
        messageQueue.shutdown();
    }

    public void shutdownAndWait() {
        shutdown();
        try {
            messageQueue.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private class MockChannelImpl implements Channel {

        public String getName() {
            return "MockChannel";
        }

        public Delivery getDeliveryRequirement() {
            return Delivery.RELIABLE;
        }

        public boolean hasSessions() {
            throw new UnsupportedOperationException();
        }

        public Iterator<ClientSession> getSessions() {
            throw new UnsupportedOperationException();
        }

        public Channel join(ClientSession session) {
            throw new UnsupportedOperationException();
        }

        public Channel join(Set<ClientSession> sessions) {
            throw new UnsupportedOperationException();
        }

        public Channel leave(ClientSession session) {
            throw new UnsupportedOperationException();
        }

        public Channel leave(Set<ClientSession> sessions) {
            throw new UnsupportedOperationException();
        }

        public Channel leaveAll() {
            throw new UnsupportedOperationException();
        }

        public Channel send(ClientSession sender, final ByteBuffer message) {
            for (final ClientChannelListener listener : clientChannelListeners) {
                messageQueue.execute(new Runnable() {
                    public void run() {
                        listener.receivedMessage(clientChannel, message);
                    }
                });
            }
            return this;
        }
    }

    private class MockClientChannelImpl implements ClientChannel {

        public String getName() {
            return "MockChannel";
        }

        public void send(final ByteBuffer message) throws IOException {
            if (clientChannelListeners.isEmpty()) {
                throw new IllegalStateException("You are not a member of this channel");
            }
            messageQueue.execute(new Runnable() {
                public void run() {
                    channelListener.receivedMessage(channel, clientSession, message);
                }
            });
        }
    }

    private class MockClientSessionImpl implements ClientSession {

        public String getName() {
            throw new UnsupportedOperationException();
        }

        public ClientSession send(ByteBuffer message) {
            throw new UnsupportedOperationException();
        }

        public boolean isConnected() {
            throw new UnsupportedOperationException();
        }
    }
}
