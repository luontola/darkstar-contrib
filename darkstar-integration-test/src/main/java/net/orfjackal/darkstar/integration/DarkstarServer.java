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
import com.sun.sgs.impl.kernel.StandardProperties;
import net.orfjackal.darkstar.integration.util.JavaProcessExecutor;
import net.orfjackal.darkstar.integration.util.ProcessHolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Esko Luontola
 * @since 17.6.2008
 */
public class DarkstarServer {

    public static final String MAIN_CLASS = "com.sun.sgs.impl.kernel.Kernel";
    public static final String APP_ROOT = StandardProperties.APP_ROOT;
    public static final String APP_NAME = StandardProperties.APP_NAME;
    public static final String APP_LISTENER = StandardProperties.APP_LISTENER;
    public static final String APP_LISTENER_NONE = StandardProperties.APP_LISTENER_NONE;
    public static final String APP_PORT = StandardProperties.APP_PORT;
    public static final String APP_PORT_DEFAULT = "1139";

    private final JavaProcessExecutor executor = new JavaProcessExecutor();
    private final File workingDir;
    private final Properties appProperties;
    private ProcessHolder process;

    public DarkstarServer(File workingDir) {
        this.workingDir = workingDir;
        appProperties = new Properties();
        appProperties.setProperty(APP_NAME, "");
        appProperties.setProperty(APP_LISTENER, APP_LISTENER_NONE);
        appProperties.setProperty(APP_PORT, APP_PORT_DEFAULT);
    }

    public String getAppName() {
        return getProperty(APP_NAME);
    }

    public void setAppName(String appName) {
        setProperty(APP_NAME, appName);
    }

    public Class<? extends AppListener> getAppListener() {
        String className = getProperty(APP_LISTENER);
        if (className.equals(APP_LISTENER_NONE)) {
            return null;
        }
        try {
            return Class.forName(className).asSubclass(AppListener.class);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void setAppListener(Class<? extends AppListener> appListener) {
        setProperty(APP_LISTENER, appListener.getName());
    }

    public int getPort() {
        return Integer.valueOf(getProperty(APP_PORT));
    }

    public void setPort(int port) {
        setProperty(APP_PORT, Integer.toString(port));
    }

    public void setProperty(String key, String value) {
        appProperties.setProperty(key, value);
    }

    public String getProperty(String key) {
        return appProperties.getProperty(key);
    }

    public void start() {
        if (isRunning()) {
            throw new IllegalStateException("Already started");
        }
        File config = prepareAppConfig();
        process = executor.exec(getMainClass(), config.getAbsolutePath());
    }

    private File prepareAppConfig() {
        if (getAppName().equals("")) {
            throw new IllegalArgumentException("appName is not set");
        }
        File appRoot = prepareAppRoot(getAppName());
        appProperties.setProperty(APP_ROOT, appRoot.getAbsolutePath());

        File appConfig = new File(workingDir, getAppName() + ".properties");
        writeToFile(appConfig, appProperties);
        return appConfig;
    }

    private File prepareAppRoot(String appName) {
        File appRoot = new File(workingDir, "data" + File.separator + appName);
        File dataDir = new File(appRoot, "dsdb");
        dataDir.mkdirs();
        if (!dataDir.isDirectory()) {
            throw new RuntimeException("Unable to create data directory: " + dataDir);
        }
        return appRoot;
    }

    private static void writeToFile(File file, Properties properties) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            properties.store(out, null);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> getMainClass() {
        try {
            return DarkstarServer.class.getClassLoader().loadClass(MAIN_CLASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        if (process == null) {
            throw new IllegalStateException("Not started");
        }
        Process p = process.getProcess();
        p.destroy();
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        try {
            if (process != null) {
                process.getProcess().exitValue();
            }
            return false;
        } catch (IllegalThreadStateException e) {
            // exitValue throws an exception if the process is running
            return true;
        }
    }

    public ByteArrayOutputStream getSystemOut() {
        return process.getSystemOut();
    }

    public ByteArrayOutputStream getSystemErr() {
        return process.getSystemErr();
    }
}
