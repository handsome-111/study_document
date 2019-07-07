package buffer;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelDemo01 {
	public static void main(String args[])throws Exception {
		String info[] = {"aa","bb","cc","dd","ee","ff"};
		FileOutputStream out = new FileOutputStream(new File("C:\\Users\\Administrator\\Desktop\\test.txt"));
		//文件通道
		FileChannel fout = out.getChannel();
		ByteBuffer buf = ByteBuffer.allocate(1024);
		for(int i = 0; i < info.length; i ++){
			buf.put(info[i].getBytes());
		}
		//重置缓冲区，将指针恢复至0，否则 指针在后面就无法写入数据
		buf.flip();
		fout.write(buf);
		
		//再写更多
		buf.position(2);
		fout.write(buf);
		//关闭通道
		fout.close();
		out.close();
	}
}


