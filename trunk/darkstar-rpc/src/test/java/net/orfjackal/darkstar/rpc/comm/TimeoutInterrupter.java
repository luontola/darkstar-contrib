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

package net.orfjackal.darkstar.rpc.comm;

/**
 * Interrupts the specified thread after the timeout has passed, unless the thread
 * where this interrupter is running is interrupted first.
 */
public class TimeoutInterrupter implements Runnable {

    // TODO: move this utility to a utility project and write tests for it
    // test: wait on java.util.concurrent.BlockingQueue.take, nothing to take -> interrupted
    // test: wait on java.util.concurrent.BlockingQueue.take, somethign to take -> no interrupt

    private final Thread threadToInterrupt;
    private final int timeout;

    public static Thread start(Thread threadToInterrupt, int timeout) {
        Thread t = new Thread(new TimeoutInterrupter(threadToInterrupt, timeout));
        t.setDaemon(true);
        t.start();
        return t;
    }

    public TimeoutInterrupter(Thread threadToInterrupt, int timeout) {
        this.threadToInterrupt = threadToInterrupt;
        this.timeout = timeout;
    }

    public void run() {
        try {
            Thread.sleep(timeout);
            if (!Thread.currentThread().isInterrupted()) {
                threadToInterrupt.interrupt();
            }
        } catch (InterruptedException e) {
            // this interrupter was itself interrupted - do nothing
        }
    }
}
