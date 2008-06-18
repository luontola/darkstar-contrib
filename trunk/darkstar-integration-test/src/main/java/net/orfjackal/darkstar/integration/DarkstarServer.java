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
import java.util.Map;
import java.util.Properties;

/**
 * @author Esko Luontola
 * @since 17.6.2008
 */
public class DarkstarServer {

    public static final String MAIN_CLASS = "com.sun.sgs.impl.kernel.Kernel";

    private final JavaProcessExecutor executor = new JavaProcessExecutor();
    private final File workingDir;
    private ProcessHolder process;

    private String appName;
    private Class<? extends AppListener> appListener;
    private int port = 1139;
    private final Properties properties = new Properties();

    public DarkstarServer(File workingDir) {
        this.workingDir = workingDir;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Class<? extends AppListener> getAppListener() {
        return appListener;
    }

    public void setAppListener(Class<? extends AppListener> appListener) {
        this.appListener = appListener;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void start() {
        if (isRunning()) {
            throw new IllegalStateException("Already started");
        }
        File config = prepareAppConfig();
        process = executor.exec(getMainClass(), config.getAbsolutePath());
    }

    private File prepareAppConfig() {
        if (appName == null) {
            throw new IllegalArgumentException("appName is null");
        }
        if (appListener == null) {
            throw new IllegalArgumentException("appListener is null");
        }
        File appRoot = prepareAppRoot(appName);

        Properties appProps = new Properties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            appProps.setProperty(entry.getKey().toString(), entry.getValue().toString());
        }
        appProps.setProperty(StandardProperties.APP_NAME, appName);
        appProps.setProperty(StandardProperties.APP_ROOT, appRoot.getAbsolutePath());
        appProps.setProperty(StandardProperties.APP_LISTENER, appListener.getName());
        appProps.setProperty(StandardProperties.APP_PORT, Integer.toString(port));
        //appProps.setProperty(StandardProperties.MANAGERS, "");

        File appConfig = new File(workingDir, appName + ".properties");
        writeToFile(appConfig, appProps);
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
