package concurrency.waxomatic;

public class Car {
	private boolean waxOn = false;
	public synchronized void waxed(){
		waxOn = true;
		notifyAll();
	}
	public synchronized void buffed(){
		waxOn = false;
		notifyAll();
	}
	public synchronized void waitForWaxing() throws InterruptedException {
		while(waxOn == false){
			wait();
		}
	}
	
	public synchronized void waitForBuffing()  throws InterruptedException {
		while(waxOn == true){
			wait();
		}
	}
}
