package cz.muni.fi.IA158.robot.tasks.runnable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;

public class EV3LineCarRunnable {
	
	private static int sleepTime = 5;
    public static void main(String[] args)
    {    	
    	int frame = 25;
    	
    	
    	EV3LargeRegulatedMotor powerMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    	EV3MediumRegulatedMotor steerMotor = new EV3MediumRegulatedMotor(MotorPort.D);
    	EV3ColorSensor colorSen = new EV3ColorSensor(SensorPort.S1);
    	EV3IRSensor IRSen = new EV3IRSensor(SensorPort.S4);
    	
    	BlockingQueue<Job> queueDist = new ArrayBlockingQueue<>(10);
    	BlockingQueue<Job> queueSteer = new ArrayBlockingQueue<>(10);
    	
    	
    	Suspender sus = new Suspender();
    	SteeringRunnable steering = new SteeringRunnable(queueSteer, powerMotor, steerMotor, colorSen, sus);
    	DistanceCheckRunnable distance = new DistanceCheckRunnable(queueDist, powerMotor, IRSen, sus);
    	

    	
    	Job distJob = null;
    	Job steerJob = null;
    	int lifeCounter = 0;
    	try {
    		Thread mainThread = Thread.currentThread();
    		mainThread.setPriority(1);
    		
    		mainThread.sleep(50 * frame);
        	distance.start();
    		mainThread.sleep(50 * frame);
    		steering.start();
    		
        	
        	powerMotor.backward();        	
        	long now = 0;
        	while (lifeCounter <= 2000) {  
    		
		    	if (distJob == null) {
		    		
					distJob = queueDist.poll();
					//System.out.println("poll queueDist");
				}
		    	if (steerJob == null) {
		    		
		    		steerJob = queueSteer.poll();
		    		//System.out.println("poll queueSteer");
				}
				if ((distJob != null) && (steerJob != null)) {
					
					if (distJob.getDeadline() < steerJob.getDeadline()) {
						//System.out.println("con 1");
						//System.out.println("distance main " + System.currentTimeMillis() + " deadline " + distJob.getDeadline());						
						sus.setSus(true);
						distance.resume();						
						mainThread.sleep(frame);
						while(sus.getSus()) {
							mainThread.sleep(sleepTime);
						}
						
						distJob = null;
					} else {
						//System.out.println("con 2");						
						sus.setSus(true);
						steering.resume();			
						mainThread.sleep(frame);
						while(sus.getSus()) {
							mainThread.sleep(sleepTime);
						}
						steerJob = null;
					}
				} else if (steerJob != null) {
					//System.out.println("con 3");
					sus.setSus(true);
					steering.resume();
					mainThread.sleep(frame);
					while(sus.getSus()) {
						mainThread.sleep(sleepTime);
					}
					steerJob = null;
				} else if (distJob != null) {
					//System.out.println("con 4");
					sus.setSus(true);
					distance.resume();
					mainThread.sleep(frame);
					while(sus.getSus()) {
						mainThread.sleep(sleepTime);
					}
					distJob = null;
				} else {
					//System.out.println("NO jobs");
				}
			
				lifeCounter++;
    	}}catch (InterruptedException e) {
 	       System.out.println("Main Thread interrupted.");
 	    }
 	    System.out.println("Main thread exiting.");
 	    
 	    powerMotor.close();
 	    steerMotor.close();
 	    colorSen.close();
 	    IRSen.close();
		System.exit(0);
    }
}