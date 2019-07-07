package buffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class ChannelCopy {
	public static void main(String args[]) throws Exception{
		FileInputStream fin = new FileInputStream(new File("E:\\迅雷下载\\B5EE0F42B854EA05362341B1B34A017D.mp4"));
		FileChannel src = fin.getChannel();
		
		FileOutputStream fout = new FileOutputStream(new File("C:\\Users\\Administrator\\Desktop\\我的视频.mp4"));
		FileChannel dest = fout.getChannel();
		
		long time1 = System.currentTimeMillis();
		channelCopy2(src,dest);
		long time2 = System.currentTimeMillis();
		System.out.println("复制时间："+(time2-time1));
		System.out.println("复制完成");
	}
	private static void channelCopy1(ReadableByteChannel src ,WritableByteChannel dest)throws Exception{
		ByteBuffer buffer = ByteBuffer.allocateDirect(16*1024);
		while(src.read(buffer) != -1){
			buffer.flip();
			dest.write(buffer);
			buffer.compact();
		}
		buffer.flip();
		while(buffer.hasRemaining()){
			dest.write(buffer);
		}
	}

	private static void channelCopy2(ReadableByteChannel src,WritableByteChannel dest) throws IOException{
		ByteBuffer buffer = ByteBuffer.allocateDirect(16*1024);
		int i = 0;
		while(src.read(buffer) != -1){
			buffer.flip();
			while(buffer.hasRemaining()){
				System.out.println(i++);
				dest.write(buffer);
			}
			buffer.clear();
		}
	}



}
