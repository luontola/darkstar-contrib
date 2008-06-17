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
import java.io.Serializable;
import java.util.Properties;

/**
 * @author Esko Luontola
 * @since 17.6.2008
 */
@RunWith(JDaveRunner.class)
public class DarkstarServerSpec extends Specification<Object> {

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
    }

    public class WhenTheServerIsStarted {

        private TempDirectory tempDirectory;
        private DarkstarServer server;

        public Object create() {
            tempDirectory = new TempDirectory();
            tempDirectory.create();
            server = new DarkstarServer(tempDirectory.getDirectory());
            server.start("HelloWorld", HelloWorld.class);
            return null;
        }

        public void destroy() {
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
            Thread.sleep(2000);

            String out = server.getSystemOut().toString();
            String err = server.getSystemErr().toString();
            specify(err.contains("HelloWorld: application is ready"));
            specify(out.contains("Howdy ho!"));
        }

        public void allFilesAreWrittenInTheWorkingDirectory() throws InterruptedException {
            long waited = new StreamWaiter(server.getSystemErr()).waitForSilenceOf(500);
            System.out.println("waited = " + waited);
            System.out.println(server.getSystemErr());

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
                    server.start("HelloWorld", HelloWorld.class);
                }
            }, should.raise(IllegalStateException.class));
        }

        public void itCanBeRestarted() throws InterruptedException {
            // the "application is ready" message comes only on when starting with an empty data store
            Thread.sleep(1000);
            server.shutdown();
            specify(!server.isRunning());
            specify(server.getSystemErr().toString().contains("The Kernel is ready"));
            specify(server.getSystemErr().toString().contains("HelloWorld: application is ready"));

            server.start("HelloWorld", HelloWorld.class);
            Thread.sleep(1000);
            specify(server.isRunning());
            specify(server.getSystemErr().toString().contains("The Kernel is ready"));
            specify(!server.getSystemErr().toString().contains("HelloWorld: application is ready"));
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
