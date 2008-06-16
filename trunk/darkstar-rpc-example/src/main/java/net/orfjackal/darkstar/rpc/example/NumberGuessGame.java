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

import java.util.Random;

/**
 * @author Esko Luontola
 * @since 16.6.2008
 */
public class NumberGuessGame {

    private static final int DEFAULT_MIN = 1;
    private static final int DEFAULT_MAX = 100;

    private final Random random;
    private int minimum = DEFAULT_MIN;
    private int maximum = DEFAULT_MAX;
    private int secret;
    private int tries;

    public NumberGuessGame() {
        this(new Random());
    }

    public NumberGuessGame(Random random) {
        this.random = random;
        newGame();
    }

    public int getMinimum() {
        return minimum;
    }

    public void setMinimum(int minimum) {
        this.minimum = minimum;
        newGame();
    }

    public int getMaximum() {
        return maximum;
    }

    public void setMaximum(int maximum) {
        this.maximum = maximum;
        newGame();
    }

    public void newGame() {
        secret = random.nextInt(maximum - minimum + 1) + minimum;
        tries = 0;
    }

    int secretNumber() {
        return secret;
    }

    public GuessResult guess(int guess) {
        tries++;
        if (guess < secret) {
            return GuessResult.TOO_LOW;
        } else if (guess > secret) {
            return GuessResult.TOO_HIGH;
        } else {
            return GuessResult.SUCCESS;
        }
    }

    public int tries() {
        return tries;
    }
}
