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

package net.orfjackal.numberguess.client;

import net.orfjackal.numberguess.game.GuessResult;
import net.orfjackal.numberguess.services.NumberGuessGameService;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * @author Esko Luontola
 * @since 16.6.2008
 */
public class GameMain implements Runnable {

    private static final Scanner in = new Scanner(System.in);

    private final String host;
    private final String port;

    private NumberGuessGameService game;

    public GameMain(String host, String port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        System.out.print("Enter your name: ");
        String username = in.nextLine();

        GameClientListener client = new GameClientListener(username);
        if (client.login(host, port)) {
            waitUntilLoggedIn(client);
            game = client.getNumberGuessGame();
            try {
                gameLoop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            client.logout();
        }
    }

    private void waitUntilLoggedIn(GameClientListener client) {
        try {
            while (!client.isConnected()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void gameLoop() throws ExecutionException, InterruptedException {
        Integer min = game.getMinimum().get();
        Integer max = game.getMaximum().get();
        boolean tryAgain;
        do {
            System.out.println();
            System.out.println("*** Starting a new game ***");
            System.out.println();

            game.newGame();

            System.out.print("Guess a number between " + min + " and " + max + ": ");

            guessLoop();

            System.out.println();
            System.out.print("Do you want to play again (Y/N)? ");
            tryAgain = in.nextLine().toUpperCase().startsWith("Y");
        } while (tryAgain);

        System.out.println("Thank you for playing.");
    }

    private void guessLoop() throws InterruptedException, ExecutionException {
        GuessResult result;
        do {
            int guess = in.nextInt();
            result = game.guess(guess).get();

            switch (result) {
                case SUCCESS:
                    Integer tries = game.tries().get();
                    System.out.println("Great! You guessed right with " + tries + " tries.");
                    break;
                case TOO_HIGH:
                    System.out.print("Too high. Guess again: ");
                    break;
                case TOO_LOW:
                    System.out.print("Too low. Guess again: ");
                    break;
            }
        } while (result != GuessResult.SUCCESS);
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            args = new String[]{
                    "localhost", "1139"
            };
        }
        String host = args[0];
        String port = args[1];
        new GameMain(host, port).run();
    }
}
