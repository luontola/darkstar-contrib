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

package net.orfjackal.darkstar.rpc.example.server;

import com.sun.sgs.app.*;
import net.orfjackal.darkstar.rpc.comm.ChannelAdapter;
import net.orfjackal.darkstar.rpc.comm.RpcGateway;
import net.orfjackal.darkstar.rpc.example.game.NumberGuessGameImpl;
import net.orfjackal.darkstar.rpc.example.services.NumberGuessGameService;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Esko Luontola
 * @since 16.6.2008
 */
public class GameClientSessionListener implements ClientSessionListener, ManagedObjectRemoval, ManagedObject, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(GameClientSessionListener.class.getName());

    private final ManagedReference<ClientSession> session;
    private final RpcGateway gateway;

    public GameClientSessionListener(ClientSession session) {
        this.session = AppContext.getDataManager().createReference(session);
        this.gateway = initGateway(session);
        gateway.registerService(NumberGuessGameService.class,
                new NumberGuessGameServiceImpl(new NumberGuessGameImpl()));
    }

    private RpcGateway initGateway(ClientSession session) {
        ChannelAdapter adapter = new ChannelAdapter();
        Channel channel = AppContext.getChannelManager()
                .createChannel("RpcChannel:" + session.getName(), adapter, Delivery.RELIABLE);
        adapter.setChannel(channel);
        return adapter.getGateway();
    }

    public void receivedMessage(ByteBuffer message) {
    }

    public void disconnected(boolean graceful) {
        logger.log(Level.INFO, "User {0} disconnected", session.get().getName());
    }

    public void removingObject() {
        logger.log(Level.INFO, "Removing " + getClass().getName());
        AppContext.getDataManager().removeObject(session.get());
    }
}
