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

package net.orfjackal.numberguess.client;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;
import net.orfjackal.darkstar.rpc.comm.ClientChannelAdapter;
import net.orfjackal.darkstar.rpc.comm.RpcGateway;
import net.orfjackal.numberguess.game.NumberGuessGameService;

import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.Set;

/**
 * @author Esko Luontola
 * @since 16.6.2008
 */
public class GameClient {

    private static final int TIMEOUT = 5000;

    private final Object loginResponseLock = new Object();
    private final Object gatewayLock = new Object();

    private final String username;
    private final SimpleClient client;

    private RpcGateway gateway;

    public GameClient(String username) {
        this.username = username;
        client = new SimpleClient(new MySimpleClientListener());
    }

    public void login(String host, String port) {
        Properties props = new Properties();
        props.put("host", host);
        props.put("port", port);
        try {
            synchronized (loginResponseLock) {
                client.login(props);
                loginResponseLock.wait(TIMEOUT);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void logout(boolean force) {
        client.logout(force);
    }

    private RpcGateway getGateway() {
        try {
            synchronized (gatewayLock) {
                if (gateway == null) {
                    gatewayLock.wait(TIMEOUT);
                }
                return gateway;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setGateway(RpcGateway gateway) {
        synchronized (gatewayLock) {
            this.gateway = gateway;
            gatewayLock.notifyAll();
        }
    }

    public NumberGuessGameService getGame() {
        Set<NumberGuessGameService> games = getGateway().remoteFindByType(NumberGuessGameService.class);
        assert games.size() == 1;
        return games.iterator().next();
    }

    private class MySimpleClientListener implements SimpleClientListener {

        public void receivedMessage(ByteBuffer message) {
        }

        public ClientChannelListener joinedChannel(ClientChannel channel) {
            if (channel.getName().startsWith("RpcChannel")) {
                ClientChannelAdapter adapter = new ClientChannelAdapter();
                setGateway(adapter.getGateway());
                return adapter.joinedChannel(channel);
            } else {
                throw new RuntimeException("Unexpected channel: " + channel);
            }
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, "password".toCharArray());
        }

        public void loggedIn() {
            System.out.println("Logged in.");
            synchronized (loginResponseLock) {
                loginResponseLock.notifyAll();
            }
        }

        public void loginFailed(String reason) {
            System.out.println("Login failed: " + reason);
            synchronized (loginResponseLock) {
                loginResponseLock.notifyAll();
            }
        }

        public void reconnecting() {
            System.out.println("Reconnecting...");
        }

        public void reconnected() {
            System.out.println("Reconnected.");
        }

        public void disconnected(boolean graceful, String reason) {
            System.out.println("Disconnected: " + reason);
            setGateway(null);
        }
    }
}
