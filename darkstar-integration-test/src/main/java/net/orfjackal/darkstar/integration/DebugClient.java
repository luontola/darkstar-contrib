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

package net.orfjackal.darkstar.integration;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.ServerSessionListener;
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Esko Luontola
 * @since 13.7.2008
 */
public class DebugClient {

    public static final String LOGGED_IN = "loggedIn";
    public static final String LOGIN_FAILED = "loginFailed";
    public static final String RECONNECTING = "reconnecting";
    public static final String RECONNECTED = "reconnected";
    public static final String DISCONNECTED = "disconnected";
    public static final String JOINED_CHANNEL = "joinedChannel";

    public final BlockingQueue<String> events = new LinkedBlockingQueue<String>();
    public final BlockingQueue<ByteBuffer> messages = new LinkedBlockingQueue<ByteBuffer>();

    private final String host;
    private final int port;
    private final String username;
    private final char[] password;

    private final SimpleClient client;

    public DebugClient(String host, int port) {
        this(host, port, "guest", "guest".toCharArray());
    }

    public DebugClient(String host, int port, String username, char[] password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.client = new SimpleClient(new MySimpleClientListener());
    }

    public void login() {
        Properties props = new Properties();
        props.setProperty("host", host);
        props.setProperty("port", Integer.toString(port));
        try {
            client.login(props);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void logout(boolean force) {
        client.logout(force);
    }

    public void send(ByteBuffer message) throws IOException {
        client.send(message);
    }

    /**
     * Override to join channels.
     *
     * @see ServerSessionListener#joinedChannel(ClientChannel)
     */
    public ClientChannelListener joinedChannel(ClientChannel channel) {
        throw new UnsupportedOperationException("Override this method to join channels");
    }


    private class MySimpleClientListener implements SimpleClientListener {

        public void receivedMessage(ByteBuffer message) {
            messages.add(message);
        }

        public ClientChannelListener joinedChannel(ClientChannel channel) {
            events.add(JOINED_CHANNEL + ": " + channel);
            return DebugClient.this.joinedChannel(channel);
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }

        public void loggedIn() {
            events.add(LOGGED_IN);
        }

        public void loginFailed(String reason) {
            events.add(LOGIN_FAILED + ": " + reason);
        }

        public void reconnecting() {
            events.add(RECONNECTING);
        }

        public void reconnected() {
            events.add(RECONNECTED);
        }

        public void disconnected(boolean graceful, String reason) {
            events.add(DISCONNECTED + ": " + graceful + ", " + reason);
        }
    }
}
