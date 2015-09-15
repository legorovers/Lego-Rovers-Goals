package ev3;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by joecollenette on 02/07/2015.
 */
public abstract class BluetoothRobot implements Runnable
{	
	public enum ConnectStatus {CONNECTED, DISCONNECTED, CONNECTING, DISCONNECTING}
	
	public enum BeliefStates {OBSTACLE,	WATER, PATH}

	public enum RobotAction	{NOTHING, FORWARD, FORWARD_BY, STOP, BACK_BY, BACKWARD, LEFT, LEFT_BY, RIGHT, RIGHT_BY}
	
	public static class BeliefSet
	{
		public float distance;
		public Color colour = Color.BLACK;
		public ArrayList<BeliefStates> states = new ArrayList<BeliefStates>();
		
		@Override
		public String toString()
		{
			StringBuilder toReturn = new StringBuilder();
			toReturn.append("Beliefs - [");
			for (int i = 0; i < states.size(); i++)
			{
				toReturn.append(states.get(i).toString());
				if (i < states.size() - 1)
				{
					toReturn.append(", ");
				}
			}
			toReturn.append("]");
			return toReturn.toString();
		}
	}

	public static class RobotDistanceAction
	{
		private RobotAction action;
		private int distance;
		
		public RobotDistanceAction(RobotAction ra, int d)
		{
			action = ra;
			distance = d;
		}
		
		public RobotAction getAction()
		{
			return action;
		}
		
		public int getDistance()
		{
			return distance;
		}
	}
	
	public static class RobotRule
	{
		private boolean on;
		private ArrayList<BeliefStates> type;
		private ArrayList<RobotDistanceAction> actions;
		private boolean onAppeared;
		private String title;

		public RobotRule(String _title)
		{
			title = _title;
			on = true;
			type = new ArrayList<BeliefStates>();
			onAppeared = true;
			actions = new ArrayList<RobotDistanceAction>();
		}
		
		public RobotRule(String _title, boolean _on, boolean _onAppeared)
		{
			title = _title;
			on = _on;
			onAppeared = _onAppeared;
			type = new ArrayList<BeliefStates>();
			actions = new ArrayList<RobotDistanceAction>();
		}
		
		public void addBelief(BeliefStates state)
		{
			if (!type.contains(state))
			{
				type.add(state);
			}
		}

		public boolean getEnabled()
		{
			return on;
		}
		
		public void setActions(ArrayList<RobotDistanceAction> rda)
		{
			actions = rda;
		}
		
		public ArrayList<RobotDistanceAction> getActions()
		{
			return actions;
		}

		public boolean getOnAppeared()
		{
			return onAppeared;
		}
		
		public boolean hasBelief(BeliefStates bs)
		{
			return type.contains(bs);
		}
		
		public void setTitle(String _title)
		{
			title = _title;
		}
		
		public String getTitle()
		{
			return title;
		}
	}

    private Robot robot;
	private String btAddress;
	private LinkedBlockingDeque<RobotDistanceAction> actions;
	private ConnectStatus status = ConnectStatus.DISCONNECTED;

	private ArrayList<RobotRule> rules;
	
	private BeliefSet state;
	private boolean obstacleChanged;
	private boolean pathChanged;
	private boolean waterChanged;

	private float objectDetected = 0.4f;
	private int blackMax = 50;
	private int waterMax = 100;
	private int speed = 10;
	private boolean running = false;
	
	public final static int WIDTH = 176;
    public final static int HEIGHT = 144;
    public final static int NUM_PIXELS = WIDTH * HEIGHT;
    public final static int BUFFER_SIZE = NUM_PIXELS * 2;
    private String ev3Output;

