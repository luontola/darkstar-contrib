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
import net.orfjackal.darkstar.rpc.MessageReciever;
import net.orfjackal.darkstar.rpc.MessageSender;
import net.orfjackal.darkstar.rpc.core.futures.ClientFutureManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * @author Esko Luontola
 * @since 15.6.2008
 */
public class ClientChannelAdapter implements ClientChannelListener {
    private static final Logger logger = LoggerFactory.getLogger(ClientChannelAdapter.class);

    // client-to-server requests
    private volatile MessageReciever responseReciever;

    // server-to-client requests
    private volatile MessageReciever requestReciever;

    private final RpcGateway gateway;
    private volatile ClientChannel channel;

    public ClientChannelAdapter() {
        gateway = new RpcGateway(new MyRequestSender(), new MyResponseSender(), new ClientFutureManager());
    }

    public RpcGateway getGateway() {
        return gateway;
    }

    public ClientChannelListener joinedChannel(ClientChannel channel) {
        assert this.channel == null;
        this.channel = channel;
        return this;
    }

    public void leftChannel(ClientChannel channel) {
        assert this.channel == channel;
        this.channel = null;
    }

    public void receivedMessage(ClientChannel channel, ByteBuffer message) {
        byte header = message.get();
        if (header == RpcGateway.REQUEST_TO_SLAVE) {
            requestReciever.receivedMessage(ByteBufferUtils.asByteArray(message));
        } else if (header == RpcGateway.RESPONSE_FROM_MASTER) {
            responseReciever.receivedMessage(ByteBufferUtils.asByteArray(message));
        } else {
            logger.warn("Unexpected header {} on channel {}", header, channel);
        }
    }

    private void sendToChannel(ByteBuffer buf) throws IOException {
        if (channel == null) {
            throw new IllegalStateException("No connection");
        }
        channel.send(buf);
    }

    private class MyRequestSender implements MessageSender, Serializable {
        private static final long serialVersionUID = 1L;

        public void send(byte[] message) throws IOException {
            ByteBuffer buf = ByteBuffer.allocate(message.length + 1);
            buf.put(RpcGateway.REQUEST_TO_MASTER);
            buf.put(message);
            buf.flip();
            sendToChannel(buf);
        }

        public void setCallback(MessageReciever callback) {
            responseReciever = callback;
        }
    }

    private class MyResponseSender implements MessageSender, Serializable {
        private static final long serialVersionUID = 1L;

        public void send(byte[] message) throws IOException {
            ByteBuffer buf = ByteBuffer.allocate(message.length + 1);
            buf.put(RpcGateway.RESPONSE_FROM_SLAVE);
            buf.put(message);
            buf.flip();
            sendToChannel(buf);
        }

        public void setCallback(MessageReciever callback) {
            requestReciever = callback;
        }
    }
}
