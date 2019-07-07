package concurrency.waxomatic;

import java.util.concurrent.TimeUnit;

public class WaxOn implements Runnable{
	private Car car;
	public WaxOn(Car car){
		this.car = car;
	}
	@Override
	public void run() {
		try {
			while(!Thread.interrupted()){
				System.out.println("Wax On---上蜡");
				TimeUnit.MILLISECONDS.sleep(200);
				car.waxed();		//上蜡
				car.waitForBuffing();	//等待擦亮
			}
		} catch (Exception e) {
			System.out.println("Exiting via interrupt---通过中断退出");
		}
		System.out.println("Ending Wax On task --任务结束");
	}
}
