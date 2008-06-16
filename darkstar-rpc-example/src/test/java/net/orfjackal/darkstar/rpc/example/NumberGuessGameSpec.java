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

package net.orfjackal.darkstar.rpc.example;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Esko Luontola
 * @since 16.6.2008
 */
@RunWith(JDaveRunner.class)
public class NumberGuessGameSpec extends Specification<Object> {

    public class TheNumberToGuess {

        private static final int MIN = 10;
        private static final int MAX = 100;

        private NumberGuessGame game;

        public Object create() {
            game = new NumberGuessGame(new Random(2));
            game.setMinimum(MIN);
            game.setMaximum(MAX);
            return null;
        }

        public void changesWithEveryGame() {
            List<Integer> numbers = new ArrayList<Integer>();
            for (int i = 0; i < 10; i++) {
                game.start();
                int n = game.secretNumber();
                specify(numbers, should.not().contain(n));
                numbers.add(n);
            }
        }

        public void isAtLeastTheMinimum() {
            specify(game.getMinimum() == MIN);
            for (int i = 0; i < 100; i++) {
                game.start();
                specify(game.secretNumber() >= MIN);
            }
        }

        public void isAtMostTheMaximum() {
            specify(game.getMaximum() == MAX);
            for (int i = 0; i < 100; i++) {
                game.start();
                specify(game.secretNumber() <= MAX);
            }
        }

        public void maySometimesEqualTheMinimum() {
            List<Integer> numbers = new ArrayList<Integer>();
            for (int i = 0; i < 100; i++) {
                game.start();
                numbers.add(game.secretNumber());
            }
            specify(numbers, should.contain(MIN));
        }

        public void maySometimesEqualTheMaximum() {
            List<Integer> numbers = new ArrayList<Integer>();
            for (int i = 0; i < 100; i++) {
                game.start();
                numbers.add(game.secretNumber());
            }
            specify(numbers, should.contain(MAX));
        }
    }

    public class WhenThePlayerTriesToGuessTheNumber {

        private NumberGuessGame game;

        public Object create() {
            final Random random = mock(Random.class);
            checking(new Expectations() {{
                allowing(random).nextInt(100); will(returnValue(41));
            }});

            game = new NumberGuessGame(random);
            return null;
        }

    }
}
