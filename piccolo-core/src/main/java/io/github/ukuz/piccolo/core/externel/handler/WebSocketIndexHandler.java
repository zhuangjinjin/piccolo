/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.github.ukuz.piccolo.core.externel.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import java.io.IOException;
import java.io.InputStream;

/**
 * Copied from netty
 */
public class WebSocketIndexHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketIndexHandler.class);
    private static final String ROOT_PATH = "/";
    private static final String INDEX = "/index.html";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        if (!HttpMethod.GET.equals(req.method())) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        if (ROOT_PATH.equals(req.uri()) || INDEX.equals(req.uri())) {
            ByteBuf content = getContent(INDEX);
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

            res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");

            HttpUtil.setContentLength(res, res.content().readableBytes());

            sendHttpResponse(ctx, req, res);
        } else {

            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
        }
    }

    private ByteBuf getContent(String fileName) {
        try (InputStream is = this.getClass().getResourceAsStream(fileName)){
            byte[] data = new byte[is.available()];
            if (is.read(data) > 0) {
                return Unpooled.wrappedBuffer(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Unpooled.EMPTY_BUFFER;
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        if (res.status().code() != HttpResponseStatus.OK.code()) {
            LOGGER.error("req error, uri: {} status: {}", req.uri(), res.status().code());
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }

        if (!HttpUtil.isKeepAlive(res) || res.status().code() != OK.code()) {
            res.headers().set(CONNECTION, CLOSE);
            ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
        } else {
            if (req.protocolVersion().equals(HTTP_1_0)) {
                res.headers().set(CONNECTION, KEEP_ALIVE);
            }
            ctx.writeAndFlush(res);
        }

    }

}
