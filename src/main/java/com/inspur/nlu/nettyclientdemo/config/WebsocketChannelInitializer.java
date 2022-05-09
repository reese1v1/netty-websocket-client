package com.inspur.nlu.nettyclientdemo.config;

import com.inspur.nlu.nettyclientdemo.handler.WebsocketClientHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;

public class WebsocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final WebsocketClientHandler handler;

    private static final String ORIGIN = "http://wsapi.xfyun.cn";

    public WebsocketChannelInitializer(WebsocketClientHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpClientCodec());
        p.addLast(new HttpObjectAggregator(8192));
        p.addLast(WebSocketClientCompressionHandler.INSTANCE);
        p.addLast(handler);

        CorsConfig corsConfig = CorsConfigBuilder.forOrigin(ORIGIN).forAnyOrigin().allowNullOrigin().allowCredentials().build();
        p.addLast(new CorsHandler(corsConfig));

    }
}