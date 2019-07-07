package nio.socket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Scanner;

import org.junit.Test;

public class NonBlockingNIO2 {
	@Test
	public void send() throws Exception {
		/**
		 * 获取数据包通道，并设置其通道为非堵塞式
		 */
		DatagramChannel dc = DatagramChannel.open();
		dc.configureBlocking(false);
		
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		
		/**
		 * 发送数据到指定IP
		 */
		Scanner scan = new Scanner(System.in);
		while(scan.hasNext()){
			String str = scan.nextLine();
			buffer.put(str.getBytes());
			buffer.flip();
			dc.send(buffer, new InetSocketAddress("127.0.0.1",998));
			buffer.clear();
		}
		dc.close();
	}
	
	@Test
	public void receive() throws Exception {
		/**
		 * 声明数据包通道，并设置为非堵塞式，绑定本地IP
		 */
		DatagramChannel dc = DatagramChannel.open();
		dc.configureBlocking(false);
		dc.bind(new InetSocketAddress(998));
		
		/**
		 * 将通道注册到选择器中
		 */
		Selector selector = Selector.open();
		dc.register(selector, SelectionKey.OP_READ);
		/**
		 * 选择器对任务进行处理
		 */
		while(selector.select() > 0){
			Iterator<SelectionKey> it = selector.selectedKeys().iterator(); 
			while(it.hasNext()){
				SelectionKey key = it.next();
				if(key.isReadable()){
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					
					dc.receive(buffer);
					System.out.println(new String(buffer.array(),0,buffer.limit()));
					buffer.clear();
				}
				it.remove();
			}
		}
	}
}
