package netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ServerHandler extends ChannelInboundHandlerAdapter {
	public void channelRead(ChannelHandlerContext ctx,Object msg)throws Exception{
		ByteBuf buf = (ByteBuf) msg;
		System.out.println("长度:"+buf.readableBytes());
		//声明一个可读字节大小的的数组
		byte[] req = new byte[buf.readableBytes()];
		buf.readBytes(req);
		
		String message = new String(req,"utf-8");
		System.out.println("message :"+message);
		
		//读操作完之后需要释放，而写不需要
		ReferenceCountUtil.release(msg);
		
		//写并冲刷数据，
		//Unpooled.copiedBuffer 指的是将数组拷贝成缓冲区
		ctx.writeAndFlush(Unpooled.copiedBuffer("随便给你回复的一条信息".getBytes()));
	}
}
