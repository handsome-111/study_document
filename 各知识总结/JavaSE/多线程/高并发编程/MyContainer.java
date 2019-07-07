package ReentrantLock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyContainer {
	private volatile List<Object> list = new ArrayList<Object>();
	private static final int MAX = 10;
	private Lock lock = new ReentrantLock();
	private Condition producer = lock.newCondition();
	private Condition consumer = lock.newCondition();
	int count = 0;
	
	public void m1(){
		while(true){
			//一直生产
			put();
		}
	}
	
	public void m2(){
		while(true){
			//一直消费
			get();
		}
	}
	
	public void put(){
		lock.lock();
		try{
			while(list.size() == MAX){
				try {
					producer.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			count ++;
			list.add(count);	
			System.out.println("添加对象："+count);
			consumer.signalAll();	//唤醒正在睡觉的消费者
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
	}
	public void get(){
		lock.lock();
		try{
			while(list.size() == 0){
				try {
					consumer.await();	//消费者睡觉
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Object o = list.remove(0);
			System.out.println(o);
			producer.signalAll();	//唤醒生产者
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
	}
	public static void main(String[] args) {
		MyContainer mc = new MyContainer();
		for(int i = 0; i < 10; i++){
			new Thread(mc::m1).start();
		}
		for(int i = 0; i < 2; i++){
			new Thread(mc::m2).start();
		}
	}
}
