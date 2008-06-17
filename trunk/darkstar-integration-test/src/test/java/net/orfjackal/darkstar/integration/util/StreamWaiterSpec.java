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

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;

/**
 * @author Esko Luontola
 * @since 17.6.2008
 */
@RunWith(JDaveRunner.class)
public class StreamWaiterSpec extends Specification<Object> {

    private ByteArrayOutputStream stream;
    private StreamWaiter waiter;

    public StreamWaiterSpec() {
        stream = new ByteArrayOutputStream();
        waiter = new StreamWaiter(stream);
    }

    public class IfThereIsNoActivityInTheStream {

        public Object create() {
            return null;
        }

        public void theWaiterWillStopWaitingAfterTheTimeout() {
            long waitTime = waiter.waitForSilenceOf(100);
            specify(waitTime, should.equal(100, 20));
        }
    }

    public class IfThereIsSomeActivityInTheStream {

        public Object create() {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    for (int i = 0; i < 10; i++) {
                        try {
                            Thread.sleep(10);
                            stream.write(i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            t.start();
            return null;
        }

        public void theWaiterWillWaitUntilThereHasBeenNoActivityForTheTimeoutsLength() {
            long waitTime = waiter.waitForSilenceOf(100);
            specify(waitTime, should.equal(200, 20));
        }
    }
}