	private void updateBeliefs(float distance, Color colour)
	{
		boolean curObs = state.states.contains(BeliefStates.OBSTACLE);
		if (Float.compare(distance, objectDetected) < 0)
		{
			if (!state.states.contains(BeliefStates.OBSTACLE))
			{
				state.states.add(BeliefStates.OBSTACLE);
			}
		}
		else
		{
			if (state.states.contains(BeliefStates.OBSTACLE))
			{
				state.states.remove(BeliefStates.OBSTACLE);
			}
		}
		obstacleChanged = curObs != state.states.contains(BeliefStates.OBSTACLE);
		int red = colour.getRed();
		int blue = colour.getBlue();
		int green = colour.getGreen();

		//pathChanged = state.states.contains(BeliefStates.PATH) != (Float.compare(light, pathLight) < 0);
		boolean curPath = state.states.contains(BeliefStates.PATH);
		if ((red < blackMax) && (blue < blackMax) && (green < blackMax))
		{
			if (!state.states.contains(BeliefStates.PATH))
			{
				state.states.add(BeliefStates.PATH);
			}
		}
		else
		{
			if (state.states.contains(BeliefStates.PATH))
			{
				state.states.remove(BeliefStates.PATH);
			}
		}
		pathChanged = curPath != state.states.contains(BeliefStates.PATH);

		//waterChanged = state.states.contains(BeliefStates.WATER) != ((Float.compare(light, waterLightRange.x) > 0) && (Float.compare(light, waterLightRange.y) < 0));
		boolean curWater = state.states.contains(BeliefStates.WATER);
		if (((blue > green) && (blue > red)) && ((red < waterMax) && (blue < waterMax) && (green < waterMax)))
		{
			if (!state.states.contains(BeliefStates.WATER))
			{
				state.states.add(BeliefStates.WATER);
			}
		}
		else
		{
			if (state.states.contains(BeliefStates.WATER))
			{
				state.states.remove(BeliefStates.WATER);
			}
		}
		waterChanged = curWater != state.states.contains(BeliefStates.WATER);
	}

	private void checkRules()
	{
		for (int i = 0; i < rules.size(); i++)
		{
			RobotRule rule = rules.get(i);
			if (rule.getEnabled())
			{
				boolean triggered = false;
				if (rule.hasBelief(BeliefStates.OBSTACLE))
				{
					triggered = (rule.getOnAppeared() == state.states.contains(BeliefStates.OBSTACLE)) && obstacleChanged;
				}
				if (rule.hasBelief(BeliefStates.PATH))
				{
					triggered = (rule.getOnAppeared() == state.states.contains(BeliefStates.PATH)) && pathChanged;
				}
				if (rule.hasBelief(BeliefStates.WATER))
				{
					triggered = (rule.getOnAppeared() == state.states.contains(BeliefStates.WATER)) && waterChanged;
				}
				
				if(triggered)
				{
					actions.addAll(rule.getActions());
				}
			}
		}
	}

	private void doAction()
	{
		if (actions.peek() != null)
		{
			if (status == ConnectStatus.CONNECTED)
			{
				RobotDistanceAction action = actions.poll();
				switch (action.getAction())
				{
					case FORWARD:
						robot.forward();
						break;
					case FORWARD_BY:
						robot.forwardBy(action.getDistance());
						break;
					case STOP:
						robot.stop();
						break;
					case BACK_BY:
						robot.backwardBy(action.getDistance());
						break;
					case BACKWARD:
						robot.backward();
						break;
					case LEFT:
						robot.left();
						break;
					case LEFT_BY:
						robot.leftBy(action.getDistance());
						break;
					case RIGHT:
						robot.right();
						break;
					case RIGHT_BY:
						robot.rightBy(action.getDistance());
						break;
					case NOTHING:
						break;
					default:
						break;
				}
			}
		}
	}
	
