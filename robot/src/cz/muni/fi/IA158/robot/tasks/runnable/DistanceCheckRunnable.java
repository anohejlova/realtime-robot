package cz.muni.fi.IA158.robot.tasks.runnable;

import java.util.concurrent.BlockingQueue;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3IRSensor;

/**
 * Class DistanceCheckRunnable represents the task of checking distance before the robot 
 * using the IR sensor and consequent reaction by adjusting the speed.
 *
 */
public class DistanceCheckRunnable implements Runnable{
	
	public Thread thread; // thread based on this runnable
	boolean suspended = false; // if the thread of this class should sleep
	private BlockingQueue<Job> queueDist; // the queue in which new generated jobs will be added
	long releaseDeadlineDiff = 200; // difference between release and deadline of the created job
	private int lastDistance = 100; // last distance measured
	private int newDistance; // new measured distance
	private EV3IRSensor ir; // IR sensor of the robot
	private EV3LargeRegulatedMotor powerMotor; // power motor of the robot
	private Suspender mainThread; // suspender of the main program thread
	
	/**
	 * Constructor for the class DistanceCheckRunnable
	 * 
	 * Initializes resources for thread of this class. 
	 * 
	 * @param queueDist  the queue in which new generated jobs will be added
	 * @param powerMotor power motor of the robot
	 * @param ir         IR Sensor of the robot
	 * @param mainThread Suspender of the main thread
	 * 
	 * @throws IllegalArgumentException if motor or sensor are not initialized
	 */
	public DistanceCheckRunnable(BlockingQueue<Job> queueDist, EV3LargeRegulatedMotor powerMotor, EV3IRSensor ir, Suspender mainThread) {
		this.queueDist = queueDist;
		this.ir = ir;
		this.powerMotor = powerMotor;
		
		if (this.ir == null) {
			throw new java.lang.IllegalArgumentException("no IR Sensor initialized in distance task");
		}
		if (this.powerMotor == null) {
			throw new java.lang.IllegalArgumentException("no large motor initialized in distance task");
		}
		
		this.mainThread = mainThread;
	}
	
	/**
	 * Method run runs checking of distance and based on its result react with adjusting the speed, 
	 * releases new Job to the queue and suspends thread of this class.
	 */
	@Override
	public void run() {
	
		try{	
			while(true) {
							
				long now = System.currentTimeMillis(); // number of milliseconds from start of the epoch
				Job release = new Job(now, now + releaseDeadlineDiff);
				queueDist.add(release);
		    
				suspend();
				mainThread.setSus(false);
			    synchronized(this) {
			    	while(suspended) {
			    		wait();
			    	}		    	
			    }			    
			    if (checkDistance()) {
			    	adaptSpeed();
			    }
			}  
	    } catch (InterruptedException e) {
	       System.err.println("distThread interrupted.");
	    }
	    //System.out.println("distThread exiting.");
	}
	
	/**
	 * Method checkDistance measures distance in front of the robot
	 * 
	 * Measures distance using the IR sensor and compares it with the last measurement.
	 * 
	 * @return if the power motor should react by adjusting the speed
	 */
	private boolean checkDistance() {
		
		lastDistance = newDistance;
		
		float [] sample = new float[ir.sampleSize()];
	    ir.fetchSample(sample, 0);
	    newDistance = (int)sample[0];	
		
		
		int diff = java.lang.Math.abs(newDistance - lastDistance);
		
		return ((diff > 10) || (newDistance < 50));
	      
	}
	
	/**
	 * Method adaptSpeed reacts to the current and last measured distance.
	 * 
	 * If current measured speed is too low, the power motor stops.
	 * If the current distance is less than last distance, the power motor slows down.
	 * If the current distance is more than last distance, the power motor speeds up.
	 */
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
		} else if ((diff > 60) && (lastDistance <= 20)) {
			powerMotor.setSpeed(180);
			powerMotor.backward();
		} else if (newDistance > 70) {
			powerMotor.setSpeed(180);			
		} else if (diff > 10) {
			powerMotor.setSpeed((int) Math.round(1.2 *curSpeed));
		}
	}
	
	/**
	 * Method start starts the thread on this runnable class.
	 */
	public void start () {
		if (thread == null) {
	    	thread = new Thread (this);
	    	thread.start();
		}
	}
	
	
	/**
	 * Method suspend enables suspending of thread of this class.
	 */
	private void suspend() {
		suspended = true;
	}
	   
	/**
	 * Method resume awakes thread of this class.
	 */
	public synchronized void resume() {
		suspended = false;
	    notify();
	}
}