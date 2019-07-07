package buffer;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class FileLockDemo implements Runnable{
	public static void main(String args[])throws Exception{
		File file = new File("C:\\Users\\Administrator\\Desktop\\test.txt");
		FileOutputStream out = new FileOutputStream(file);
		FileChannel channel = out.getChannel();
		FileLock lock = channel.tryLock();
		System.out.println(lock);
		new Thread(new FileLockDemo()).start();
		/**
		 * 将文件锁定，则在这20秒内其他线程是无法对文件进行读写操作的
		 */
		if(lock != null){
			System.out.println(file.getName()+"文件锁定20秒");
			Thread.sleep(1000);
			lock.release();
		}
	}

	@Override
	public void run() {
		for(int i =0;i < 10; i ++){
			System.out.println(i);
		}
	}
}
