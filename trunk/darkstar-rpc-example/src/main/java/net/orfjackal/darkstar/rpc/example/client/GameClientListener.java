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

package net.orfjackal.darkstar.rpc.example.client;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;
import net.orfjackal.darkstar.rpc.comm.ClientChannelAdapter;
import net.orfjackal.darkstar.rpc.comm.RpcGateway;
import net.orfjackal.darkstar.rpc.example.services.NumberGuessGameService;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.Set;

/**
 * @author Esko Luontola
 * @since 16.6.2008
 */
public class GameClientListener implements SimpleClientListener {

    private final SimpleClient client;
    private String username;
    private NumberGuessGameService numberGuessGame;

    public GameClientListener(String username) {
        this.username = username;
        client = new SimpleClient(this);
    }

    public boolean login(String host, String port) {
        Properties props = new Properties();
        props.put("host", host);
        props.put("port", port);
        try {
            client.login(props);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void logout() {
        client.logout(false);
    }

    private void initServices(RpcGateway gateway) {
        Set<NumberGuessGameService> numberGuessGame = gateway.remoteFindByType(NumberGuessGameService.class);
        assert numberGuessGame.size() == 1;
        this.numberGuessGame = numberGuessGame.iterator().next();
    }

    private void resetServices() {
        this.numberGuessGame = null;
    }

    public NumberGuessGameService getNumberGuessGame() {
        if (numberGuessGame == null) {
            throw new IllegalStateException("Not connected to server");
        }
        return numberGuessGame;
    }

    public void receivedMessage(ByteBuffer message) {
    }

    public ClientChannelListener joinedChannel(ClientChannel channel) {
        ClientChannelAdapter adapter = new ClientChannelAdapter();
        ClientChannelListener listener = adapter.joinedChannel(channel);
        initServices(adapter.getGateway());
        return listener;
    }

    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, "password".toCharArray());
    }

    public void loggedIn() {
        System.out.println("Logged in.");
    }

    public void loginFailed(String reason) {
        System.out.println("Login failed: " + reason);
    }

    public void reconnecting() {
        System.out.println("Reconnecting...");
    }

    public void reconnected() {
        System.out.println("Reconnected.");
    }

    public void disconnected(boolean graceful, String reason) {
        System.out.println("Disconnected: " + reason);
        resetServices();
    }
}
