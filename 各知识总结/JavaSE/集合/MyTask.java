package container;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class MyTask implements Delayed{
	static BlockingQueue<MyTask> bq = new DelayQueue<>();
	long time;
	public MyTask(long time){
		this.time = time;
	}
	@Override
	public int compareTo(Delayed o) {
		if(this.getDelay(TimeUnit.MILLISECONDS) < o.getDelay(TimeUnit.MILLISECONDS)){
			return -1;
		}else if(this.getDelay(TimeUnit.MILLISECONDS) > o.getDelay(TimeUnit.MILLISECONDS)){
			return 1;
		}
		return 0;
	}
	@Override
	public long getDelay(TimeUnit unit) {
		//延迟的时间，单位毫秒     
		return 	unit.convert(time - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}
	public String toString(){
		return time+"";
	}
	public static void main(String[] args) {
		long now = System.currentTimeMillis();
		MyTask m1 = new MyTask(now + 4000);
		MyTask m2 = new MyTask(now + 5000);
		MyTask m3 = new MyTask(now + 6000);
		MyTask m4 = new MyTask(now + 7000);

		try {
			bq.put(m1);
			bq.put(m2);
			bq.put(m3);
			bq.put(m4);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(bq);
		for(int i = 0; i < bq.size(); i++){
			try {
				//当要取出的时候，会指定在某个时间后取出 
				System.out.println(bq.take());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
