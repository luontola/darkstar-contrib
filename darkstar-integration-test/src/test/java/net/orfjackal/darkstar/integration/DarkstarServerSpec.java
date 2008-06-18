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

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

/**
 * @author Esko Luontola
 * @since 17.6.2008
 */
@RunWith(JDaveRunner.class)
public class DarkstarServerSpec extends Specification<Object> {

    //private static final byte[] KERNEL_READY_MSG = "Kernel is ready".getBytes();
    private static final byte[] APPLICATION_READY_MSG = "application is ready".getBytes();
    private static final byte[] APPLESS_CONTEXT_READY_MSG = "non-application context is ready".getBytes();
    private static final int TIMEOUT = 10000;

    private TempDirectory tempDirectory;

    public void create() {
        tempDirectory = new TempDirectory();
        tempDirectory.create();
    }

    public void destroy() {
        tempDirectory.dispose();
    }


    public class WhenTheServerHasNotBeenStarted {

        private DarkstarServer server;

        public Object create() {
            server = new DarkstarServer(tempDirectory.getDirectory());
            return null;
        }

        public void destroy() {
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

        public void itIsNotListeningToTheSpecifiedPort() throws IOException {
            specify(new Block() {
                public void run() throws Throwable {
                    new Socket("localhost", server.getPort());
                }
            }, should.raise(ConnectException.class));
        }

        public void appNameMustBeSetBeforeStarting() {
            specify(new Block() {
                public void run() throws Throwable {
                    server.start();
                }
            }, should.raise(IllegalArgumentException.class, "appName is not set"));
        }
    }

    public class WhenAnEmptyServerIsStarted {

        private DarkstarServer server;
        private StreamWaiter waiter;

        public Object create() {
            server = new DarkstarServer(tempDirectory.getDirectory());
            server.setAppName("NoApp");
            server.start();
            waiter = new StreamWaiter(server.getSystemErr());
            return null;
        }

        public void destroy() {
            waiter.dispose();
            server.shutdown();
        }

        public void theServerStartsWithoutAppListener() throws InterruptedException, TimeoutException {
            specify(server.getAppListener(), should.equal(null));
            waiter.waitForBytes(APPLESS_CONTEXT_READY_MSG, TIMEOUT);
            String err = server.getSystemErr().toString();
            specify(err, err.contains("NoApp"));
        }
    }

    public class WhenTheServerIsStartedWithAnApplication {

        private DarkstarServer server;
        private StreamWaiter waiter;

        public Object create() throws InterruptedException {
            server = new DarkstarServer(tempDirectory.getDirectory());
            server.setAppName("HelloWorld");
            server.setAppListener(HelloWorld.class);
            server.setPort(12345);
            server.setProperty("my.custom.key", "MyValue");
            server.start();
            waiter = new StreamWaiter(server.getSystemErr());

            specify(server.getAppName(), should.equal("HelloWorld"));
            specify(server.getAppListener(), should.equal(HelloWorld.class));
            specify(server.getPort(), should.equal(12345));
            specify(server.getProperty("my.custom.key"), should.equal("MyValue"));
            return null;
        }

        public void destroy() {
            waiter.dispose();
            server.shutdown();
        }

        public void itIsRunning() {
            specify(server.isRunning());
        }

        public void itCanBeShutDown() {
            server.shutdown();
            specify(!server.isRunning());
        }

        public void itPrintsSomeLogMessages() throws InterruptedException, TimeoutException {
            waiter.waitForBytes(APPLICATION_READY_MSG, TIMEOUT);

            String out = server.getSystemOut().toString();
            String err = server.getSystemErr().toString();
            specify(err, err.contains("HelloWorld: application is ready"));
            specify(out, out.contains("Howdy ho!"));
        }

        public void itListensToTheSpecifiedPort() throws IOException {
            Socket clientSocket = new Socket("localhost", 12345);
            specify(clientSocket.isConnected());
            clientSocket.close();
        }

        public void customPropertyKeysCanBeSet() throws IOException {
            File configFile = new File(tempDirectory.getDirectory(), "HelloWorld.properties");
            Properties appProps = new Properties();
            FileInputStream in = new FileInputStream(configFile);
            appProps.load(in);
            in.close();
            specify(appProps.getProperty("my.custom.key"), should.equal("MyValue"));
        }

        public void allFilesAreWrittenInTheWorkingDirectory() throws InterruptedException, TimeoutException {
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
            specify((Object) totalSize, totalSize > 10 * MB);
        }

        public void itCanNotBeStartedWithoutFirstShuttingItDown() {
            specify(new Block() {
                public void run() throws Throwable {
                    server.start();
                }
            }, should.raise(IllegalStateException.class));
            specify(new Block() {
                public void run() throws Throwable {
                    server.start(new File(tempDirectory.getDirectory(), "HelloWorld.properties"));
                }
            }, should.raise(IllegalStateException.class));
        }

        public void itCanBeRestarted() throws InterruptedException, TimeoutException {
            waiter.waitForBytes(APPLICATION_READY_MSG, TIMEOUT);
            server.shutdown();
            specify(!server.isRunning());
            String log1 = server.getSystemErr().toString();
            specify(log1, log1.contains("The Kernel is ready"));
            specify(log1, log1.contains("HelloWorld: application is ready"));
            specify(log1, !log1.contains("recovering for node"));

            server.start();
            waiter.setStream(server.getSystemErr());
            waiter.waitForBytes(APPLICATION_READY_MSG, TIMEOUT);
            specify(server.isRunning());
            String log2 = server.getSystemErr().toString();
            specify(log2, log2.contains("The Kernel is ready"));
            specify(log2, log2.contains("HelloWorld: application is ready"));
            specify(log2, log2.contains("recovering for node"));
        }
    }

    public class WhenAnExistingAppPropertiesFileIsUsed {

        private DarkstarServer server;
        private StreamWaiter waiter;

        private File dsdb;

        public Object create() throws InterruptedException, IOException {
            File appRoot = new File(tempDirectory.getDirectory(), "customAppRoot");
            appRoot.mkdir();
            dsdb = new File(appRoot, "dsdb");
            dsdb.mkdir();

            Properties p = new Properties();
            p.setProperty(DarkstarServer.APP_NAME, "AppInCustomRoot");
            p.setProperty(DarkstarServer.APP_LISTENER, HelloWorld.class.getName());
            p.setProperty(DarkstarServer.APP_PORT, DarkstarServer.APP_PORT_DEFAULT);
            p.setProperty(DarkstarServer.APP_ROOT, appRoot.getAbsolutePath());
            File configFile = new File(appRoot, "MyConfig.properties");
            FileOutputStream out = new FileOutputStream(configFile);
            p.store(out, null);
            out.close();

            server = new DarkstarServer(tempDirectory.getDirectory());
            server.start(configFile);
            waiter = new StreamWaiter(server.getSystemErr());
            return null;
        }

        public void destroy() {
            System.out.println(server.getSystemOut());
            System.err.println(server.getSystemErr());
            waiter.dispose();
            server.shutdown();
        }

        public void theServerIsStartedUnderTheSpecifiedAppRoot() throws TimeoutException {
            waiter.waitForBytes(APPLICATION_READY_MSG, TIMEOUT);
            String err = server.getSystemErr().toString();
            specify(err, err.contains("AppInCustomRoot"));
            specify(dsdb.listFiles().length > 5);
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
