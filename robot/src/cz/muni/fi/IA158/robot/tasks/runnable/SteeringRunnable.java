package cz.muni.fi.IA158.robot.tasks.runnable;

import java.util.concurrent.BlockingQueue;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;

public class SteeringRunnable implements Runnable{
	
	static EV3LargeRegulatedMotor powerMotor;
    static EV3MediumRegulatedMotor steerMotor;    
    static EV3ColorSensor light;
	    
    private int baseLine;
    private static int cat = 5;
    private int thres = 20;
    
	private static int history_size = 11;
	private static double threshold = 0.05;
	private static int max_angle = 50;
	private static int level_angle = 10;

	private int current_his_pos = 0;
	private int count = 0;
	private int measurment = 0;
	private int change = 0;
	private int new_angle = 0;
	private int last_angle = 0;

	private int[] meas_his;
	private int[] steer_his;
	
	public Thread thread;
	boolean suspended = false;
	BlockingQueue<Job> queueSteer;
	long releaseDeadlineDiff = 100;
	
	public SteeringRunnable(BlockingQueue<Job> queueSteer, EV3LargeRegulatedMotor power, EV3MediumRegulatedMotor steer, EV3ColorSensor lightSen)
	{
		this.queueSteer = queueSteer;
		
		meas_his = new int[history_size];
		for(int i=0;i<history_size; i++)
		{
			meas_his[i] = 0;
		}
		
		steer_his = new int[history_size];
		for(int i=0;i<history_size; i++)
		{
			steer_his[i] = -3;
		}
		
		powerMotor = power;
		steerMotor = steer;
		light = lightSen;
		
		baseLine = measurment();
		//thres = baseLine / cat;
	}
	
	@Override
	public void run() {
		try{
			if (powerMotor == null) {
				throw new java.lang.IllegalArgumentException("no power motor initialized in streering task");
			}
			if (steerMotor == null) {
				throw new java.lang.IllegalArgumentException("no medium motor initialized in streering task");
			}
			if (light == null) {
				throw new java.lang.IllegalArgumentException("no light sensor initialized in streering task");
			}
			
			steerMotor.rotateTo(0, false);
			measurment = measurment();
			for(int i=0;i<history_size; i++)
			{
				meas_his[i] = measurment;
			}
			
			
			while(true) {
				//System.out.println("Steer");
				long now = System.currentTimeMillis(); // number of milliseconds from start of the epoch
				Job release = new Job(now, now + releaseDeadlineDiff);
				queueSteer.add(release);				
				//System.out.println("Steer release");
			    suspend();
			     
			    synchronized(this) {
			    	while(suspended) {
			    		wait();
			    	}
			    }
			    
			    //steering_cor();
			    steer();
			}   		       
		 } catch (InterruptedException e) {
		    System.out.println("steerThread interrupted.");
		 }
		 System.out.println("steerThread exiting.");
	}
	
	private int indexChange(int current, int change){
		return (history_size + (current - change)) % history_size;
	}
	
	private int indexPlusOne(int current){
		return (current + 1) % history_size;
	}
	
	public void start () {
		if (thread == null) {
	    	thread = new Thread (this);
	    	//thread.setPriority(thread.getPriority() + 1);
	    	//thread.setPriority(10);
	    	thread.start();
		}
	}
	
	private void suspend() {
		suspended = true;
	}
	   
	public synchronized void resume() {
		suspended = false;
	    notify();
	}
	
			
    private void setLargeMotorSpeed(int newSpeed)
    {
    	powerMotor.setSpeed(newSpeed);
    }
    
    private int getLargeMotorSpeed()
    {
    	return powerMotor.getSpeed();
    	//return 50;
    }
    
    private void changeAngle(int angle)
    {
    	steerMotor.rotateTo(angle, false);
    }
    
    private int measurment()
    {
    	//return (int)(Math.round(((light.getRedMode().sampleSize())*100)));
    	//return 50;
    	
    	float [] sample = new float[light.getRedMode().sampleSize()];
	    light.getRedMode().fetchSample(sample, 0);
	    float measurmentVal = sample[0];
//	    System.out.println("Measurement " + measurementVal);
	    return (int)(measurmentVal * 100);
    }
    
    private float measurmentFloat()
    {
    	//return (int)(Math.round(((light.getRedMode().sampleSize())*100)));
    	//return 50;
    	
    	float [] sample = new float[light.getRedMode().sampleSize()];
	    light.getRedMode().fetchSample(sample, 0);
	    return  sample[0];
	    
    }
	
