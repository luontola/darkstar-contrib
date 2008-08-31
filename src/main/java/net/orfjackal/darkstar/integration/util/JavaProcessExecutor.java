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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Esko Luontola
 * @since 17.6.2008
 */
public class JavaProcessExecutor {

    private static final String JAVA_HOME = System.getProperty("java.home");
    private static final String CLASSPATH = System.getProperty("java.class.path");
    private static final String LIBRARY_PATH = System.getProperty("java.library.path");

    private final ProcessExecutor executor;
    private String[] vmOptions = new String[0];
    private File tempDirectory;

    public JavaProcessExecutor() {
        this(new ProcessExecutorImpl());
    }

    public JavaProcessExecutor(ProcessExecutor executor) {
        this.executor = executor;
    }

    public String[] getVmOptions() {
        return vmOptions;
    }

    public void setVmOptions(String... vmOptions) {
        if (vmOptions == null) {
            throw new NullPointerException("vmOptions is null");
        }
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
        Process process = executor.exec(commandFor(mainClass, args), out, err);
        return new ProcessHolder(process, out, err);
    }

    private String[] commandFor(Class<?> mainClass, String[] args) {
        return combinedArray(
                java(), vmOptions, java_io_tmpdir(), java_library_path(), classpath(),
                mainClass.getName(), args);
    }

    private String java() {
        return new File(new File(JAVA_HOME, "bin"), "java").getAbsolutePath();
    }

    private String[] classpath() {
        return new String[]{"-classpath", CLASSPATH};
    }

    private String java_library_path() {
        return "-Djava.library.path=" + LIBRARY_PATH;
    }

    private String java_io_tmpdir() {
        if (tempDirectory == null) {
            return null;
        }
        return "-Djava.io.tmpdir=" + tempDirectory.getAbsolutePath();
    }

    private String[] combinedArray(Object... elements) {
        List<String> result = new ArrayList<String>();
        for (Object o : elements) {
            if (o instanceof String) {
                result.add((String) o);
            } else if (o instanceof String[]) {
                result.addAll(Arrays.asList((String[]) o));
            } else if (o == null) {
                // NOOP: do not add empty elements
            } else {
                throw new IllegalArgumentException("Element of wrong type: " + o + " (" + o.getClass() + ")");
            }
        }
        return result.toArray(new String[result.size()]);
    }
}
