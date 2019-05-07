package cz.muni.fi.IA158.robot.tasks;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.SensorPort;

public class Steering
{	
    //static EV3LargeRegulatedMotor powerMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    //static EV3MediumRegulatedMotor steerMotor = new EV3MediumRegulatedMotor(MotorPort.D);    
    //static EV3ColorSensor light = new EV3ColorSensor(SensorPort.S1);
    
	private static int history_size = 5;
	private static double threshold = 0.1;
	private static int max_angle = 60;
	private static int level_angle = 10;

	private int current_his_pos = 0;
	private int count = 0;
	private int measurment = 0;
	private int change = 0;
	private int new_angle = 0;
	private int last_angle = 0;

	private int[] meas_his;
	private int[] steer_his;	
	
	
	public Steering()
	{
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
	}
	
		
    private void setLargeMotorSpeed(int newSpeed)
    {
    	//powerMotor.setSpeed(newSpeed);
    }
    
    private int getLargeMotorSpeed()
    {
    	//return powerMotor.getSpeed();
    	return 50;
    }
    
    private void changeAngle(int angle)
    {
    	//steerMotor.rotateTo(angle, false);
    }
    
    private int measurment()
    {
    	//return (int)(Math.round(((light.getRedMode().sampleSize())*100)));
    	return 50;
    }
	
	public void steering_cor(){
	
		setLargeMotorSpeed(100);
		changeAngle(0);
		int o=0;
		while(o<1000){
			++o;
			measurment = measurment();	
			change = meas_his[(history_size + (current_his_pos - 1)) % history_size];
			if((change > (-threshold)) && (change < threshold))
			{
				setLargeMotorSpeed((getLargeMotorSpeed() /100) * 105);
				meas_his[current_his_pos] = measurment;
				steer_his[current_his_pos] = 0;
				current_his_pos = (current_his_pos + 1) % history_size;
				//suspend
				continue;
			}
			
			last_angle = steer_his[(history_size + (current_his_pos -1)) % history_size];
			last_angle = Math.min(max_angle, last_angle);
			last_angle = Math.max(((-1)*max_angle), last_angle);
			count = (int)Math.round(change / threshold);
			
			if(change > 0)
			{
				new_angle = (int) level_angle * count;
				if(last_angle > 0)
				{
					changeAngle((-1)*(last_angle + new_angle));
					steer_his[current_his_pos] = (-1) * new_angle;
				}else{
					changeAngle((-1)*last_angle + new_angle);
					steer_his[current_his_pos] = new_angle;
				}
				
				setLargeMotorSpeed((getLargeMotorSpeed() /100) * 90);
				meas_his[current_his_pos] = measurment;				
				current_his_pos = (current_his_pos + 1) % history_size;
				//suspend
				continue;
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
				current_his_pos = (current_his_pos + 1) % history_size;
				//suspend
				continue;				
			}
		}
	}
	
	
	
	
	
}