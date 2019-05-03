package cz.muni.fi.IA158.robot.tasks;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.SensorPort;

public class Steering
{	
    static EV3LargeRegulatedMotor powerMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    static EV3MediumRegulatedMotor steerMotor = new EV3MediumRegulatedMotor(MotorPort.D);    
    static EV3ColorSensor light = new EV3ColorSensor(SensorPort.S1);
    
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
		for(int i=0;i<history_size; ++i)
		{
			meas_his[i] = 0;
		}
		
		steer_his = new int[history_size];
		for(int i=0;i<history_size; ++i)
		{
			steer_his[i] = 0;
		}
	}
	
		
    private void setLargeMotorSpeed(int newSpeed)
    {
    	powerMotor.setSpeed(newSpeed);
    }
    
    private int getLargeMotorSpeed()
    {
    	return powerMotor.getSpeed();
    }
    
    private void change_angle(int angle)
    {
    	steerMotor.rotateTo(angle, true);
    }
    
    private int measurment()
    {
    	return (int)(Math.round(((light.getRedMode().sampleSize())*100)));
    }
	
	public void steering_cor(){
	
		setLargeMotorSpeed(5);
		steerMotor.rotateTo(0, false);
		while(true){
			measurment = measurment();	
			change = meas_his[history_size + (current_his_pos - 1) % history_size];
			if((change > (-threshold)) && (change < threshold))
			{
				//change engine speed +5%
				meas_his[current_his_pos] = measurment;
				steer_his[current_his_pos] = 0;
				current_his_pos = (current_his_pos + 1) % history_size;
				//suspend
				continue;
			}
			
			last_angle = steer_his[history_size + (current_his_pos -1) % history_size];
			last_angle = Math.min(max_angle, last_angle);
			last_angle = Math.max(((-1)*max_angle), last_angle);
			count = (int)Math.round(change / threshold);
			
			if(change > 0)
			{
				new_angle = (int) level_angle * count;
				if(last_angle > 0)
				{
					change_angle((-1)*(last_angle + new_angle));
					steer_his[current_his_pos] = (-1) * new_angle;
				}else{
					change_angle((-1)*last_angle + new_angle);
					steer_his[current_his_pos] = new_angle;
				}
				
				//change engine speed -10%
				meas_his[current_his_pos] = measurment;				
				current_his_pos = (current_his_pos + 1) % history_size;
				//suspend
				continue;
			}else{
				new_angle = (level_angle / 4) * count;
				if(last_angle > 0)
				{
					change_angle((-1)*(new_angle));
					steer_his[current_his_pos] = last_angle - new_angle;
				}else{
					change_angle(new_angle);
					steer_his[current_his_pos] = last_angle + new_angle;
				}
				
				//change engine speed += count * 5%
				meas_his[current_his_pos] = measurment;				
				current_his_pos = (current_his_pos + 1) % history_size;
				//suspend
				continue;				
			}
		}
	}
}