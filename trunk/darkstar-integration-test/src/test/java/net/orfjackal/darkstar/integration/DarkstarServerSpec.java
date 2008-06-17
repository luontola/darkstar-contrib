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

import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import jdave.Block;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.darkstar.integration.util.StreamWaiter;
import net.orfjackal.darkstar.integration.util.TempDirectory;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.Properties;

/**
 * @author Esko Luontola
 * @since 17.6.2008
 */
@RunWith(JDaveRunner.class)
public class DarkstarServerSpec extends Specification<Object> {

    private static final int TIMEOUT = 10000;
    private static final byte[] APPLICATION_READY_MSG = "application is ready".getBytes();

    public class WhenTheServerHasNotBeenStarted {

        private TempDirectory tempDirectory;
        private DarkstarServer server;

        public Object create() {
            tempDirectory = new TempDirectory();
            tempDirectory.create();
            server = new DarkstarServer(tempDirectory.getDirectory());
            return null;
        }

        public void destroy() {
            tempDirectory.dispose();
        }

        public void itIsNotRunning() {
            specify(!server.isRunning());
        }

        public void itCanNotBeShutDown() {
            specify(new Block() {
                public void run() throws Throwable {
                    server.shutdown();
                }
            }, should.raise(IllegalStateException.class));
        }

        public void appNameMustBeSetBeforeStarting() {
            server.setAppListener(HelloWorld.class);
            specify(new Block() {
                public void run() throws Throwable {
                    server.start();
                }
            }, should.raise(IllegalArgumentException.class, "appName is null"));
        }

        public void appListenerMustBeSetBeforeStarting() {
            server.setAppName("HelloWorld");
            specify(new Block() {
                public void run() throws Throwable {
                    server.start();
                }
            }, should.raise(IllegalArgumentException.class, "appListener is null"));
        }
    }

    public class WhenTheServerIsStarted {

        private TempDirectory tempDirectory;
        private DarkstarServer server;
        private StreamWaiter waiter;

        public Object create() throws InterruptedException {
            tempDirectory = new TempDirectory();
            tempDirectory.create();
            server = new DarkstarServer(tempDirectory.getDirectory());
            server.setAppName("HelloWorld");
            server.setAppListener(HelloWorld.class);
            server.setPort(12345);
            server.start();
            waiter = new StreamWaiter(server.getSystemErr());
            return null;
        }

        public void destroy() {
            waiter.dispose();
            server.shutdown();
            tempDirectory.dispose();
        }

        public void itIsRunning() {
            specify(server.isRunning());
        }

        public void itCanBeShutDown() {
            server.shutdown();
            specify(!server.isRunning());
        }

        public void itPrintsSomeLogMessages() throws InterruptedException {
            waiter.waitForBytes(APPLICATION_READY_MSG, TIMEOUT);

            String out = server.getSystemOut().toString();
            String err = server.getSystemErr().toString();
            specify(err.contains("HelloWorld: application is ready"));
            specify(out.contains("Howdy ho!"));
        }

        public void itListensToTheSpecifiedPort() throws IOException {
            Socket clientSocket = new Socket("localhost", 12345);
            specify(clientSocket.isConnected());
            clientSocket.close();
        }

        public void allFilesAreWrittenInTheWorkingDirectory() throws InterruptedException {
            waiter.waitForBytes(APPLICATION_READY_MSG, TIMEOUT);

            File dir = tempDirectory.getDirectory();
            File appProps = new File(dir, "HelloWorld.properties");
            File dataDir = new File(dir, "data" + File.separator + "HelloWorld" + File.separator + "dsdb");
            specify(appProps.isFile());
            specify(dataDir.isDirectory());

            final long MB = 1024 * 1024;
            long totalSize = 0;
            for (File file : dataDir.listFiles()) {
                totalSize += file.length();
            }
            specify(totalSize > 10 * MB);
        }

        public void itCanNotBeStartedWithoutFirstShuttingItDown() {
            specify(new Block() {
                public void run() throws Throwable {
                    server.start();
                }
            }, should.raise(IllegalStateException.class));
        }

        public void itCanBeRestarted() throws InterruptedException {
            waiter.waitForBytes(APPLICATION_READY_MSG, TIMEOUT);
            server.shutdown();
            specify(!server.isRunning());
            String log1 = server.getSystemErr().toString();
            specify(log1.contains("The Kernel is ready"));
            specify(log1.contains("HelloWorld: application is ready"));
            specify(!log1.contains("recovering for node"));

            server.start();
            waiter.setStream(server.getSystemErr());
            waiter.waitForBytes(APPLICATION_READY_MSG, TIMEOUT);
            specify(server.isRunning());
            String log2 = server.getSystemErr().toString();
            specify(log2.contains("The Kernel is ready"));
            specify(log2.contains("HelloWorld: application is ready"));
            specify(log2.contains("recovering for node"));
        }
    }

    public static class HelloWorld implements AppListener, Serializable {

        private static final long serialVersionUID = 1L;

        public void initialize(Properties props) {
            System.out.println("Howdy ho!");
        }

        public ClientSessionListener loggedIn(ClientSession session) {
            return null;
        }
    }
}
