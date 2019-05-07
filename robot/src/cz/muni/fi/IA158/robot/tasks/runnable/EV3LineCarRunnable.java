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
    	steering.start();
    	distance.start();

    	
    	Job distJob = null;
    	Job steerJob = null;
    	int lifeCounter = 0;
    	
    	while (lifeCounter <= 5000) {  
    		
	    	if (distJob == null) {
	    		
				distJob = queueDist.poll();
				System.err.println("poll queueDist");
			}
	    	if (steerJob == null) {
	    		
	    		steerJob = queueSteer.poll();
	    		System.err.println("poll queueSteer");
			}
			if ((distJob != null) && (steerJob != null)) {
				
				if (distJob.getDeadline() < steerJob.getDeadline()) {
					System.err.println("main - 1. condition - front");
					distance.resume();
					Thread.yield();
					System.err.println("main - 1. condition -back");
					distJob = null;
				} else {
					System.err.println("main - 2. condition- front");
					steering.resume();
					Thread.yield();
					System.err.println("main - 2. condition - back");
					steerJob = null;
				}
			} else if (steerJob != null) {
				steering.resume();
				Thread.yield();
				System.err.println("main - 3. condition");
				distJob = null;
			} else if (distJob != null) {
				System.err.println("main - 4. condition-front");
				distance.resume();
				Thread.yield();
				System.err.println("main - 4. condition-back");
				steerJob = null;
			}
			
			lifeCounter++;
    	}
		   	
    }
}

