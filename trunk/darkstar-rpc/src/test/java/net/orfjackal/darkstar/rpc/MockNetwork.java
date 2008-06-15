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

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Esko Luontola
 * @since 14.6.2008
 */
public class MockNetwork {

    private final ExecutorService messageQueue;

    private final AbstractMessageSender serverToClient;
    private final AbstractMessageSender clientToServer;

    public MockNetwork() {
        messageQueue = Executors.newCachedThreadPool();

        serverToClient = new AbstractMessageSender() {
            public void send(final byte[] message) throws IOException {
                messageQueue.execute(new Runnable() {
                    public void run() {
                        clientToServer.getCallback().receivedMessage(message);
                    }
                });
            }
        };
        clientToServer = new AbstractMessageSender() {
            public void send(final byte[] message) throws IOException {
                messageQueue.execute(new Runnable() {
                    public void run() {
                        serverToClient.getCallback().receivedMessage(message);
                    }
                });
            }
        };
    }

    public MessageSender getServerToClient() {
        return serverToClient;
    }

    public MessageSender getClientToServer() {
        return clientToServer;
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
}
