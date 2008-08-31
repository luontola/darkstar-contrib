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

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * @author Esko Luontola
 * @since 17.6.2008
 */
public class JavaProcessExecutor {

    private static final String JAVA_HOME = System.getProperty("java.home");
    private static final String CLASSPATH = System.getProperty("java.class.path");
    private static final String LIBRARY_PATH = System.getProperty("java.library.path");

    private final ProcessExecutor executor;
    private String vmOptions;
    private File tempDirectory;

    public JavaProcessExecutor() {
        this(new ProcessExecutorImpl());
    }

    public JavaProcessExecutor(ProcessExecutor executor) {
        this.executor = executor;
    }

    public String getVmOptions() {
        return vmOptions;
    }

    public void setVmOptions(String vmOptions) {
        this.vmOptions = vmOptions;
    }

    public File getTempDirectory() {
        return tempDirectory;
    }

    public void setTempDirectory(File tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public ProcessHolder exec(Class<?> mainClass, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        Process process = executor.exec(commandFor(mainClass, args).trim(), out, err);
        return new ProcessHolder(process, out, err);
    }

    private String commandFor(Class<?> mainClass, String[] args) {
        return java()
                + optional(vmOptions)
                + optional(java_io_tmpdir())
                + java_library_path()
                + classpath()
                + " " + mainClass.getName() + " " + quoteAll(args);
    }

    private String java() {
        return quote(new File(new File(JAVA_HOME, "bin"), "java").getAbsolutePath());
    }

    private String classpath() {
        return " -classpath " + quote(CLASSPATH);
    }

    private String java_library_path() {
    	// TODO: escaping seems to go wrong if (on windows) the path ends with \
        return " -Djava.library.path=" + quote(LIBRARY_PATH);
    }

    private String java_io_tmpdir() {
        if (tempDirectory == null) {
            return null;
        }
        return " -Djava.io.tmpdir=" + quote(tempDirectory.getAbsolutePath());
    }

    private static String optional(String s) {
        return (s != null ? " " + s.trim() : "");
    }

    private static String quote(String s) {
        return '"' + s + '"';
    }

    private String quoteAll(String[] strings) {
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb.append(quote(s)).append(" ");
        }
        return sb.toString();
    }
}
