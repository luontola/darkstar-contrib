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

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Esko Luontola
 * @since 17.6.2008
 */
@RunWith(JDaveRunner.class)
public class JavaProcessExecutorSpec extends Specification<Object> {

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
            specify(dummyExecutor.lastCommand.endsWith(" " + HelloWorld.class.getName()));
        }

        public void theSpecifiedProgramArgumentsAreUsed() {
            javaExecutor.exec(HelloWorld.class, "foo", "bar");
            specify(dummyExecutor.lastCommand.endsWith(" \"foo\" \"bar\""));
        }

        public void jreIsTheSameAsInTheParent() {
            javaExecutor.exec(HelloWorld.class);
            String javaHome = System.getProperty("java.home");
            String sep = System.getProperty("file.separator");
            specify(dummyExecutor.lastCommand.startsWith("\"" + javaHome + sep + "bin" + sep + "java\" "));
        }

        public void classpathIsTheSameAsInTheParent() {
            javaExecutor.exec(HelloWorld.class);
            String classpath = System.getProperty("java.class.path");
            specify(dummyExecutor.lastCommand.contains(" -classpath \"" + classpath + "\" "));
        }

        public void libraryPathIsTheSameAsInTheParent() {
            javaExecutor.exec(HelloWorld.class);
            String libraryPath = System.getProperty("java.library.path");
            specify(dummyExecutor.lastCommand.contains(" -Djava.library.path=\"" + libraryPath + "\" "));
        }

        public void vmOptionsMayBeSpecified() {
            javaExecutor.exec(HelloWorld.class);
            specify(!dummyExecutor.lastCommand.contains("-ea -server"));
            specify(!dummyExecutor.lastCommand.contains("null"));

            javaExecutor.setVmOptions("-ea -server");
            javaExecutor.exec(HelloWorld.class);
            specify(dummyExecutor.lastCommand.contains(" -ea -server "));
        }

        public void tempDirectoryMayBeSpecified() {
            File dir = new File(System.getProperty("java.io.tmpdir"), "CustomTemp.tmp");

            javaExecutor.exec(HelloWorld.class);
            specify(!dummyExecutor.lastCommand.contains("CustomTemp.tmp"));
            specify(!dummyExecutor.lastCommand.contains("null"));

            javaExecutor.setTempDirectory(dir);
            javaExecutor.exec(HelloWorld.class);
            specify(dummyExecutor.lastCommand.contains(" -Djava.io.tmpdir=\"" + dir + "\" "));
        }
    }

    public class WhenTheChildJavaProcessIsExecuted {

        private JavaProcessExecutor javaExecutor;

        public Object create() {
            javaExecutor = new JavaProcessExecutor();
            return null;
        }

        public void theSystemOutCanBeRead() {
            ProcessResult result = javaExecutor.exec(HelloWorld.class);
            specify(result.getSystemOut().trim(), should.equal("Hello world!"));
        }

        public void theSystemErrCanBeRead() {
            ProcessResult result = javaExecutor.exec(WazzupWorld.class);
            specify(result.getSystemErr().trim(), should.equal("Wazzup world!"));
        }

        public void programArgumentsCanBeGiven() {
            ProcessResult result = javaExecutor.exec(HelloWorld.class, "foobar");
            specify(result.getSystemOut().contains("foobar"));
        }

        public void systemPropertiesAreTransmittedCorrectly() throws IOException {
            javaExecutor.setVmOptions("-DtestProperty=foobar");
            ProcessResult result = javaExecutor.exec(SystemPropertiesPrinter.class);

            Properties child = new Properties();
            child.load(new ByteArrayInputStream(result.getSystemOut().getBytes()));

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
        }
    }

    private static class WazzupWorld {
        public static void main(String[] args) {
            System.err.println("Wazzup world!");
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
