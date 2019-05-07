package cz.muni.fi.IA158.robot.tasks.runnable;

import java.util.concurrent.BlockingQueue;

public class DistanceCheckRunnable implements Runnable{
	
	boolean suspended = false;
	private BlockingQueue<Job> queueDist;
	long releaseDeadlineDiff = 200;
	
	public DistanceCheckRunnable(BlockingQueue<Job> queueDist) {
		this.queueDist = queueDist;
	}

	@Override
	public void run() {
	
		try{
			long now = System.currentTimeMillis(); // number of milliseconds from start of the epoch
			Job release = new Job(now, now + releaseDeadlineDiff);
			queueDist.add(release);
			System.err.println("distance");
		    suspend();
		     
		    synchronized(this) {
		    	while(suspended) {
		    		wait();
		    	}
		    }
		       
		       //distance check code
	    } catch (InterruptedException e) {
	       System.out.println("distThread interrupted.");
	    }
	    System.out.println("distThread exiting.");
	}
	
	
	void suspend() {
		suspended = true;
	}
	   
	synchronized void resume() {
		suspended = false;
	    notify();
	}

}
