package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {
	public static void main(String[] args) throws Exception {
		/**
		 * 创建两个线程组
		 */
		//1.用于处理服务器接收客户端连接的	2.用户通信的（网络读写）
		EventLoopGroup pGroup = new NioEventLoopGroup();
		EventLoopGroup cGroup = new NioEventLoopGroup();
		/**
		 * 配置参数
		 */
		//创建辅助工具类，用于服务器通道的一系列配置
		ServerBootstrap b = new ServerBootstrap(); 
		b.group(pGroup, cGroup)
			.channel(NioServerSocketChannel.class)	//指定NIO模式
			.option(ChannelOption.SO_BACKLOG, 1024)		//设置缓冲区大小
			.option(ChannelOption.SO_SNDBUF,32*1024)	//设置发送缓冲区大小
			.option(ChannelOption.SO_RCVBUF,32*1024)	//设置接收缓冲区大小
			.option(ChannelOption.SO_KEEPALIVE, true)	//保持连接
			.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel channel) throws Exception {
					channel.pipeline().addLast(new ServerHandler());
				}
			});
		
		ChannelFuture cf = b.bind(998).sync();
		
	}
}





