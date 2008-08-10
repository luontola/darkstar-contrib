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

package net.orfjackal.darkstar.rpc.core.futures;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.util.ScalableHashMap;
import net.orfjackal.darkstar.rpc.core.Request;
import net.orfjackal.darkstar.rpc.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.Future;

/**
 * @author Esko Luontola
 * @since 10.8.2008
 */
public class ServerFutureManager implements FutureManager, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ServerFutureManager.class);

    private final ManagedReference<ScalableHashMap<Long, ServerFuture<?>>> waitingForResponse;

    public ServerFutureManager() {
        waitingForResponse = AppContext.getDataManager().createReference(new ScalableHashMap<Long, ServerFuture<?>>());
    }

    public <V> Future<V> waitForResponseTo(Request request) {
        ServerFuture<V> f = new ServerFuture<V>(request, this);
        assert !waitingForResponse.get().containsKey(request.requestId);
        waitingForResponse.get().put(request.requestId, f);
        return f;
    }

    public void recievedResponse(Response response) {
        ServerFuture<?> f = waitingForResponse.get().remove(response.requestId);
        if (f != null) {
            f.markDone(response);
        } else {
            logger.warn("Unexpected response: {}", response);
        }
    }

    public int waitingForResponse() {
        return waitingForResponse.get().size();
    }

    protected void doNotWaitForResponse(Request request) {
        waitingForResponse.get().remove(request.requestId);
    }
}
