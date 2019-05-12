package cz.muni.fi.IA158.robot.tasks.runnable;

import java.util.concurrent.BlockingQueue;

public class SteeringRunnable implements Runnable{

	public Thread thread;
	boolean suspended = false;
	BlockingQueue<Job> queueSteer;
	long releaseDeadlineDiff = 100;
	
	public SteeringRunnable(BlockingQueue<Job> queueSteer) {
		this.queueSteer = queueSteer;
	}
	
	@Override
	public void run() {
		try{
			while(true) {
				System.out.println("Steer");
				long now = System.currentTimeMillis(); // number of milliseconds from start of the epoch
				Job release = new Job(now, now + releaseDeadlineDiff);
				queueSteer.add(release);				
			    suspend();
			     
			    synchronized(this) {
			    	while(suspended) {
			    		wait();
			    	}
			    }
			    
			  //steering code
			}   		       
		 } catch (InterruptedException e) {
		    System.out.println("steerThread interrupted.");
		 }
		 System.out.println("steerThread exiting.");
	}
	
	public void start () {
		if (thread == null) {
	    	thread = new Thread (this);
	    	thread.setPriority(thread.getPriority() + 1);
	    	thread.setPriority(10);
	    	thread.start ();
		}
	}
	
	private void suspend() {
		suspended = true;
	}
	   
	public synchronized void resume() {
		suspended = false;
	    notify();
	}

}
