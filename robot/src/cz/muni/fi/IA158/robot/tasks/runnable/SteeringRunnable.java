package cz.muni.fi.IA158.robot.tasks.runnable;

import java.util.concurrent.BlockingQueue;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;

public class SteeringRunnable implements Runnable{
	
	//pointers to motors and sensor
	static EV3LargeRegulatedMotor powerMotor;
    static EV3MediumRegulatedMotor steerMotor;    
    static EV3ColorSensor light;
    
    private float baseLine;
    private int maxSpeed = 270;
	
	public Thread thread;
	boolean suspended = false;
	//queue for steering
	BlockingQueue<Job> queueSteer;	
	//deadline of steering tasks
	long releaseDeadlineDiff = 100;

	//used to supress main thread
	private Suspender mainThread;

	//constructor 
	public SteeringRunnable(BlockingQueue<Job> queueSteer, EV3LargeRegulatedMotor power, EV3MediumRegulatedMotor steer, EV3ColorSensor lightSen, Suspender mainThread)
	{
		this.queueSteer = queueSteer;
				
		powerMotor = power;
		steerMotor = steer;
		light = lightSen;
		
		baseLine = measurementFloat();
		
		this.mainThread = mainThread;
	}
	
	
	/**
	 * Function is called when new thread is started.
	 * It takes care of executing steering jobs and releasing new one.
	 * First it checks correct resource allocation.
	 * Creates new job of steering task and add to the queue. Suspends itself. Executes a job of steering task.
	 */
	@Override
	public void run() {
		try{
			//check if motors and sensor were loaded properly
			if (powerMotor == null) {
				throw new java.lang.IllegalArgumentException("no power motor initialized in streering task");
			}
			if (steerMotor == null) {
				throw new java.lang.IllegalArgumentException("no medium motor initialized in streering task");
			}
			if (light == null) {
				throw new java.lang.IllegalArgumentException("no light sensor initialized in streering task");
			}
			
			//reset steering angle and set max engine speed
			steerMotor.rotateTo(0, false);
			setLargeMotorSpeed(maxSpeed);

			//the ensures that this thread never stops
			while(true) {
				long now = System.currentTimeMillis(); // number of milliseconds from start of the epoch

				//create new job of steering task
				Job release = new Job(now, now + releaseDeadlineDiff);
				
				//add new task to the queue
				queueSteer.add(release);
				
				//set suspend variable to true
			    suspend();

			    //stop suspending main thread
			    mainThread.setSus(false);

			    //enter wait cycle
			    synchronized(this) {
			    	while(suspended) {
			    		wait();
			    	}
			    }
			    //execute steering job
			    steeringFix();
			}   		       
		 } catch (InterruptedException e) {
		    System.out.println("steerThread interrupted.");
		 }
		 System.out.println("steerThread exiting.");
	}
		
	/**
	 * Function creates a new thread that is tasked with executing this class instance.
	 */
	public void start () {
		if (thread == null) {
	    	thread = new Thread (this);
	    	thread.start();
		}
	}
	
	/**
	 * Sets the suspend variable to true to cause the thread to start the wait.
	 */
	private void suspend() {
		suspended = true;
	}
	   
	/**
	 * Sets the suspend variable to false to stop the thread from waiting.
	 */
	public synchronized void resume() {
		suspended = false;
	    notify();
	}
	
	/**
	 * Set news maximum speed for the large engine (Power engine).
	 * @param newSpeed - new maximum speed
	 */
    private void setLargeMotorSpeed(int newSpeed)
    {
    	powerMotor.setSpeed(newSpeed);
    }
    
    /**
     * Get the current maximum speed of the large engine (Power engine).
     * @return current maximum speed of the large engine
     */
    private int getLargeMotorSpeed()
    {
    	return powerMotor.getSpeed();
    }
    
    /**
     * Changes angle of the medium engine by value given in angle. Change is relative to current angle.
     * @param angle number of degrees
     */
    private void changeAngle(int angle)
    {
    	steerMotor.rotate(angle, true);

    }
    
    /**
     * Measure the amount of reflected light with color sensor
     * @return the amount of reflected light
     */
    private float measurementFloat()
    {	
    	float [] sample = new float[light.getRedMode().sampleSize()];
	    light.getRedMode().fetchSample(sample, 0);
	    return  (sample[0] * 100);
	    
    }
    
    /**
     * Main function for steering. It measures current reflected light and adjusts the steering acoordingly.
     */
    public void steeringFix() {
    //measure new reflected light value
	float measurement = measurementFloat(); 
		
		//difference between current measurement and baseline value
		int diff = (int)(baseLine - measurement); 
	    int speed = getLargeMotorSpeed();
	    
	    //If diff value is small enough and current speed is not maximum, then accelerate by 10%.
	    if(Math.abs(diff) <= 10) {
	    	if(speed < maxSpeed) {
	    		setLargeMotorSpeed((speed / 10) * 11);
	    	}	    	
	    }	    

	    //Steering angle is adjusted only by 1/8 of diff to make the steering smoother.
	    changeAngle(diff/8);
		return;	
    }   
}