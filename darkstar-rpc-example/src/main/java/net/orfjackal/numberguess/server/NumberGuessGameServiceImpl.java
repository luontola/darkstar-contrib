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

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import net.orfjackal.darkstar.rpc.ServiceHelper;
import net.orfjackal.numberguess.game.GuessResult;
import net.orfjackal.numberguess.game.NumberGuessGame;
import net.orfjackal.numberguess.services.NumberGuessGameService;

import java.io.Serializable;
import java.util.concurrent.Future;

/**
 * @author Esko Luontola
 * @since 16.6.2008
 */
public class NumberGuessGameServiceImpl implements NumberGuessGameService, Serializable {
    private static final long serialVersionUID = 1L;

    private final ManagedReference<NumberGuessGame> game;

    public NumberGuessGameServiceImpl(NumberGuessGame game) {
        this.game = AppContext.getDataManager().createReference(game);
    }

    public Future<Integer> getMinimum() {
        return ServiceHelper.wrap(game.get().getMinimum());
    }

    public void setMinimum(int minimum) {
        game.get().setMinimum(minimum);
    }

    public Future<Integer> getMaximum() {
        return ServiceHelper.wrap(game.get().getMaximum());
    }

    public void setMaximum(int maximum) {
        game.get().setMaximum(maximum);
    }

    public void newGame() {
        game.get().newGame();
    }

    public Future<GuessResult> guess(int guess) {
        return ServiceHelper.wrap(game.get().guess(guess));
    }

    public Future<Integer> tries() {
        return ServiceHelper.wrap(game.get().tries());
    }
}
