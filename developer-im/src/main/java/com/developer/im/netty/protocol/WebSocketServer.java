package com.developer.im.netty.protocol;

import com.developer.im.netty.protocol.codec.WSDecoder;
import com.developer.im.netty.protocol.codec.WSEncoder;
import com.developer.im.netty.service.IMChannelHandler;
import com.developer.im.netty.service.IMServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WebSocketServer implements IMServer {

    /**
     * 服务是否准备就绪
     */
    private volatile boolean ready = false;

    /**
     * 启动端口
     */
    @Value("${websocket.port}")
    private Integer port;

    private ServerBootstrap bootstrap;
    private EventLoopGroup boosGroup;
    private EventLoopGroup workGroup;

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public void start() {
        bootstrap = new ServerBootstrap();
        boosGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();

        bootstrap.group(boosGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new IdleStateHandler(300,0,0, TimeUnit.SECONDS));
                        pipeline.addLast("http-codec", new HttpServerCodec());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(65535));
                        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                        pipeline.addLast(new WebSocketServerProtocolHandler("/im"));
                        pipeline.addLast("encode",new WSEncoder());
                        pipeline.addLast("decode",new WSDecoder());
                        pipeline.addLast("handler", new IMChannelHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG,5)
                .childOption(ChannelOption.SO_KEEPALIVE,true);

        try {
            // 绑定端口，启动select线程，轮询监听channel事件，监听到事件之后就会交给从线程池处理
            Channel channel = bootstrap.bind(port).sync().channel();
            // 就绪标志
            this.ready = true;
            log.info("websocket server 初始化完成,端口：{}",port);
            // 等待服务端口关闭
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.info("websocket server 初始化异常",e);
        }
    }

    @Override
    public void stop() {
        if(boosGroup!=null && !boosGroup.isShuttingDown() && !boosGroup.isShutdown()){
            boosGroup.shutdownGracefully();
        }

        if(workGroup!=null && !workGroup.isShuttingDown() && !workGroup.isShutdown()){
            workGroup.shutdownGracefully();
        }

        this.ready=false;
        log.info("websocket 服务停止");
    }
}
