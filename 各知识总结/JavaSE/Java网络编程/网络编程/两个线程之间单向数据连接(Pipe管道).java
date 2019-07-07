package nio.socket;

import static org.hamcrest.CoreMatchers.startsWith;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SourceChannel;

public class PipeTest {
	private static Pipe pipe;
	static {
		try {
			pipe = Pipe.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void send() throws Exception {
		//获取发送数据的通道
		Pipe.SinkChannel sinkChannel = pipe.sink();
		/**
		 * 向缓冲区里存放数据
		 */
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.put("通过单向管道发送数据".getBytes());
		buffer.flip();
		/**
		 * 将缓冲区发送到管道中
		 */
		sinkChannel.write(buffer);
	}
	
	public void receive() throws Exception {
		/**
		 * 读取信息通道
		 */
		SourceChannel sourceChannel = pipe.source();
		/**
		 * 将数据读入缓冲区
		 */
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		sourceChannel.read(buffer);
		
		System.out.println(new String(buffer.array(),0,buffer.limit()));
	}
	public static void main(String[] args) throws Exception {
		PipeTest pt = new PipeTest();
		new Thread(() -> {
			try {
				System.out.println("睡5秒再发送数据");
				Thread.sleep(5000);
				pt.send();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
		new Thread(() -> {
			try {
				System.out.println("快带给我发数据啊");
				pt.receive();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
	}
}