	private void doRobot()
	{
		try
		{
			Socket s = new Socket(btAddress, 55555);
	    	BufferedInputStream is = new BufferedInputStream(s.getInputStream());
	    	BufferedOutputStream os = new BufferedOutputStream(s.getOutputStream());
	    	os.write(0);
	    	os.flush();
	    	byte[] frame = new byte[BUFFER_SIZE];
	    	int offset = 0;
	    	System.out.println("Ready for Video Frames");
			robot.connectToRobot(btAddress);
			connected();
			status = ConnectStatus.CONNECTED;
			float disInput;
			float[] rgb;
			int curSpeed = speed;
			int red;
			int blue;
			int green;
			
			while (status == ConnectStatus.CONNECTED)
			{
				
				disInput = robot.getuSensor().getSample();
				rgb = robot.getRGBSensor().getRGBSample();
				red = Math.min(255, (int)(rgb[0] * 850));
				green = Math.min(255, (int)(rgb[1] * 1026));
				blue = Math.min(255, (int)(rgb[2] * 1815));
				state.colour = new Color(red, green, blue);
				state.distance = disInput;
				updateBeliefs(disInput, state.colour);
				update(state);
				if (curSpeed != speed)
				{
					robot.setTravelSpeed(speed);
					curSpeed = speed;
				}
				if (running)
				{
					checkRules();
					doAction();
				}
				else
				{
					robot.stop();
					actions.clear();
				}
				offset = 0;
				newFrame(frame);
		    	os.write(0);
		    	os.flush();
		    	while (offset < BUFFER_SIZE)
		    	{
		    		offset += is.read(frame, offset, BUFFER_SIZE - offset);
		    	}
				update(state);
			}
			os.write(-1);
	    	os.flush();
	    	os.close();
	    	is.close();
	    	s.close();
	    	
			robot.close();
			status = ConnectStatus.DISCONNECTED;
			state.states.clear();
			state.colour = Color.BLACK;
			state.distance = 0;
			update(state);
			disconnected();
	    }
	    catch (Exception e)
	    {
	    	status = ConnectStatus.DISCONNECTING;
			if (robot != null && robot.isConnected())
			{
				robot.close();
			}
			status = ConnectStatus.DISCONNECTED;
			errorConnecting(e);
	    }
	}
	
    @Override
    public void run()
    {
		status = ConnectStatus.CONNECTING;
		ev3Output = "Uploading";
		EV3ScpUpload ev3Program = new EV3ScpUpload(btAddress, "/home/lejos/programs/EV3Video.jar", "/Users/joecollenette/workspace/VideoTest/VideoTest.jar", true, false)
		{
			@Override
			public void outputLine(String line) 
			{
				if(line.toLowerCase().contains("accepting"))
				{
					doRobot();
				}
				else if (line.toLowerCase().contains("exception"))
				{
					errorConnecting(new Exception("Video Error"));
				}
			}
			
		};
		new Thread(ev3Program).start();
			
    }

	public BluetoothRobot()
	{
		actions = new LinkedBlockingDeque<RobotDistanceAction>();
		rules = new ArrayList<RobotRule>();
		state = new BeliefSet();
		state.colour = Color.BLACK;
		robot = new Robot();
	}
	
	
	public int getNoOfRules()
	{
		return rules.size();
	}
	
	public RobotRule getRule(int i)
	{
		if (i != -1)
		{
			return rules.get(i);
		}
		else
		{
			return null;
		}
	}
	
	public void disconnect()
	{
		status = ConnectStatus.DISCONNECTING;
	}
	
	public void start()
	{
		running = true;
	}
	
	public void stop()
	{
		running = false;
	}
	
	public float getObsDistance()
	{
		return objectDetected;
	}
	
	public void setObsDistance(float distance)
	{
		objectDetected = distance;
	}
	
	public int getWaterMax()
	{
		return waterMax;
	}
	
	public void setWaterMax(int max)
	{
		waterMax = max;
	}
	
	public int getPathMax()
	{
		return blackMax;
	}
	
	public void setPathMax(int max)
	{
		blackMax = max;
	}
	
	public void moveRuleUp(int ruleToMove)
	{
		Collections.swap(rules, ruleToMove, ruleToMove - 1);
	}
	
	public void moveRuleDown(int ruleToMove)
	{
		Collections.swap(rules, ruleToMove, ruleToMove + 1);
	}
	
	public void addNewRule(RobotRule rule)
	{
		rules.add(rule);
	}
	
	public void changeRule(RobotRule rule, int i)
	{
		rules.set(i, rule);
	}
	
	public void removeRule(int i)
	{
		rules.remove(i);
	}

	public void setBTAddress(String address)
	{
		btAddress = address;
	}

	public void changeSettings(float objectRange, int blackMaximum, int waterMaximum)
	{
		objectDetected = objectRange;
		blackMax = blackMaximum;
		waterMax = waterMaximum;
	}
	
	public abstract void update(BeliefSet state);
	
	public abstract void connected();
	
	public abstract void disconnected();
	
	public abstract void errorConnecting(Exception e);
	
	public abstract void newFrame(byte[] frame);
}
