package ev3;

import lejos.remote.ev3.RemoteRequestEV3;
import lejos.remote.ev3.RemoteRequestPilot;
import lejos.remote.ev3.RemoteRequestRegulatedMotor;
import lejos.robotics.RegulatedMotor;

public class Robot extends BasicRobot
{
    RemoteRequestRegulatedMotor motorL;
    RemoteRequestRegulatedMotor motorR;
    RemoteRequestRegulatedMotor motor;
    RemoteRequestPilot pilot;

	EASSUltrasonicSensor uSensor;
	EASSRGBColorSensor cSensor;

    private boolean closed = false;
    private boolean straight = false;

    int ultra_port = 2;
    int color_port = 3;

    int slow_turn = 70;
    int fast_turn = 80;
    int travel_speed = 10;

    public Robot()
    {
		
    }

    public void connectToRobot(String address) throws Exception
    {

        connect(address);
		closed = false;
        RemoteRequestEV3 brick = getBrick();


        String ultra_portstring = "S" + ultra_port;
        String color_portstring = "S" + color_port;

        try {
            System.out.println("Connecting to Ultrasonic Sensor" + '\n');
            uSensor = new EASSUltrasonicSensor(brick, ultra_portstring);
            System.out.println("Connected to Sensor " + '\n');
            setSensor(ultra_port, uSensor);
        } catch (Exception e) {
            brick.disConnect();
            throw e;
        }


        try {
            System.out.println("Connecting to Colour Sensor " + '\n');
            cSensor = new EASSRGBColorSensor(brick, color_portstring);
            System.out.println("Connected to Sensor " + '\n');
            setSensor(color_port, cSensor);
        } catch (Exception e) {
            uSensor.close();
            brick.disConnect();
            throw e;
        }

        try {
            System.out.println("Creating Pilot " + '\n');
            // Creating motors as well as pilot in order to allow turning on the spot.
            motorR = (RemoteRequestRegulatedMotor) brick.createRegulatedMotor("B", 'L');
            motorL = (RemoteRequestRegulatedMotor) brick.createRegulatedMotor("C", 'L');
            motorR.setSpeed(200);
            motorL.setSpeed(200);
            pilot = (RemoteRequestPilot) brick.createPilot(7, 20, "C", "B " + '\n');
            System.out.println("Created Pilot " + '\n');
        } catch (Exception e) {
            uSensor.close();
            cSensor.close();
            brick.disConnect();
            throw e;
        }

        try {
            System.out.println("Contacting Medium Motor " + '\n');
            motor = (RemoteRequestRegulatedMotor) brick.createRegulatedMotor("A", 'M');
            System.out.println("Created Medium Motor " + '\n');
        } catch (Exception e) {
            //uSensor.close();
            //cSensor.close();
            motorR.close();
            motorL.close();
            brick.disConnect();
            throw e;
        }
    }

    public void close() {
        if (! closed) {
            super.disconnected = true;
            try {
                motor.stop();
				//System.out.println("   Closing Jaw Motor " + '\n');
				motor.close();

                Thread.sleep(10);
				motorR.stop();
				motorL.stop();
				System.out.println("   Closing Right Motor " + '\n');
				motorR.close();

                Thread.sleep(10);
				System.out.println("   Closing Left Motor " + '\n');
				motorL.close();

                Thread.sleep(10);
                pilot.stop();
                System.out.println("   Closing Pilot " + '\n');
                pilot.close();
                Thread.sleep(10);
            } catch (Exception e) {

            }
            System.out.println("   Closing Remaining Sensors" + '\n');
			super.close();
        }
        closed = true;
    }


    /**
     * Get the medium motor that control the dinosaur's jaws.
     * @return
     */
    public RegulatedMotor getMotor() {
        return motor;
    }

    public void setTravelSpeed(int travelSpeed)
	{
		travel_speed = travelSpeed;
	}

    /**
     * Move forward
     */
    public void forward() {
        pilot.setTravelSpeed(travel_speed);
        if (!straight) {
            straight = true;
        }

        pilot.forward();
    }
    
    public void forwardBy(int d)
    {
    	pilot.setTravelSpeed(travel_speed);
    	if(!straight)
    	{
    		straight = true;
    	}
    	
    	pilot.travel(d);
    }

    /**
     * Move forward a short distance.
     */
    public void short_forward() {
        pilot.setTravelSpeed(travel_speed);
        if (!straight) {
            straight = true;
        }

        pilot.travel(10);
    }


    /**
     * Move backward
     */
    public void backward() {
        pilot.setTravelSpeed(travel_speed);
        if (!straight) {
            straight = true;
        }
        pilot.backward();
    }
    
    public void backwardBy(int d)
    {
    	pilot.setTravelSpeed(travel_speed);
    	if(!straight)
    	{
    		straight = true;
    	}
    	
    	pilot.travel(-d);
    }

    /**
     * Move backward a short distance.
     */
    public void short_backward() {
        pilot.setTravelSpeed(travel_speed);
        if (!straight) {
            straight = true;
        }
        pilot.travel(-10);
    }

    /**
     * Stop.
     */
    public void stop() {
        pilot.stop();
    }

    /**
     * Turn left on the spot.
     */
    public void left() {
        motorR.setSpeed(fast_turn);
        motorL.setSpeed(fast_turn);
        motorR.backward();
        motorL.forward();
        straight = false;
    }
    
    public void leftBy(int d)
    {
    	pilot.setRotateSpeed(travel_speed);    	
    	pilot.rotate(-d);
    	straight = false;
    }

    /**
     * Turn left through an angle (approx 90 on the wheeled robots).
     */
    public void short_left() {
        pilot.setRotateSpeed(travel_speed);
        pilot.rotate(-90);
        straight = false;
    }

    /**
     * Move left around stopped wheel.
     */
    public void forward_left() {
        motorL.setSpeed(slow_turn);
        motorL.forward();
        motorR.stop();
        straight = false;
    }

    /**
     * Turn right on the spot.
     */
    public void right() {
        motorR.setSpeed(fast_turn);
        motorL.setSpeed(fast_turn);
        motorR.forward();
        motorL.backward();
        straight = false;
    }
    
    public void rightBy(int d)
    {
    	pilot.setRotateSpeed(travel_speed);    	
    	pilot.rotate(d);
    	straight = false;
    }

    /**
     * Turn a short distance right (approx 90 on a wheeled robot)
     */
    public void short_right() {
        pilot.setRotateSpeed(travel_speed);
        pilot.rotate(90);
        straight = false;
    }


    /**
     * Turn right around stopped left whell.
     */
    public void forward_right() {
        motorR.setSpeed(slow_turn);
        motorR.forward();
        motorL.stop();
        straight = false;
    }

    /**
     * Snap jaws to scare something.
     */
    public void scare() {
        int pos = motor.getTachoCount();
        motor.rotateTo(pos + 20);
        motor.waitComplete();
        motor.rotateTo(pos);
        motor.waitComplete();
        motor.rotateTo(pos + 20);
        motor.waitComplete();
        motor.rotateTo(pos);
        motor.waitComplete();
    }

    /**
     * Rotate right motor some angle.
     * @param d
     */
    public void turn(int d) {
        motorR.rotate(d);
    }

    public EASSRGBColorSensor getRGBSensor() {
        return cSensor;
    }

	public EASSUltrasonicSensor getuSensor()
	{
		return uSensor;
	}

	public boolean isMoving()
	{
		return pilot.isMoving();
	}
}
