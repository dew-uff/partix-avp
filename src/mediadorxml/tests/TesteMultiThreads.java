package mediadorxml.tests;

public class TesteMultiThreads implements Runnable {
	
	private int id;
	private long sleepTime;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Starting threads");
		
		Thread t1 = new Thread(new TesteMultiThreads(1, 5000));
		Thread t2 = new Thread(new TesteMultiThreads(2, 1000));
		Thread t3 = new Thread(new TesteMultiThreads(3, 2000));
		
		t1.start();
		t2.start();
		t3.start();
		
		try{
			t1.join();
			t2.join();
			t3.join();
			
			System.out.println("Threads completed execution");
		}
		catch(InterruptedException iex){
			System.out.println("Join threads not possible");
		}		
	}
	
	public TesteMultiThreads(int id, long sleepTime){
		this.id = id;
		this.sleepTime = sleepTime;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	public void run(){
		try{
			System.out.println("Hola from Thread " + id);
			Thread.sleep(this.getSleepTime());
			System.out.println("Thread " + id + " finished sleep");
		}
		catch (InterruptedException iex){
			System.out.println("Thread exception (id: " + id + ")");
		}
	}

}
