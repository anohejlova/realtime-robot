package cz.muni.fi.IA158.robot.tasks.runnable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;

/**
 * The program EV3LineCarRunnable drives Lego Mindstorms EV3 robot. The robot follows the line and reacts to obstacles.
 * 
 * Main class coordinating everything.
 * Allocates and creates everything that is needed.
 * Start new threads.
 * Poll and choose next running job. 
 * Clear up and terminate program execution.
 * 
 * @author Alice Nohejlova, Pavel Fikar
 */
public class EV3LineCarRunnable {
	//values used for controlling main thread 
	private static int sleepTime = 5;
    private static int frame = 25;
	
    /**
     * Allocates and creates everything that is needed.
     * Start new threads.
     * Poll and choose next running job. 
     * Clear up and terminate program execution.
     */
    public static void main(String[] args)
    {    	
    	//creates new pointers to the motors and sensors
    	EV3LargeRegulatedMotor powerMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    	EV3MediumRegulatedMotor steerMotor = new EV3MediumRegulatedMotor(MotorPort.D);
    	EV3ColorSensor colorSen = new EV3ColorSensor(SensorPort.S1);
    	EV3IRSensor IRSen = new EV3IRSensor(SensorPort.S4);
    	
    	//queues for jobs of steering and distance check tasks
    	BlockingQueue<Job> queueDist = new ArrayBlockingQueue<>(10);
    	BlockingQueue<Job> queueSteer = new ArrayBlockingQueue<>(10);
    	
    	//creation of shared class and instances of steering and distance check tasks
    	Suspender sus = new Suspender();
    	SteeringRunnable steering = new SteeringRunnable(queueSteer, powerMotor, steerMotor, colorSen, sus);
    	DistanceCheckRunnable distance = new DistanceCheckRunnable(queueDist, powerMotor, IRSen, sus);
    	

    	//initialization
    	Job distJob = null;
    	Job steerJob = null;
    	int lifeCounter = 0;

    	//try-check is necessary because of Thread.sleep
    	try {
    		//saving the name of the main thread
    		Thread mainThread = Thread.currentThread();
    		mainThread.setPriority(1);
    		
    		//start of distance check and steering tasks
    		mainThread.sleep(50 * frame);
        	distance.start();
    		mainThread.sleep(50 * frame);
    		steering.start();
    		
        	//staring the power motor
        	powerMotor.backward();        	
        	
        	//limits the time how long the entire code will run, it´s an arbitrary number
        	//it´s a cycle of getting current available jobs, comparing deadlines, selecting job that will run and letting it get executed 
        	while (lifeCounter <= 2000) {  
    		
		    	//get available job from distance check task
        		if (distJob == null) {
		    		
					distJob = queueDist.poll();
				}
        		//get available job from steering task
		    	if (steerJob == null) {
		    		
		    		steerJob = queueSteer.poll();
				}
		    	
		    	//if jobs from both tasks are available, then compare their deadline
				if ((distJob != null) && (steerJob != null)) {
					
					if (distJob.getDeadline() < steerJob.getDeadline()) {						
						sus.setSus(true); //sets the suspend to true 
						distance.resume();	//wakes up the thread for distance check task					
						mainThread.sleep(frame); //main thread go to sleep to let other threads run
						while(sus.getSus()) { //until other thread sets suspend to false the main thread will wait
							mainThread.sleep(sleepTime); //
						}						
						distJob = null; //remove executed job
					} else {						
						sus.setSus(true); //sets the suspend to true
						steering.resume(); //wakes up the thread for steering task	
						mainThread.sleep(frame); //main thread go to sleep to let other threads run
						while(sus.getSus()) { //until other thread sets suspend to false the main thread will wait
							mainThread.sleep(sleepTime);
						}
						steerJob = null;
					}
				  //if only one job is available then its executed regardless of its deadline
				} else if (steerJob != null) {
					sus.setSus(true); //sets the suspend to true
					steering.resume(); //wakes up the thread for steering task	
					mainThread.sleep(frame); //main thread go to sleep to let other threads run
					while(sus.getSus()) { //until other thread sets suspend to false the main thread will wait
						mainThread.sleep(sleepTime);
					}
					steerJob = null; //remove executed job
				} else if (distJob != null) {
					sus.setSus(true); //sets the suspend to true
					distance.resume(); //wakes up the thread for distance check task	
					mainThread.sleep(frame); //main thread go to sleep to let other threads run
					while(sus.getSus()) { //until other thread sets suspend to false the main thread will wait
						mainThread.sleep(sleepTime);
					}
					distJob = null; //remove executed job
				} else {
				}
			
				lifeCounter++; //increase the iteration counter
    	}}catch (InterruptedException e) {
 	       System.out.println("Main Thread interrupted.");
 	    }
 	    System.out.println("Main thread exiting.");
 	    
 	    //closing all pointers to motors and sensors
 	    powerMotor.close();
 	    steerMotor.close();
 	    colorSen.close();
 	    IRSen.close();
		System.exit(0); //force exit to kill other threads
    }
}