package cz.muni.fi.IA158.robot.tasks.runnable;

import java.util.concurrent.BlockingQueue;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;

public class SteeringRunnable implements Runnable{
	
	static EV3LargeRegulatedMotor powerMotor;
    static EV3MediumRegulatedMotor steerMotor;    
    static EV3ColorSensor light;
    
    private float baseLine;
    private int thres = 10;
    private int timer = 0;
    private int maxSpeed = 180;
    
	private static int history_size = 11;
	private static double threshold = 0.05;
	private static int max_angle = 50;
	private static int level_angle = 10;

	private int current_his_pos = 0;
	private int count = 0;
	private int measurement = 0;
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
			steer_his[i] = 0;
		}
		
		powerMotor = power;
		steerMotor = steer;
		light = lightSen;
		
		baseLine = measurementFloat();
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
			measurement = measurement();
			setLargeMotorSpeed(maxSpeed);
			for(int i=0;i<history_size; i++)
			{
				meas_his[i] = measurement;
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
			    //steer();
			    steeringFix();
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
    	steerMotor.rotate(angle, true);

    }
    
    private void changeAngleTo(int angle)
    {
    	steerMotor.rotateTo(angle, true);

    }
    
    private int measurement()
    {
    	//return (int)(Math.round(((light.getRedMode().sampleSize())*100)));
    	//return 50;
    	
    	float [] sample = new float[light.getRedMode().sampleSize()];
	    light.getRedMode().fetchSample(sample, 0);
	    float measurementVal = sample[0];
	    //System.out.println("Measurement " + measurementVal);
	    return (int)(measurementVal * 100);
    }
    
    private float measurementFloat()
    {
    	//return (int)(Math.round(((light.getRedMode().sampleSize())*100)));
    	//return 50;
    	
    	float [] sample = new float[light.getRedMode().sampleSize()];
	    light.getRedMode().fetchSample(sample, 0);
	    return  (sample[0] * 100);
	    
    }
	
	public void steer() {
		float measurementFloat;
		measurementFloat = measurementFloat();
		measurementFloat = measurementFloat * 100;
		
		System.out.println(measurementFloat);
		
		if(measurementFloat >= 40) {
			setLargeMotorSpeed((getLargeMotorSpeed() /100) * 110);
			//meas_his[current_his_pos] = measurement;
			//steer_his[current_his_pos] = 0;
			
			System.out.println("keep direction");
			return;
		} else if(measurementFloat >= 25) {
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
		} else if(measurementFloat >= 12) {
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
    
    public void steeringFix() {
	float measurement = measurementFloat();
//	    System.out.println("Measurement " + measurement);
		int diff = (int)(baseLine - measurement);
//	    System.out.println("Diff " + diff);
	    int speed = getLargeMotorSpeed();

	    
	    if(Math.abs(diff) <= 10) {
	    	if(speed < maxSpeed) {
	    		setLargeMotorSpeed((speed / 10) * 11);
	    	}
	    	
	    }
	    
	    changeAngle(diff/8);
		return;	
    }
	
    public void steering_cor(){
	
		int hist = 0;
		measurement = measurement();	
		change = meas_his[indexChange(current_his_pos, 1)];
		if((meas_his[indexChange(current_his_pos, 1)] - measurement) > threshold) {
			change = meas_his[indexChange(current_his_pos, 1)] - measurement;
			hist = 1;
			}
		else {
			if((meas_his[indexChange(current_his_pos, 5)] - measurement) > threshold) {
				change = meas_his[indexChange(current_his_pos, 5)] - measurement;
				hist = 5;
			}
			else {
				change = meas_his[indexChange(current_his_pos, 10)] - measurement;
				hist = 10;	
			}			
		}
			
		
		
		if((change > (-threshold)) && (change < threshold))
		{
			setLargeMotorSpeed((getLargeMotorSpeed() /100) * 105);
			meas_his[current_his_pos] = measurement;
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
			meas_his[current_his_pos] = measurement;				
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
			meas_his[current_his_pos] = measurement;				
			current_his_pos = indexPlusOne(current_his_pos); //++position
			return;				
			}
		   
		
	}


}
