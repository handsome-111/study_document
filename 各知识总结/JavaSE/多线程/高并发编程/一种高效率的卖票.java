package ReentrantLock;

import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**
 * 一种高效率的卖票
 * @author Administrator
 *
 */
public class ConcurrentLinkQueue {
	private static Vector<String> tickets = new Vector<String>();
	static {
		for(int i= 100; i > 0;i--){
			tickets.add("票:"+i);
		}
	}
	public static void main(String[] args) {
		Lock lock = new ReentrantLock();
		for(int i = 0; i < 10; i++){
			new Thread(new Runnable(){
				@Override
				public void run() {
					while(true){
						String str = null;
						try {
							//因为vector是具有原子性的，所以先取出来在判断，如果出现异常则直接不卖了
							str = ConcurrentLinkQueue.tickets.remove(0);
							System.out.println(str);
						} catch (Exception e) {
							break;
						}
					}
				}
			}).start();
		}
	}
}
