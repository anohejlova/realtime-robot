package cz.muni.fi.IA158.robot.tasks.runnable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;

public class EV3LineCarRunnable {
	
	static EV3LargeRegulatedMotor powerMotor;

    public static void main(String[] args)
    {
    	
    	EV3LargeRegulatedMotor powerMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    	BlockingQueue<Job> queueDist = new ArrayBlockingQueue<>(10);
    	BlockingQueue<Job> queueSteer = new ArrayBlockingQueue<>(10);
    	
    	
    	
    	SteeringRunnable steering = new SteeringRunnable(queueSteer);
    	DistanceCheckRunnable distance = new DistanceCheckRunnable(queueDist);
    	Thread steerThread = new Thread(steering);
    	Thread distThread = new Thread(distance);
    	steerThread.start();
    	distThread.start();
    	new Thread(distance).start();
    	
    	Job distJob = null;
    	Job steerJob = null;
    	int lifeCounter = 0;
    	
    	while (lifeCounter <= 5000) {  
    		
	    	if (distJob == null) {
	    		
				distJob = queueDist.poll();
			}
	    	if (steerJob == null) {
	    		
	    		steerJob = queueSteer.poll();
			}
			if ((distJob != null) && (steerJob != null)) {
				
				if (distJob.getDeadline() < steerJob.getDeadline()) {
					distThread.notify();
					System.err.println("main - 1. condition");
					distJob = null;
				} else {
					steerThread.notify();
					System.err.println("main - 2. condition");
					steerJob = null;
				}
			} else if (steerJob != null) {
				steerThread.notify();
				System.err.println("main - 3. condition");
				distJob = null;
			} else if (distJob != null) {
				distThread.notify();
				System.err.println("main - 4. condition");
				steerJob = null;
			}
			
			lifeCounter++;
    	}
		   	
    }
}

