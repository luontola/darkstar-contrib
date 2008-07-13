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
import java.util.Arrays;

/**
 * @author Esko Luontola
 * @since 9.6.2008
 */
public class Request {

    public final long requestId;
    public final long serviceId;
    public final String methodName;
    public final Class<?>[] paramTypes;
    public final Object[] parameters;

    public Request(long requestId, long serviceId, String methodName, Class<?>[] paramTypes, Object[] parameters) {
        this.requestId = requestId;
        this.serviceId = serviceId;
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.parameters = parameters;
    }

    public byte[] toBytes() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bytes);
            out.writeLong(requestId);
            out.writeLong(serviceId);
            out.writeUTF(methodName);
            out.writeObject(paramTypes);
            out.writeObject(parameters);
            out.close();
            return bytes.toByteArray();

        } catch (IOException e) {                   // should never happen
            throw new RuntimeException(e);
        }
    }

    public static Request fromBytes(byte[] message) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(message));
            long requestId = in.readLong();
            long serviceId = in.readLong();
            String methodName = in.readUTF();
            Class<?>[] paramTypes = (Class<?>[]) in.readObject();
            Object[] parameters = (Object[]) in.readObject();
            in.close();
            return new Request(requestId, serviceId, methodName, paramTypes, parameters);

        } catch (IOException e) {                   // should never happen
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {        // may happen if there are classpath problems
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return "Request[" + requestId + "," + serviceId + "," + methodName +
                "," + Arrays.toString(paramTypes) + "," + Arrays.toString(parameters) + "]";
    }
}
