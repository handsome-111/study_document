package ReentrantLock;

import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TicketSeller {
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
					//lock.lock();
					while(TicketSeller.tickets.size() > 0){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String ticket = TicketSeller.tickets.remove(0);
						System.out.println(ticket);	
					}
					//lock.unlock();
				}
			}).start();
		}
	}
}
