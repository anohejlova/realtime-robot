package cz.muni.fi.IA158.robot.tasks;

import lejos.hardware.sensor.EV3IRSensor;
import lejos.utility.Delay;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.SensorPort;




public class EV3LineCar {
    private static EV3LargeRegulatedMotor powerMotor;
    private static EV3MediumRegulatedMotor steerMotor;
    private static EV3IRSensor ir;
    private static EV3ColorSensor light;

    public static void main(String[] args)
    {
    	powerMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    	if (powerMotor != null) {
    		powerMotor.backward();
    		Delay.msDelay(5000);
    		powerMotor.stop();
    	} 
    	
    	//powerMotor.setSpeed(powerMotor.getMaxSpeed());
    	//steerMotor.rotateTo(0);
    	//Steering steer = new Steering();
    	//steer.steering_cor();  	
    	
    ;}
    
    public EV3LineCar() {
    	//ir = new EV3IRSensor(SensorPort.S4);
    	powerMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    	steerMotor = new EV3MediumRegulatedMotor(MotorPort.D);
    	
    	//light = new EV3ColorSensor(SensorPort.S1);    	
    }

    
}

