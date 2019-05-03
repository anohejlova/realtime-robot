package cz.muni.fi.IA158.robot.tasks;

import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.SensorPort;



public class EV3LineCar {
    static EV3LargeRegulatedMotor powerMotor;
    static EV3IRSensor ir;


    public static void main(String[] args)
    {
    	
    ;}
    
    public EV3LineCar() {
    	ir = new EV3IRSensor(SensorPort.S4);
    	powerMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    	
    	
    }
}

