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

import com.sun.sgs.app.*;
import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import com.sun.sgs.client.simple.SimpleClient;
import com.sun.sgs.client.simple.SimpleClientListener;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.darkstar.integration.DarkstarServer;
import net.orfjackal.darkstar.integration.util.StreamWaiter;
import net.orfjackal.darkstar.integration.util.TempDirectory;
import net.orfjackal.darkstar.rpc.ServiceHelper;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.Serializable;
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 * @author Esko Luontola
 * @since 18.6.2008
 */
@RunWith(JDaveRunner.class)
public class DarkstarIntegrationSpec extends Specification<Object> {

    private static final String STARTUP_MSG = "RpcTest has started";
    private static final int TIMEOUT = 5000;

    public class WhenUsingARealDarkstarServer {

        private TempDirectory tempDirectory;
        private DarkstarServer server;
        private StreamWaiter waiter;
        private RpcTestClient client;
        private Thread testTimeouter;

        public Object create() throws TimeoutException {
            tempDirectory = new TempDirectory();
            tempDirectory.create();

            server = new DarkstarServer(tempDirectory.getDirectory());
            server.setAppName("RpcTest");
            server.setAppListener(RpcTestAppListener.class);
            server.start();
            client = new RpcTestClient("localhost", server.getPort());

            // wait for the server to start up before running the tests
            waiter = new StreamWaiter(server.getSystemOut());
            try {
                waiter.waitForBytes(STARTUP_MSG.getBytes(), TIMEOUT);
            } catch (TimeoutException e) {
                System.out.println(server.getSystemOut());
                System.err.println(server.getSystemErr());
                throw e;
            }

            // needed to avoid blocking in client.events.take()
            testTimeouter = TimeoutInterrupter.start(Thread.currentThread(), TIMEOUT);
            return null;
        }

        public void destroy() {
            System.out.println("client.events = " + client.events);
            System.out.println("client.messages = " + client.messages);
            System.out.println("Server Out:");
            System.out.println(server.getSystemOut());
            System.err.println("Server Log:");
            System.err.println(server.getSystemErr());
            testTimeouter.interrupt();
            server.shutdown();
            tempDirectory.dispose();
        }

        public void theClientCanLogin() throws InterruptedException {
            client.login();
            String event = client.events.take();
            specify(event, event.startsWith(RpcTestClient.LOGGED_IN));
        }
    }

    // Interface implementaitons for connecting to Darkstar

    public static class RpcTestAppListener implements AppListener, Serializable {
        private static final long serialVersionUID = 1L;

        public void initialize(Properties props) {
            System.out.println(STARTUP_MSG);
        }

        public ClientSessionListener loggedIn(ClientSession session) {
            return new RpcTestClientSessionListener(session);
        }
    }

    private static class RpcTestClientSessionListener implements ClientSessionListener, ManagedObject, Serializable {
        private static final long serialVersionUID = 1L;

        private final ManagedReference<ClientSession> session;
        private final RpcGateway gateway;

        public RpcTestClientSessionListener(ClientSession session) {
            this.session = AppContext.getDataManager().createReference(session);
            gateway = initGateway(session);
            gateway.registerService(Echo.class, new EchoImpl());
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
        }
    }

    private static class RpcTestClient implements SimpleClientListener {

        public static final String LOGGED_IN = "loggedIn";
        public static final String LOGIN_FAILED = "loginFailed";
        public static final String RECONNECTING = "reconnecting";
        public static final String RECONNECTED = "reconnected";
        public static final String DISCONNECTED = "disconnected";
        public static final String JOINED_CHANNEL = "joinedChannel";

        public final BlockingQueue<String> events = new LinkedBlockingQueue<String>();
        public final BlockingQueue<String> messages = new LinkedBlockingQueue<String>();

        private final String host;
        private final int port;
        private final SimpleClient client;

        public RpcTestClient(String host, int port) {
            this.host = host;
            this.port = port;
            this.client = new SimpleClient(this);
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

        public void receivedMessage(ByteBuffer message) {
            byte[] bytes = ByteBufferUtils.asByteArray(message);
            messages.add(new String(bytes));
        }

        public ClientChannelListener joinedChannel(ClientChannel channel) {
            events.add(JOINED_CHANNEL + ": " + channel);
            return null;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("guest", "guest".toCharArray());
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

    // Application logic

    private interface Echo {
        Future<String> echo(String s);
    }

    private static class EchoImpl implements Echo, Serializable {
        private static final long serialVersionUID = 1L;

        public Future<String> echo(String s) {
            return ServiceHelper.wrap(s + ", " + s);
        }
    }
}
