package com.inspur.nlu.nettyclientdemo.client;

import com.inspur.nlu.nettyclientdemo.Exception.MyException;
import com.inspur.nlu.nettyclientdemo.config.WebsocketChannelInitializer;
import com.inspur.nlu.nettyclientdemo.handler.WebsocketClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class WebsocketClient extends AbstractWebsocketClient {
    private static final Logger log = LoggerFactory.getLogger(WebsocketClient.class);

    private static final NioEventLoopGroup NIO_GROUP = new NioEventLoopGroup();

    private final URI uri;

    private final int port;

    private Bootstrap bootstrap;

    private WebsocketClientHandler handler;

    private Channel channel;

    public WebsocketClient(String url, int connectionTimeout) throws URISyntaxException, MyException {
        super(connectionTimeout);
        this.uri = new URI(url);
        this.port = getPort();
    }

    public WebsocketClient(URI uri, int connectionTimeout) throws URISyntaxException, MyException {
        super(connectionTimeout);
        this.uri = uri;
        this.port = getPort();
    }

    /**
     * Extract the specified port
     *
     * @return the specified port or the default port for the specific scheme
     */
    private int getPort() throws MyException {
        int port = uri.getPort();
        if (port == -1) {
            String scheme = uri.getScheme();
            if ("wss".equals(scheme)) {
                return 443;
            } else if ("ws".equals(scheme)) {
                return 80;
            } else {
                throw new MyException("unknown scheme: " + scheme);
            }
        }
        return port;
    }

    @Override
    protected void doOpen() {
        // websocket??????????????????????????????
        WebSocketClientHandshaker webSocketClientHandshaker = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());
        // ???????????????
        handler = new WebsocketClientHandler(webSocketClientHandshaker, this.websocketContext);
        // client????????????client channel??????
        bootstrap = new Bootstrap();
        // ???????????? ???????????? ??????????????????
        bootstrap.group(NIO_GROUP).channel(NioSocketChannel.class).handler(new WebsocketChannelInitializer(handler));
    }

    @Override
    protected void doConnect() {
        try {
            // ????????????
            channel = bootstrap.connect(uri.getHost(), port).sync().channel();
            // ??????????????????
            handler.handshakeFuture().sync();
        } catch (InterruptedException e) {
            log.error("websocket??????????????????", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected Channel getChannel() {
        return channel;
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.close();
        }
    }

    public boolean isOpen() {
        return channel.isOpen();
    }

}
