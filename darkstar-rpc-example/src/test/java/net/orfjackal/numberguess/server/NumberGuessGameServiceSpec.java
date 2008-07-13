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

package net.orfjackal.numberguess.server;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.darkstar.exp.mocks.MockAppContext;
import net.orfjackal.numberguess.game.GuessResult;
import net.orfjackal.numberguess.game.NumberGuessGame;
import net.orfjackal.numberguess.game.NumberGuessGameImpl;
import net.orfjackal.numberguess.services.NumberGuessGameService;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

/**
 * @author Esko Luontola
 * @since 16.6.2008
 */
@RunWith(JDaveRunner.class)
public class NumberGuessGameServiceSpec extends Specification<Object> {

    public void create() throws Exception {
        MockAppContext.install();
    }

    public void destroy() throws Exception {
        MockAppContext.uninstall();
    }

    
    public class ANumberGuessGameService {

        private NumberGuessGame game;
        private NumberGuessGameService service;

        public Object create() {
            game = mock(NumberGuessGameImpl.class);
            service = new NumberGuessGameServiceImpl(game);
            return null;
        }

        public void forwardsGetMinimum() throws ExecutionException, InterruptedException {
            checking(new Expectations(){{
                one(game).getMinimum(); will(returnValue(1));
            }});
            specify(service.getMinimum().get(), should.equal(1));
        }

        public void forwardsSetMinimum() throws ExecutionException, InterruptedException {
            checking(new Expectations(){{
                one(game).setMinimum(1);
            }});
            service.setMinimum(1);
        }

        public void forwardsGetMaximum() throws ExecutionException, InterruptedException {
            checking(new Expectations(){{
                one(game).getMaximum(); will(returnValue(100));
            }});
            specify(service.getMaximum().get(), should.equal(100));
        }

        public void forwardsSetMaximum() throws ExecutionException, InterruptedException {
            checking(new Expectations(){{
                one(game).setMaximum(100);
            }});
            service.setMaximum(100);
        }

        public void forwardsNewGame() throws ExecutionException, InterruptedException {
            checking(new Expectations(){{
                one(game).newGame();
            }});
            service.newGame();
        }

        public void forwardsGuess() throws ExecutionException, InterruptedException {
            checking(new Expectations(){{
                one(game).guess(42); will(returnValue(GuessResult.SUCCESS));
            }});
            specify(service.guess(42).get(), should.equal(GuessResult.SUCCESS));
        }

        public void forwardsTries() throws ExecutionException, InterruptedException {
            checking(new Expectations(){{
                one(game).tries(); will(returnValue(5));
            }});
            specify(service.tries().get(), should.equal(5));
        }
    }
}
