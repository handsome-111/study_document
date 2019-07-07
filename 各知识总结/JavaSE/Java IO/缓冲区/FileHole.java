package buffer;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 
 * 文件空洞
 *
 */
public class FileHole {
	public static void main(String args[])throws Exception{
		File temp = File.createTempFile("holy", null);
		RandomAccessFile file = new RandomAccessFile(temp,"rw");
		FileChannel channel = file.getChannel();
		
		ByteBuffer buffer = ByteBuffer.allocateDirect(100);
		putData(0, buffer, channel);
		putData(5000000,buffer,channel);
		putData(50000,buffer,channel);
		
		System.out.println("Wrote temp file'" + temp.getPath() + "', size = " + channel.size());
		channel.close();
		file.close();
	}
	
	private static void putData(int position, ByteBuffer buffer,FileChannel channel)throws Exception{
		String string = "*<-- location " + position;
		buffer.clear();
		buffer.put(string.getBytes());
		buffer.flip();
		/**
		 * 将缓冲去里的数据写入通道
		 */
		channel.position(position);
		channel.write(buffer);
	}
}



