package ReentrantLock;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**
 * 采用高并发链表队列实现的卖票高并发
 * @author Administrator
 *
 */
public class ConcurrentLinkQueue {
	private static Queue<String> tickets = new ConcurrentLinkedDeque<String>();
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
						//因为vector是具有原子性的，所以先取出来在判断，如果出现异常则直接不卖了
						str = ConcurrentLinkQueue.tickets.poll();
						if(str == null){
							break;
						}
						System.out.println(str);
					}
				}
			}).start();
		}
	}
}
