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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * For debugging purposes, does not execute anything - only prints the command.
 *
 * @author Esko Luontola
 * @since 1.12.2007
 */
public class DummyProcessExecutor implements ProcessExecutor {

    private static final int LINE_LENGTH = 120;

    private final boolean printCommand;
    public String lastCommand;

    public DummyProcessExecutor() {
        this(false);
    }

    public DummyProcessExecutor(boolean printCommand) {
        this.printCommand = printCommand;
    }

    public Process exec(String command) {
        lastCommand = command;
        if (printCommand) {
            System.out.println(DummyProcessExecutor.class.getName() + ".execute(), command:");
            System.out.print(lineWrap(command));
        }
        return new DummyProcess();
    }

    public Process exec(String command, OutputStream stdout, OutputStream stderr) {
        exec(command);
        return new DummyProcess();
    }

    private static String lineWrap(String text) {
        StringBuilder wrapped = new StringBuilder();
        for (int begin = 0; begin < text.length(); begin += LINE_LENGTH) {
            int end = Math.min(begin + LINE_LENGTH, text.length());
            wrapped.append(text.substring(begin, end));
            wrapped.append("\n");
        }
        return wrapped.toString();
    }

    private static class DummyProcess extends Process {

        public OutputStream getOutputStream() {
            return null;
        }

        public InputStream getInputStream() {
            return null;
        }

        public InputStream getErrorStream() {
            return null;
        }

        public int waitFor() throws InterruptedException {
            return 0;
        }

        public int exitValue() {
            return 0;
        }

        public void destroy() {
        }
    }
}
