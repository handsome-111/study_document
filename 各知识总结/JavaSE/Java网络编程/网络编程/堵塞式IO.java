package nio.socket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;
/**
 * 堵塞式NIO
 * @author Administrator
 *
 */
public class TestBlocking {
	@Test
	public void client() throws Exception {
		//1.获取通道
		SocketChannel channel = SocketChannel.open(new InetSocketAddress("127.0.0.1",998));
		FileChannel inchannel = FileChannel.open(Paths.get("视频.mp4"), StandardOpenOption.READ);
		//2.分配缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		//3.读取本地文件，发送到服务器
		while(inchannel.read(buffer) != -1){
			buffer.flip();
			channel.write(buffer);
			buffer.clear();
		}
		inchannel.close();
		channel.close();
	}
	@Test
	public void server() throws Exception {
		//1.获取通道
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		FileChannel fc = FileChannel.open(Paths.get("server.mp4"), StandardOpenOption.WRITE,StandardOpenOption.CREATE);
		//2.绑定连接
		serverChannel.bind(new InetSocketAddress(998));
		//3.获取客户端的通道
		SocketChannel channel = serverChannel.accept();
		//4.分配缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		//5.接收客户端数据并保存到本地
		while(channel.read(buffer) != -1){
			buffer.flip();
			fc.write(buffer);
			buffer.clear();
		}
		//6.关闭通道
		channel.close();
		serverChannel.close();
		fc.close();
	}
}
