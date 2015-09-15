package ui;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import ev3.BluetoothRobot.RobotAction;
import ev3.BluetoothRobot.RobotDistanceAction;

public class JActionPanel extends JPanel implements ComponentListener
{
	private static final long serialVersionUID = 3918117355903553231L;
	private ArrayList<JActionItem> actions = new ArrayList<JActionItem>();
	private final static int itemSize = 50;
	
	private void removePanel(JActionItem toRemove)
	{
		actions.remove(toRemove);
		remove(toRemove);
		componentResized(null);
	}
	
	private JActionItem newActionItem(RobotAction ra, int d)
	{
		return new JActionItem(ra, d)
		{
			private static final long serialVersionUID = 4144680612101722604L;

			@Override
			public void removeRequested(JActionItem toRemove)
			{
				removePanel(toRemove);
			}
		};

	}
	
	public JActionPanel()
	{
		super();
		setLayout(null);
		addComponentListener(this);
	}
	
	public ArrayList<RobotDistanceAction> getActions()
	{
		ArrayList<RobotDistanceAction> tmp = new ArrayList<RobotDistanceAction>();
		for(int i = 0; i < actions.size(); i++)
		{
			tmp.add(actions.get(i).asAction());
		}
		return tmp;
	}
	
	public void addAction()
	{
		JActionItem tmp = newActionItem(RobotAction.NOTHING, 10);		
		actions.add(tmp);
		int y = (actions.size() - 1) * itemSize; 
		tmp.setBounds(0, y, getWidth(), itemSize);
		add(tmp);
		setPreferredSize(new Dimension(getWidth(), actions.size() * itemSize));
		tmp.repaint();
		revalidate();
	}
	
	@Override
	public void componentResized(ComponentEvent e)
	{
		for (int i = 0; i < actions.size(); i++)
		{
			int y = i * itemSize; 
			actions.get(i).setBounds(0, y, getWidth(), itemSize);
		}
	}

	@Override
	public void componentMoved(ComponentEvent e) 
	{
		System.out.println("MOVED");
	}

	@Override
	public void componentShown(ComponentEvent e) 
	{
		System.out.println("SHOWN");
	}

	@Override
	public void componentHidden(ComponentEvent e) 
	{
		System.out.println("HIDDEN");
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		for(JActionItem i : actions)
		{
			i.setEnabled(enabled);
		}
	}

	public void clear() 
	{
		actions.clear();
		removeAll();
		repaint();
	}

	public void addActions(ArrayList<RobotDistanceAction> rda) 
	{
		for(RobotDistanceAction r : rda)
		{
			JActionItem tmp = newActionItem(r.getAction(), r.getDistance());
			actions.add(tmp);
			add(tmp);
			tmp.repaint();
		}
		componentResized(null);
		setPreferredSize(new Dimension(getWidth(), actions.size() * itemSize));
		revalidate();
	}
}
