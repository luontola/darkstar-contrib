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
import java.io.IOException;

/**
 * @author Esko Luontola
 * @since 17.6.2008
 */
@RunWith(JDaveRunner.class)
public class StreamWaiterSpec extends Specification<Object> {

    private static final int DELTA = 40;

    private ByteArrayOutputStream stream;
    private StreamWaiter waiter;

    public StreamWaiterSpec() {
        stream = new ByteArrayOutputStream();
        waiter = new StreamWaiter(stream);
    }

    public class WhenThereIsNoActivityInTheStream {

        public Object create() {
            return null;
        }

        public void theWaiterWillStopWaitingAfterTheTimeout() {
            long waitTime = waiter.waitForSilenceOf(100);
            specify(waitTime, should.equal(100, DELTA));
        }
    }

    public class WhenThereIsSomeActivityWhileWaiting {

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
            specify(waitTime, should.equal(200, DELTA));
        }
    }

    public class WhenThereWasSomeActivityBeforeWaitingStarted {

        public Object create() {
            stream.write(1);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void theWaiterMeasuresTheTimeoutSinceThePastActivity() {
            long waitTime = waiter.waitForSilenceOf(200);
            specify(waitTime, should.equal(100, DELTA));
        }
    }

    public class WhenTheMonitorIsDisposed {

        public Object create() {
            stream.write(1);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void theStreamWillNotAnymoreBeMonitoredByADaemonThread() throws InterruptedException {
            waiter.dispose();
            Thread.sleep(50);
            stream.write(1);
            long waitTime = waiter.waitForSilenceOf(200);
            specify(waitTime, should.equal(50, DELTA));
        }
    }

    public class WhenTheStreamBeingMonitoredIsChanged {

        public Object create() {
            return null;
        }

        public void theActivityTimerWillBeReset() throws InterruptedException {
            Thread.sleep(100);
            long waitTime1 = waiter.waitForSilenceOf(100);
            specify(waitTime1, should.equal(0, DELTA));

            waiter.setStream(new ByteArrayOutputStream());
            long waitTime2 = waiter.waitForSilenceOf(100);
            specify(waitTime2, should.equal(100, DELTA));
        }

        public void theNewStreamWillBeMonitored() throws IOException, InterruptedException {
            stream.write(new byte[10]);
            Thread.sleep(50);

            final ByteArrayOutputStream newStream = new ByteArrayOutputStream();
            waiter.setStream(newStream);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    for (int i = 0; i < 10; i++) {
                        try {
                            Thread.sleep(10);
                            newStream.write(i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            t.start();
            long waitTime = waiter.waitForSilenceOf(50);
            specify(waitTime, should.equal(150, DELTA));
        }
    }
}
