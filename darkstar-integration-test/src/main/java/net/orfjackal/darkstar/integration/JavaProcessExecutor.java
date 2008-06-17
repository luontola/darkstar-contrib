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

    public JavaProcessExecutor(ProcessExecutor executor) {
        this.executor = executor;
    }

    public void exec(Class<?> mainClass, String... args) {
        String java = quote(new File(new File(JAVA_HOME, "bin"), "java").getAbsolutePath());
        executor.exec(java +
                " -Djava.library.path=" + quote(LIBRARY_PATH) +
                " -classpath " + quote(CLASSPATH) +
                " " + mainClass.getName() + " " + quoteAll(args));
    }

    private static String quote(String s) {
        return '"' + s + '"';
    }

    private String quoteAll(String[] strings) {
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(quote(s));
        }
        return sb.toString();
    }
}
