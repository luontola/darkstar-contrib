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

package net.orfjackal.numberguess.server;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.darkstar.integration.DarkstarServer;
import net.orfjackal.darkstar.integration.util.StreamWaiter;
import net.orfjackal.darkstar.integration.util.TempDirectory;
import net.orfjackal.darkstar.integration.util.TimedInterrupt;
import net.orfjackal.numberguess.client.GameClient;
import net.orfjackal.numberguess.game.GuessResult;
import net.orfjackal.numberguess.game.NumberGuessGameService;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author Esko Luontola
 * @since 19.6.2008
 */
@RunWith(JDaveRunner.class)
public class SystemIntegrationSpec extends Specification<Object> {

    private static final int TIMEOUT = 5000;

    private DarkstarServer server;
    private TempDirectory tempDirectory;
    private StreamWaiter waiter;
    private Thread testTimeout;

    public void create() throws Exception {
        tempDirectory = new TempDirectory();
        tempDirectory.create();

        server = new DarkstarServer(tempDirectory.getDirectory());
        server.setAppName("NumberGuessGame");
        server.setAppListener(GameAppListener.class);
        server.start();

        // wait for the server to start
        waiter = new StreamWaiter(server.getSystemErr());
        waiter.waitForBytes("NumberGuessGame".getBytes(), TIMEOUT);

        // abort the tests if they take too long
        testTimeout = TimedInterrupt.startOnCurrentThread(TIMEOUT);
    }

    public void destroy() throws Exception {
        System.out.println(server.getSystemOut());
        System.err.println(server.getSystemErr());
        testTimeout.interrupt();
        server.shutdown();
        tempDirectory.dispose();
    }


    public class WhenClientConnectsToTheServer {

        private GameClient client;

        public Object create() throws TimeoutException {
            client = new GameClient("John Doe");
            specify(!client.isConnected());
            client.login("localhost", String.valueOf(server.getPort()));
            return null;
        }

        public void destroy() {
            client.logout(true);
        }

        public void clientIsConnected() {
            specify(client.isConnected());
        }

        public void gameCanBeUsed() throws ExecutionException, InterruptedException {
            NumberGuessGameService game = client.getGame();
            game.setMinimum(4);
            game.setMaximum(4);
            specify(game.guess(5).get(), should.equal(GuessResult.TOO_HIGH));
            specify(game.guess(3).get(), should.equal(GuessResult.TOO_LOW));
            specify(game.guess(4).get(), should.equal(GuessResult.SUCCESS));
            specify(game.tries().get(), should.equal(3));
        }
    }
}
