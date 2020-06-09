/*
 * Decompiled with CFR 0.145.
 * 
 * Could not load the following classes:
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelFuture
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelInboundHandlerAdapter
 *  io.netty.channel.EventLoop
 *  io.netty.handler.timeout.ReadTimeoutException
 *  io.netty.util.concurrent.EventExecutor
 *  io.netty.util.concurrent.ScheduledFuture
 */
package com.mongodb.connection.netty;

import com.mongodb.assertions.Assertions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.concurrent.EventExecutor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

final class ReadTimeoutHandler
extends ChannelInboundHandlerAdapter {
    private final long readTimeout;
    private volatile ScheduledFuture<?> timeout;

    ReadTimeoutHandler(long readTimeout) {
        Assertions.isTrueArgument("readTimeout must be greater than zero.", readTimeout > 0L);
        this.readTimeout = readTimeout;
    }

    void scheduleTimeout(ChannelHandlerContext ctx) {
        Assertions.isTrue("Handler called from the eventLoop", ctx.channel().eventLoop().inEventLoop());
        if (this.timeout == null) {
            this.timeout = ctx.executor().schedule((Runnable)new ReadTimeoutTask(ctx), this.readTimeout, TimeUnit.MILLISECONDS);
        }
    }

    void removeTimeout(ChannelHandlerContext ctx) {
        Assertions.isTrue("Handler called from the eventLoop", ctx.channel().eventLoop().inEventLoop());
        if (this.timeout != null) {
            this.timeout.cancel(false);
            this.timeout = null;
        }
    }

    private static final class ReadTimeoutTask
    implements Runnable {
        private final ChannelHandlerContext ctx;

        ReadTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (this.ctx.channel().isOpen()) {
                try {
                    this.ctx.fireExceptionCaught((Throwable)ReadTimeoutException.INSTANCE);
                    this.ctx.close();
                }
                catch (Throwable t) {
                    this.ctx.fireExceptionCaught(t);
                }
            }
        }
    }

}

