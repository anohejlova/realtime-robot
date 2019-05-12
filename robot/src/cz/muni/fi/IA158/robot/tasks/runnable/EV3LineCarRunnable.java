package cz.muni.fi.IA158.robot.tasks.runnable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;

public class EV3LineCarRunnable {
	
	//static EV3LargeRegulatedMotor powerMotor;

    public static void main(String[] args)
    {
    	
    	//EV3LargeRegulatedMotor powerMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    	BlockingQueue<Job> queueDist = new ArrayBlockingQueue<>(10);
    	BlockingQueue<Job> queueSteer = new ArrayBlockingQueue<>(10);
    	
    	
    	
    	SteeringRunnable steering = new SteeringRunnable(queueSteer);
    	DistanceCheckRunnable distance = new DistanceCheckRunnable(queueDist);
    	

    	
    	Job distJob = null;
    	Job steerJob = null;
    	int lifeCounter = 0;
    	try {
    		Thread.sleep(2000);
    		steering.start();
    		Thread.sleep(2000);
        	distance.start();
        	
    	while (lifeCounter <= 1000) {  
    		
	    	if (distJob == null) {
	    		
				distJob = queueDist.poll();
				System.out.println("poll queueDist");
			}
	    	if (steerJob == null) {
	    		
	    		steerJob = queueSteer.poll();
	    		System.out.println("poll queueSteer");
			}
			if ((distJob != null) && (steerJob != null)) {
				
				if (distJob.getDeadline() < steerJob.getDeadline()) {
					System.out.println("con 1");					
					distance.resume();
					Thread.yield();					
					distJob = null;
				} else {
					System.out.println("con 2");
					steering.resume();
					Thread.yield();
					steerJob = null;
				}
			} else if (steerJob != null) {
				System.out.println("con 3");
				steering.resume();
				Thread.yield();
				steerJob = null;
			} else if (distJob != null) {
				System.out.println("con 4");				
				distance.resume();
				Thread.yield();
				distJob = null;
			} else {
				System.out.println("NO jobs");
			}
			
			lifeCounter++;
    	}}catch (InterruptedException e) {
 	       System.out.println("Main Thread interrupted.");
 	    }
 	    System.out.println("Main thread exiting.");
 	    
 	    
		System.exit(0);
    }
}

