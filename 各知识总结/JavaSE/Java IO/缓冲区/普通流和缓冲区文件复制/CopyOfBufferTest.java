package buffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class CopyOfBufferTest {
	public static void main(String args[])throws Exception{
		File file = new File("C:\\Users\\Administrator\\Desktop\\itoolssetup_4.2.7.1.exe");
		File file2 = new File("C:\\Users\\Administrator\\Desktop\\itools2.exe");
		
		FileInputStream input = null;
		FileOutputStream out = null;
		
		input = new FileInputStream(file);
		out = new FileOutputStream(file2);
		
		FileChannel fin = input.getChannel();
		FileChannel fout = out.getChannel();
		
		ByteBuffer buf = ByteBuffer.allocate(20);
		int temp = 0;
		long time1 = System.currentTimeMillis();
		while((temp = fin.read(buf)) != -1){
			buf.flip();
			fout.write(buf);
			buf.clear();
		}
		long time2 = System.currentTimeMillis();
		System.out.println("所用的时间为"+(time2-time1));
		fin.close();
		fout.close();
		input.close();
		out.close();
	}
}
