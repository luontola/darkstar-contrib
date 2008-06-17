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

package net.orfjackal.darkstar.integration;

import jdave.Block;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * @author Esko Luontola
 * @since 17.6.2008
 */
@RunWith(JDaveRunner.class)
public class TempDirectorySpec extends Specification<Object> {

    public class WhenATempDirectoryIsCreated {

        private TempDirectory tempDirectory;
        private File expectedDir1;
        private File expectedDir2;

        public Object create() {
            tempDirectory = new TempDirectory();
            expectedDir1 = new File(System.getProperty("java.io.tmpdir"), TempDirectory.class.getName() + ".1");
            expectedDir2 = new File(System.getProperty("java.io.tmpdir"), TempDirectory.class.getName() + ".2");
            return null;
        }

        public void destroy() {
            expectedDir1.delete();
            expectedDir2.delete();
        }

        public void atFirstTheDirectoryDoesNotExists() {
            specify(!expectedDir1.exists());
        }

        public void afterCreationTheDirectoryExists() {
            tempDirectory.create();
            specify(expectedDir1.exists());
            specify(tempDirectory.getDirectory(), should.equal(expectedDir1));
        }

        public void ifTheDirectoryAlreadyExistedADifferentNameIsUsed() {
            expectedDir1.mkdir();
            specify(expectedDir1.exists());
            specify(!expectedDir2.exists());
            tempDirectory.create();
            specify(expectedDir1.exists());
            specify(expectedDir2.exists());
            specify(tempDirectory.getDirectory(), should.equal(expectedDir2));
        }

        public void creatingTwiseIsNotAllowed() {
            tempDirectory.create();
            specify(new Block() {
                public void run() throws Throwable {
                    tempDirectory.create();
                }
            }, should.raise(IllegalStateException.class));
            specify(expectedDir1.exists());
            specify(!expectedDir2.exists());
        }
    }
}