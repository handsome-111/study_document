package nio.socket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;

import org.junit.Test;

public class NonBlockingNIO {
	@Test
	public void client() throws Exception {
		//获取通道
		SocketChannel sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",998));
		//配置非堵塞式通道
		sChannel.configureBlocking(false);
		//分配缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		//发送数据给服务器
		buffer.put(new Date().toString().getBytes());
		buffer.flip();
		sChannel.write(buffer);
		
		sChannel.close();
	}
	
	@Test
	public void server() throws Exception {
		//获取通道
		ServerSocketChannel ssChannel = ServerSocketChannel.open();
		//绑定端口
		ssChannel.bind(new InetSocketAddress(998));
		//绑定连接 
		ssChannel.configureBlocking(false);
		
		/**
		 * 开启一个选择器,并将“接收”选项的管道注册到选择器上
		 */
		Selector selector = Selector.open();
		ssChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		/**
		 * select()方法会堵塞，每当有一个选项或事件进入时，就会开始工作
		 */
		while(selector.select() > 0){
			//获取选择器里所有的键，并进行判断处理
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while(it.hasNext()){
				//取出“准备就绪”的事件
				SelectionKey sk = it.next();
				/**
				 * 以下是对准备好的事件进行处理
				 */
				if(sk.isAcceptable()){
					/**
					 * 获取客户端管道，并设置为非堵塞式,然后再将客户端通道注册到选择器上
					 */
					SocketChannel sChannel = ssChannel.accept();
					sChannel.configureBlocking(false);
					sChannel.register(selector, SelectionKey.OP_READ);
				}else if(sk.isReadable()){
					//获取“读就绪”状态的通道
					SocketChannel sChannel = (SocketChannel) sk.channel();
					sChannel.configureBlocking(false);
					
					ByteBuffer buf = ByteBuffer.allocate(1024);
					int len = 0;
					while((len = sChannel.read(buf)) > 0){
						buf.flip();
						System.out.println(new String(buf.array(),0,len));
						buf.clear();
					}
					//不管是读还是写，只要写一个相反的，就可以防止死循环
					sChannel.register(selector, SelectionKey.OP_WRITE);
				}
				//删除完成的事件
				it.remove();
			}
		}
	}
}




