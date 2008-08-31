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

import java.io.File;
import java.util.logging.Logger;

/**
 * @author Esko Luontola
 * @since 17.6.2008
 */
public class TempDirectory {

    private static final Logger log = Logger.getLogger(TempDirectory.class.getName());

    public static final String PREFIX = TempDirectory.class.getSimpleName() + ".";

    private File directory;

    public TempDirectory() {
        this(null);
    }

    public TempDirectory(File directory) {
        this.directory = directory;
    }

    public File getDirectory() {
        return directory;
    }

    public void create() {
        if (directory == null) {
            directory = nonExistingTempDir();
        }
        if (directory.exists()) {
            throw new IllegalArgumentException("Already exists: " + directory);
        }
        if (!directory.mkdir()) {
            throw new RuntimeException("Unable to create directory: " + directory);
        }
    }

    private static File nonExistingTempDir() {
        int i = 0;
        File dir;
        do {
            i++;
            dir = new File(System.getProperty("java.io.tmpdir"), PREFIX + i);
        } while (dir.exists());
        return dir;
    }

    public void dispose() {
        deleteRecursively(directory);
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File contained : file.listFiles()) {
                deleteRecursively(contained);
            }
        }
        if (!file.delete()) {
            log.warning("Unable to delete file: " + file);
            anotherDeleteAttempt(file);
        }
    }

    private static void anotherDeleteAttempt(File file) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        file.delete();
        file.deleteOnExit();
        if (!file.exists()) {
            log.info("File was deleted on a second try: " + file);
        }
    }
}
