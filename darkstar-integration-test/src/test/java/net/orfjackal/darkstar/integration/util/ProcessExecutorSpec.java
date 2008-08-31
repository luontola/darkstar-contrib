/*
 * Copyright (c) 2007, Esko Luontola. All Rights Reserved.
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

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * @author Esko Luontola
 * @since 6.12.2007
 */
@RunWith(JDaveRunner.class)
public class ProcessExecutorSpec extends Specification<ProcessExecutor> {

    public class AProcessExecutor {

        private ByteArrayOutputStream stdout;
        private ByteArrayOutputStream stderr;
        private ProcessExecutor executor;

        public ProcessExecutor create() {
            stdout = new ByteArrayOutputStream();
            stderr = new ByteArrayOutputStream();
            executor = new ProcessExecutorImpl();
            return executor;
        }

        public void shouldExecuteTheSystemCommand() throws InterruptedException {
            File f = new File("testExecuter.tmp");
            specify(!f.exists());
            Process p = executor.exec("cmd", "/c", "mkdir", "testExecuter.tmp");
            p.waitFor();
            specify(f.exists());
            specify(f.isDirectory());
            specify(f.delete());
        }

        public void shouldRedirectStdout() throws InterruptedException {
            // "cmd /c" is required by Windows because "echo" is not a file (unlike in Linux) but a shell command
            Process p = executor.exec(new String[]{"cmd", "/c", "echo", "foo"}, stdout, stderr);
            p.waitFor();
            specify(stdout.toString(), should.equal("foo\r\n"));
            specify(stderr.toString(), should.equal(""));
        }

        public void shouldRedirectStderr() throws InterruptedException {
            Process p = executor.exec(new String[]{"cmd", "/c", "echo", "bar>&2"}, stdout, stderr);
            p.waitFor();
            specify(stdout.toString(), should.equal(""));
            specify(stderr.toString(), should.equal("bar\r\n"));
        }

        public void shouldReturnTheExitValue() throws InterruptedException {
            int ok = executor.exec(new String[]{"cmd", "/c", "echo", "foo"}, stdout, stderr).waitFor();
            int fail = executor.exec(new String[]{"cmd", "/c", "dir", "doesNotExist"}, stdout, stderr).waitFor();
            specify(ok, should.equal(0));
            specify(fail, should.equal(1));
        }
    }
}
