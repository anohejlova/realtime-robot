package cz.muni.fi.IA158.robot.tasks;

import cz.muni.fi.IA158.robot.tasks.EV3LineCar;
import lejos.robotics.subsumption.Behavior;

class DistanceCheck implements Behavior {
	
	private int lastDistance = 0;
	private int newDistance;

	private boolean checkDistance() {
		
		lastDistance = newDistance;
		newDistance = EV3LineCar.ir.getDistanceMode().sampleSize();
		int diff = java.lang.Math.abs(newDistance - lastDistance);
		
		return ((diff > 10) || (newDistance < 50));
	      
	  }
	      
	
	@Override
	public boolean takeControl() {
		return checkDistance();
	}

	@Override
	public void action() {
		
		
		if (newDistance <= 20) {
			EV3LineCar.powerMotor.stop();
			return;
		} 
		
		int diff = newDistance - lastDistance;
		double curSpeed = EV3LineCar.powerMotor.getSpeed();
					
		if (diff <= -60) {
			EV3LineCar.powerMotor.setSpeed((int) Math.round(0.2 *curSpeed));
		} else if (diff <= -40) {
			EV3LineCar.powerMotor.setSpeed((int) Math.round(0.4 *curSpeed));
		} else if (diff <= -20) {
			EV3LineCar.powerMotor.setSpeed((int) Math.round(0.6 *curSpeed));
		} else if (diff < 10) {
			EV3LineCar.powerMotor.setSpeed((int) Math.round(0.8 *curSpeed));
		} else if (diff > 60) {
			EV3LineCar.powerMotor.setSpeed(EV3LineCar.powerMotor.getMaxSpeed());
		} else if (diff > 10) {
			EV3LineCar.powerMotor.setSpeed((int) Math.round(1.2 *curSpeed));
		}
		
	}

	@Override
	public void suppress() {
	}
	
}
