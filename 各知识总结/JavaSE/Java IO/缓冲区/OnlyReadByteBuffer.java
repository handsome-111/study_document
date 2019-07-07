package buffer;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class OnlyReadByteBuffer {
	public static void main(String args[]) throws Exception{
		FileInputStream file = new FileInputStream(new File("C:\\Users\\Administrator\\Desktop\\test.txt"));
		FileChannel channel = file.getChannel();
		
		ByteBuffer bb = ByteBuffer.allocateDirect(100);
		bb.put("哈哈哈哈".getBytes());
		//抛出异常，默认只读不能写
		channel.write(bb);
	}


}