	public void steer() {
		float measurmentFloat;
		measurmentFloat = measurmentFloat();
		measurmentFloat = measurmentFloat * 100;
		
		System.out.println(measurmentFloat);
		
		if(measurmentFloat >= 40) {
			setLargeMotorSpeed((getLargeMotorSpeed() /100) * 110);
			//meas_his[current_his_pos] = measurment;
			//steer_his[current_his_pos] = 0;
			
			System.out.println("keep direction");
			return;
		} else if(measurmentFloat >= 25) {
			last_angle = steer_his[indexChange(current_his_pos, 1)];
			//last_angle = Math.min(max_angle, last_angle);
			//last_angle = Math.max(((-1)*max_angle), last_angle);
			System.out.println("thres 25");
			new_angle = Math.min((int) level_angle, max_angle);
			if(last_angle > 0)
			{
				changeAngle((-1)*(last_angle + new_angle));
				steer_his[current_his_pos] = (-1) * new_angle;
			}else{
				changeAngle((-1)*last_angle + new_angle);
				steer_his[current_his_pos] = new_angle;
			}			
			//setLargeMotorSpeed((getLargeMotorSpeed() /100) * 95);							
			current_his_pos = indexPlusOne(current_his_pos); //++position
			return;
		} else if(measurmentFloat >= 12) {
			last_angle = steer_his[indexChange(current_his_pos, 1)];
			//last_angle = Math.min(max_angle, last_angle);
			//last_angle = Math.max(((-1)*max_angle), last_angle);
			System.out.println("thres 12");
			new_angle = Math.min((int) level_angle * 2, max_angle);
			if(last_angle > 0)
			{
				changeAngle((-1)*(last_angle + new_angle));
				steer_his[current_his_pos] = (-1) * new_angle;
			}else{
				changeAngle((-1)*last_angle + new_angle);
				steer_his[current_his_pos] = new_angle;
			}			
			//setLargeMotorSpeed((getLargeMotorSpeed() /100) * 75);							
			current_his_pos = indexPlusOne(current_his_pos); //++position
			return;
		} else {
			last_angle = steer_his[indexChange(current_his_pos, 1)];
			//last_angle = Math.min(max_angle, last_angle);
			//last_angle = Math.max(((-1)*max_angle), last_angle);
			System.out.println("else");
			new_angle = Math.min((int) level_angle * 4, max_angle);
			if(last_angle > 0)
			{
				changeAngle((-1)*(last_angle + new_angle));
				steer_his[current_his_pos] = (-1) * new_angle;
			}else{
				changeAngle((-1)*last_angle + new_angle);
				steer_his[current_his_pos] = new_angle;
			}			
			//setLargeMotorSpeed((getLargeMotorSpeed() /100) * 50);							
			current_his_pos = indexPlusOne(current_his_pos); //++position
			return;
		} 
		
	}
    
    public void steering_cor(){
	
		int hist = 0;
		measurment = measurment();	
		//change = meas_his[indexChange(current_his_pos, 1)];
		if((meas_his[indexChange(current_his_pos, 1)] - measurment) > threshold) {
			change = meas_his[indexChange(current_his_pos, 1)] - measurment;
			hist = 1;
			}
		else {
			if((meas_his[indexChange(current_his_pos, 5)] - measurment) > threshold) {
				change = meas_his[indexChange(current_his_pos, 5)] - measurment;
				hist = 5;
			}
			else {
				change = meas_his[indexChange(current_his_pos, 10)] - measurment;
				hist = 10;	
			}			
		}
			
		
		
		if((change > (-threshold)) && (change < threshold))
		{
			setLargeMotorSpeed((getLargeMotorSpeed() /100) * 105);
			meas_his[current_his_pos] = measurment;
			steer_his[current_his_pos] = 0;
			current_his_pos = indexPlusOne(current_his_pos); //++position
			System.out.println("keep direction");
			return;
		}
		
		last_angle = steer_his[indexChange(current_his_pos, 1)];
		last_angle = Math.min(max_angle, last_angle);
		last_angle = Math.max(((-1)*max_angle), last_angle);
		count = ((int)Math.round(change / (threshold*100)));
		
		System.out.println("Change " + change);
		System.out.println("Count " + count);
		
		if(change > 0)
		{
			new_angle = Math.min((int) level_angle * count, max_angle);
			if(last_angle > 0)
			{
				changeAngle((-1)*(last_angle + new_angle));
				steer_his[current_his_pos] = (-1) * new_angle;
			}else{
				changeAngle((-1)*last_angle + new_angle);
				steer_his[current_his_pos] = new_angle;
			}
			
			//setLargeMotorSpeed((getLargeMotorSpeed() /100) * 90);
			meas_his[current_his_pos] = measurment;				
			current_his_pos = indexPlusOne(current_his_pos); //++position
			return;
		}else{
			new_angle = (level_angle / 4) * count;
			if(last_angle > 0)
			{
				changeAngle((-1)*(new_angle));
				steer_his[current_his_pos] = last_angle - new_angle;
			}else{
				changeAngle(new_angle);
				steer_his[current_his_pos] = last_angle + new_angle;
			}
			
			setLargeMotorSpeed((getLargeMotorSpeed() /100) * 110);
			meas_his[current_his_pos] = measurment;				
			current_his_pos = indexPlusOne(current_his_pos); //++position
			return;				
			}
		   
		
	}


}
