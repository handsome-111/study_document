package buffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class CopyOfStreamTest {
	public static void main(String args[])throws Exception{
		ByteBuffer buf = null;
		File file = new File("C:\\Users\\Administrator\\Desktop\\itoolssetup_4.2.7.1.exe");
		File file2 = new File("C:\\Users\\Administrator\\Desktop\\itools.exe");
		int length = (int) file.length();
		System.out.println(length);
		buf = ByteBuffer.allocate((int) file.length());
		
		OutputStream out = new FileOutputStream(file2);
		InputStream in = new FileInputStream(file);
		
		Timer t =new Timer();
		TimerTask task = new TimerTask(){
			int i = 0;
			public void run() {
				System.out.println("当前时间"+(i*10)+"秒");
				i++;
			}
			
		};
		t.schedule(task, 0,10000);
		
		int temp = 0;
		long time1 = System.currentTimeMillis();
		while((temp = in.read()) != -1){
			out.write(temp);
		}
		long time2 = System.currentTimeMillis();
		System.out.println(time2-time1);
	}
}




