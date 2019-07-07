package buffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class CopyOfMappedByteTest {
	public static void main(String args[])throws Exception{
		File file = new File("E:\\迅雷下载\\E95E78914A8B0BC57C0601D5FC58A84B.mp4");
		File file2 = new File("C:\\Users\\Administrator\\Desktop\\电影.mp4");
		
		FileChannel fout = new FileOutputStream(file2).getChannel();
		FileChannel fin = new RandomAccessFile(file,"rw").getChannel();
		
		int size = 1024*1024;	
		long length = file.length();
		int begin = 0;
		int page = (int) (length%size == 0 ? length/size : length/size+1);
		
		long time1 = System.currentTimeMillis();
		
		for(int i = 1; i <= page; i++){
			if(i == page){
				size = (int) (length%size);
			}
			//将内容填入内存映射缓冲区
			MappedByteBuffer mbb = fin.map(FileChannel.MapMode.READ_ONLY, begin,size);
			//将内容从缓冲区输出到通道中
			fout.write(mbb);
			begin = i*size;
			System.out.println(begin);
		}
		
		long time2 = System.currentTimeMillis();
		
		System.out.println(time2-time1);
		
		fin.close();
		fout.close();
	}
	
}
