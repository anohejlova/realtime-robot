package cz.muni.fi.IA158.robot.tasks.runnable;

import java.util.concurrent.BlockingQueue;

public class SteeringRunnable implements Runnable{

	boolean suspended = false;
	BlockingQueue<Job> queueSteer;
	long releaseDeadlineDiff = 100;
	
	public SteeringRunnable(BlockingQueue<Job> queueSteer) {
		this.queueSteer = queueSteer;
	}
	
	@Override
	public void run() {
		try{
			long now = System.currentTimeMillis(); // number of milliseconds from start of the epoch
			Job release = new Job(now, now + releaseDeadlineDiff);
			queueSteer.add(release);
			System.err.println("steering");
		    suspend();
		     
		    synchronized(this) {
		    	while(suspended) {
		    		wait();
		    	}
		    }
		       
		       //distance check code
	    } catch (InterruptedException e) {
	       System.out.println("steerThread interrupted.");
	    }
	    System.out.println("steerThread exiting.");
	}
	
	
	void suspend() {
		suspended = true;
	}
	   
	synchronized void resume() {
		suspended = false;
	    notify();
	}

}
