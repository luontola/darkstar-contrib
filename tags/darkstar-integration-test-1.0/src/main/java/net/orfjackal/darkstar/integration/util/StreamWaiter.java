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
import java.util.concurrent.TimeoutException;

/**
 * @author Esko Luontola
 * @since 17.6.2008
 */
public class StreamWaiter {

    private final Thread monitor;
    private volatile ByteArrayOutputStream stream;
    private volatile long lastActivity;

    public StreamWaiter(ByteArrayOutputStream stream) {
        setStream(stream);
        monitor = new Thread(new MonitorRunnable());
        monitor.setDaemon(true);
        monitor.start();
    }

    public void setStream(ByteArrayOutputStream stream) {
        this.stream = stream;
        lastActivity = System.currentTimeMillis();
    }

    public void dispose() {
        monitor.interrupt();
    }

    public long waitForSilenceOf(int millis) {
        long start = System.currentTimeMillis();
        Thread t = new Thread(new WaiterRunnable(millis));
        t.setDaemon(true);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    public long waitForBytes(byte[] needle, int timeout) throws TimeoutException {
        long start = System.currentTimeMillis();
        while (!contains(needle, stream.toByteArray())) {
            if (System.currentTimeMillis() > start + timeout) {
                throw new TimeoutException("Stream did not contain: " + new String(needle));
            }
            sleep(5);
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    private static boolean contains(byte[] needle, byte[] haystack) {
        for (int h = 0; h < haystack.length; h++) {
            int matches = 0;
            for (int n = 0; n < needle.length && h + n < haystack.length; n++) {
                if (haystack[h + n] == needle[n]) {
                    matches++;
                } else {
                    break;
                }
            }
            if (matches == needle.length) {
                return true;
            }
        }
        return false;
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class WaiterRunnable implements Runnable {

        private final int timeout;

        public WaiterRunnable(int timeout) {
            this.timeout = timeout;
        }

        public void run() {
            assert Thread.currentThread().isDaemon();
            while (System.currentTimeMillis() < lastActivity + timeout) {
                sleep(5);
            }
        }
    }

    private class MonitorRunnable implements Runnable {

        public void run() {
            assert Thread.currentThread().isDaemon();
            try {
                int lastSize = 0;
                do {
                    lastSize = checkForActivity(lastSize);
                    Thread.sleep(5);
                } while (!Thread.currentThread().isInterrupted());

            } catch (InterruptedException e) {
                // stop monitoring
            }
        }

        private int checkForActivity(int lastSize) {
            int currentSize = stream.size();
            if (currentSize > lastSize) {
                lastActivity = System.currentTimeMillis();
            }
            return currentSize;
        }
    }
}
