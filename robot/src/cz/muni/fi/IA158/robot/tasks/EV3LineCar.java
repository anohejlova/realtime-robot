package cz.muni.fi.IA158.robot.tasks;

import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.SensorPort;




public class EV3LineCar {
    static EV3LargeRegulatedMotor powerMotor;
    static EV3MediumRegulatedMotor steerMotor;
    static EV3IRSensor ir;
    static EV3ColorSensor light;

    public static void main(String[] args)
    {
    	//powerMotor.setSpeed(powerMotor.getMaxSpeed());
    	//steerMotor.rotateTo(0);
    	Steering steer = new Steering();
    	steer.steering_cor();  	
    	
    ;}
    
    public EV3LineCar() {
    	//ir = new EV3IRSensor(SensorPort.S4);
    	//powerMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    	//steerMotor = new EV3MediumRegulatedMotor(MotorPort.D);
    	//light = new EV3ColorSensor(SensorPort.S1);    	
    }
    
/*    public void setLargeMotorSpeed(int newSpeed)
    {
    	powerMotor.setSpeed(newSpeed);
    }
    
    public void changeAngle(int angle)
    {
    	steerMotor.rotateTo(angle, true);
    }
    
    public int measurment()
    {
    	return (int)(Math.round(((light.getRedMode().sampleSize())*100)));
    }*/
    
    
}

