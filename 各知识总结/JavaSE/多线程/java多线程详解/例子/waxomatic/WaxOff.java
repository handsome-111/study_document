package concurrency.waxomatic;

import java.util.concurrent.TimeUnit;

public class WaxOff implements Runnable{
	private Car car;
	public WaxOff(Car car){
		this.car = car;
	}
	@Override
	public void run() {
		try {
			while(!Thread.interrupted()){
				car.waitForWaxing();     		//等待上蜡
				System.out.println("Wax Off--擦亮");	//擦亮
				TimeUnit.MILLISECONDS.sleep(200);
				car.buffed();     				//擦亮
			}
		} catch (Exception e) {
			System.out.println("Exiting via interrupt");
		}
		System.out.println("Ending Wax Off task--任务结束");
	}
}
