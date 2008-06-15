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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * @author Esko Luontola
 * @since 15.6.2008
 */
public class ClientChannelAdapter implements ClientChannelListener {

    private static final Logger log = Logger.getLogger(ClientChannelAdapter.class.getName());

    // client-to-server requests
    private MessageReciever responseReciever;

    // server-to-client requests
    private MessageReciever requestReciever;

    private final RpcGateway gateway;
    private ClientChannel channel;

    public ClientChannelAdapter() {
        this(1000);
    }

    public ClientChannelAdapter(int timeout) {
        gateway = new RpcGateway(new MyRequestSender(), new MyResponseSender(), timeout);
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
            log.warning("Unexpected header " + header + " on channel " + channel);
        }
    }

    private void sendToChannel(ByteBuffer buf) throws IOException {
        if (channel == null) {
            throw new IllegalStateException("No connection");
        }
        channel.send(buf);
    }

    private class MyRequestSender implements MessageSender {

        public void send(byte[] message) throws IOException {
            ByteBuffer buf = ByteBuffer.allocateDirect(message.length + 1);
            buf.put(RpcGateway.REQUEST_TO_MASTER);
            buf.put(message);
            buf.flip();
            sendToChannel(buf);
        }

        public void setCallback(MessageReciever callback) {
            responseReciever = callback;
        }
    }

    private class MyResponseSender implements MessageSender {

        public void send(byte[] message) throws IOException {
            ByteBuffer buf = ByteBuffer.allocateDirect(message.length + 1);
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
