package cz.muni.fi.IA158.robot.tasks.runnable;

import java.util.concurrent.BlockingQueue;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3IRSensor;

public class DistanceCheckRunnable implements Runnable{
	
	public Thread thread;
	boolean suspended = false;
	private BlockingQueue<Job> queueDist;
	long releaseDeadlineDiff = 200;
	private int lastDistance = 0;
	private int newDistance;
	private EV3IRSensor ir; 
	private EV3LargeRegulatedMotor powerMotor;
	
	public DistanceCheckRunnable(BlockingQueue<Job> queueDist, EV3LargeRegulatedMotor powerMotor, EV3IRSensor ir) {
		this.queueDist = queueDist;
		this.ir = ir;
		this.powerMotor = powerMotor;
		
		if (this.ir == null) {
			throw new java.lang.IllegalArgumentException("no IR Sensor initialized in distance task");
		}
		if (this.powerMotor == null) {
			throw new java.lang.IllegalArgumentException("no large motor initializedin distance task");
		}
		
	}

	@Override
	public void run() {
	
		try{
			while(true) {
				System.out.println("Dist");
				long now = System.currentTimeMillis(); // number of milliseconds from start of the epoch
				Job release = new Job(now, now + releaseDeadlineDiff);
				queueDist.add(release);			
			    suspend();
			    synchronized(this) {
			    	while(suspended) {
			    		wait();
			    	}		    	
			    }
			    
			  /*if (checkDistance()) {
				  adaptSpeed();
			  }*/
			}  
	    } catch (InterruptedException e) {
	       System.out.println("distThread interrupted.");
	    }
	    System.out.println("distThread exiting.");
	}
	
	private boolean checkDistance() {
		
		lastDistance = newDistance;
		newDistance = ir.getDistanceMode().sampleSize();
		int diff = java.lang.Math.abs(newDistance - lastDistance);
		
		return ((diff > 10) || (newDistance < 50));
	      
	}
	
	private void adaptSpeed() {
		
		if (newDistance <= 20) {
			powerMotor.stop();
			return;
		} 
		
		int diff = newDistance - lastDistance;
		double curSpeed = powerMotor.getSpeed();
					
		if (diff <= -60) {
			powerMotor.setSpeed((int) Math.round(0.2 *curSpeed));
		} else if (diff <= -40) {
			powerMotor.setSpeed((int) Math.round(0.4 *curSpeed));
		} else if (diff <= -20) {
			powerMotor.setSpeed((int) Math.round(0.6 *curSpeed));
		} else if (diff < 10) {
			powerMotor.setSpeed((int) Math.round(0.8 *curSpeed));
		} else if ((diff > 60) &&  (curSpeed == 0)) {
			powerMotor.setSpeed((int) Math.round(0.2 * powerMotor.getMaxSpeed()));
		} else if (diff > 10) {
			powerMotor.setSpeed((int) Math.round(1.2 *curSpeed));
		}
	}
	
	public void start () {
		if (thread == null) {
	    	thread = new Thread (this);
	    	thread.setPriority(thread.getPriority() + 1);
	    	//thread.setPriority(10);
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