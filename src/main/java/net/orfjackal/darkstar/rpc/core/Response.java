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

package net.orfjackal.darkstar.rpc.core;

import java.io.*;

/**
 * @author Esko Luontola
 * @since 9.6.2008
 */
public class Response {

    public final long requestId;
    public final Object value;
    public final Throwable exception;

    public static Response valueReturned(long requestId, Object value) {
        return new Response(requestId, value, null);
    }

    public static Response exceptionThrown(long requestId, Throwable exception) {
        return new Response(requestId, null, exception);
    }

    private Response(long requestId, Object value, Throwable exception) {
        this.requestId = requestId;
        this.value = value;
        this.exception = exception;
    }

    public byte[] toBytes() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bytes);
            out.writeLong(requestId);
            out.writeObject(value);
            out.writeObject(exception);
            out.close();
            return bytes.toByteArray();

        } catch (IOException e) {                   // should never happen
            throw new RuntimeException(e);
        }
    }

    public static Response fromBytes(byte[] message) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(message));
            long requestId = in.readLong();
            Object value = in.readObject();
            Throwable exception = (Throwable) in.readObject();
            in.close();
            return new Response(requestId, value, exception);

        } catch (IOException e) {                   // should never happen
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {        // may happen if there are classpath problems
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return "Response[" + requestId + "," + value + "," + exception + "]";
    }
}
