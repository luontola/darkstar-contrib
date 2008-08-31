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

package net.orfjackal.darkstar.integration.util;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * @author Esko Luontola
 * @since 17.6.2008
 */
@RunWith(JDaveRunner.class)
public class JavaProcessExecutorSpec extends Specification<Object> {

    private static String fromEnd(List<String> x, int i) {
        return x.get(x.size() - i);
    }

    public class InTheCommandForLaunchingTheChildJavaProcess {

        private DummyProcessExecutor dummyExecutor;
        private JavaProcessExecutor javaExecutor;

        public Object create() {
            dummyExecutor = new DummyProcessExecutor();
            javaExecutor = new JavaProcessExecutor(dummyExecutor);
            return null;
        }

        public void theSpecifiedMainClassIsUsed() {
            javaExecutor.exec(HelloWorld.class);
            specify(fromEnd(dummyExecutor.lastCommand, 1).equals(HelloWorld.class.getName()));
        }

        public void theSpecifiedProgramArgumentsAreUsed() {
            javaExecutor.exec(HelloWorld.class, "foo", "bar");
            specify(fromEnd(dummyExecutor.lastCommand, 2).equals("foo"));
            specify(fromEnd(dummyExecutor.lastCommand, 1).equals("bar"));
        }

        public void jreIsTheSameAsInTheParent() {
            javaExecutor.exec(HelloWorld.class);
            String javaHome = System.getProperty("java.home");
            String sep = System.getProperty("file.separator");
            specify(dummyExecutor.lastCommand.get(0), should.equal(javaHome + sep + "bin" + sep + "java"));
        }

        public void classpathIsTheSameAsInTheParent() {
            javaExecutor.exec(HelloWorld.class);
            String classpath = System.getProperty("java.class.path");
            specify(dummyExecutor.lastCommand, should.containInPartialOrder("-classpath", classpath));
        }

        public void libraryPathIsTheSameAsInTheParent() {
            javaExecutor.exec(HelloWorld.class);
            String libraryPath = System.getProperty("java.library.path");
            specify(dummyExecutor.lastCommand, should.contain("-Djava.library.path=" + libraryPath));
        }

        public void vmOptionsMayBeSpecified() {
            javaExecutor.exec(HelloWorld.class);
            specify(dummyExecutor.lastCommand, should.not().contain("-ea"));
            specify(dummyExecutor.lastCommand, should.not().contain("-server"));
            specify(dummyExecutor.lastCommand, should.not().contain("null"));

            javaExecutor.setVmOptions("-ea", "-server");
            javaExecutor.exec(HelloWorld.class);
            specify(dummyExecutor.lastCommand, should.contain("-ea"));
            specify(dummyExecutor.lastCommand, should.contain("-server"));
            specify(javaExecutor.getVmOptions(), should.containInOrder("-ea", "-server"));
        }

        public void tempDirectoryMayBeSpecified() {
            File dir = new File(System.getProperty("java.io.tmpdir"), "CustomTemp.tmp");

            javaExecutor.exec(HelloWorld.class);
            specify(dummyExecutor.lastCommand, should.not().contain("CustomTemp.tmp"));
            specify(dummyExecutor.lastCommand, should.not().contain("null"));

            javaExecutor.setTempDirectory(dir);
            javaExecutor.exec(HelloWorld.class);
            specify(dummyExecutor.lastCommand, should.contain("-Djava.io.tmpdir=" + dir));
            specify(javaExecutor.getTempDirectory(), should.equal(dir));
        }
    }

    public class WhenTheChildJavaProcessIsExecuted {

        private JavaProcessExecutor javaExecutor;

        public Object create() {
            javaExecutor = new JavaProcessExecutor();
            return null;
        }

        public void systemOutCanBeRead() throws InterruptedException {
            ProcessHolder p = javaExecutor.exec(HelloWorld.class);
            p.getProcess().waitFor();
            specify(p.getSystemOut().toString().trim(), should.equal("Hello world!"));
        }

        public void systemErrCanBeRead() throws InterruptedException {
            ProcessHolder p = javaExecutor.exec(WazzupWorld.class);
            p.getProcess().waitFor();
            specify(p.getSystemErr().toString().trim(), should.equal("Wazzup world!"));
        }

        public void exitValueCanBeRead() throws InterruptedException {
            ProcessHolder ok = javaExecutor.exec(HelloWorld.class);
            specify(ok.getProcess().waitFor(), should.equal(0));
            ProcessHolder fail = javaExecutor.exec(WazzupWorld.class);
            specify(fail.getProcess().waitFor(), should.equal(1));
        }

        public void programArgumentsAreTransmittedCorrectly() throws InterruptedException {
            ProcessHolder p = javaExecutor.exec(HelloWorld.class, "foobar");
            p.getProcess().waitFor();
            specify(p.getSystemOut().toString().contains("foobar"));
        }

        public void systemPropertiesAreTransmittedCorrectly() throws IOException, InterruptedException {
            javaExecutor.setVmOptions("-DtestProperty=foobar");
            ProcessHolder p = javaExecutor.exec(SystemPropertiesPrinter.class);
            p.getProcess().waitFor();

            Properties child = new Properties();
            child.load(new ByteArrayInputStream(p.getSystemOut().toByteArray()));

            specify(child.getProperty("testProperty"), should.equal("foobar"));
            specify(child.getProperty("java.home"), should.equal(System.getProperty("java.home")));
            specify(child.getProperty("java.class.path"), should.equal(System.getProperty("java.class.path")));
            specify(child.getProperty("java.library.path"), should.equal(System.getProperty("java.library.path")));
        }
    }

    private static class HelloWorld {
        public static void main(String[] args) {
            System.out.println("Hello world!");
            for (String arg : args) {
                System.out.println(arg);
            }
            System.exit(0);
        }
    }

    private static class WazzupWorld {
        public static void main(String[] args) {
            System.err.println("Wazzup world!");
            System.exit(1);
        }
    }

    private static class SystemPropertiesPrinter {
        public static void main(String[] args) throws IOException {
            System.getProperties().store(System.out, null);
        }
    }

//    private static void debugSystemProperties() {
//        Properties properties = System.getProperties();
//        List<String> pairs = new ArrayList<String>();
//        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
//            pairs.add(entry.getKey() + "\t= " + entry.getValue());
//        }
//        Collections.sort(pairs);
//        for (String pair : pairs) {
//            System.err.println(pair);
//        }
//    }
}
