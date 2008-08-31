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

import jdave.Block;
import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 13.7.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TimedInterruptSpec extends Specification<Object> {

    private static final int TIMEOUT = 50;
    private static final double DELTA = 20.0;

    private Thread timer;

    public void create() throws Exception {
        timer = TimedInterrupt.startOnCurrentThread(TIMEOUT);
    }

    public void destroy() throws Exception {
        timer.interrupt();
    }


    public class WhenTheTimeoutIsExceeded {

        public Object create() {
            return null;
        }

        public void theThreadIsInterrupted() {
            long start = System.currentTimeMillis();
            specify(new Block() {
                public void run() throws Throwable {
                    Thread.sleep(TIMEOUT * 2);
                }
            }, should.raise(InterruptedException.class));
            long end = System.currentTimeMillis();
            specify(end - start, should.equal(TIMEOUT, DELTA));
        }
    }

    public class WhenTheTimeoutIsCancelledFirst {

        public Object create() throws InterruptedException {
            return null;
        }

        public void theThreadIsNotInterrupted() throws InterruptedException {
            long start = System.currentTimeMillis();
            timer.interrupt();
            Thread.sleep(TIMEOUT * 2);
            long end = System.currentTimeMillis();
            specify(end - start, should.equal(TIMEOUT * 2, DELTA));
        }
    }
}
