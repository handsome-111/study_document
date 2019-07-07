package netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter{
	public void ChannelRead(ChannelHandlerContext ctx,Object msg) throws Exception {
		ByteBuf buffer = (ByteBuf) msg;
		
		byte[] message = new byte[buffer.readableBytes()];
		buffer.readBytes(message);
		String str = new String(message,"utf-8");
		System.out.println("客户端获取的消息:"+str);
		ReferenceCountUtil.release(msg);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("激活.....");
	}
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("读取完毕...");
		super.channelRead(ctx, msg);
	}
}
