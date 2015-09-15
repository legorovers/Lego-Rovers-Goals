package ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import ev3.BluetoothRobot.RobotAction;
import ev3.BluetoothRobot.RobotDistanceAction;

public abstract class JActionItem extends JPanel implements ItemListener, ActionListener
{
	private static final long serialVersionUID = 2816654093831339646L;
	private final JLabel lblDo = new JLabel("Then do");
	private final JSpinner spnValue = new JSpinner();
	private final JButton btnRemove = new JButton("Remove");
	private final JComboBox<String> comboBox = new JComboBox<String>();
	
	public JActionItem(RobotAction action, int distance)
	{
		super();
		add(lblDo);
		
		int index = 0;
		for (int i = 0; i < RobotAction.values().length; i++)
		{
			if (RobotAction.values()[i] == action)
			{
				index = i;
			}
			comboBox.addItem(RobotAction.values()[i].toString().replace('_', ' '));
		}
		comboBox.addItemListener(this);
		comboBox.setSelectedIndex(index);
		add(comboBox);
		
		spnValue.setPreferredSize(new Dimension(100, 28));
		spnValue.setModel(new SpinnerNumberModel(10, 0, 100, 1));
		spnValue.setValue(distance);
		add(spnValue);
		
		btnRemove.addActionListener(this);
		add(btnRemove);
		itemStateChanged(null);
	}
	
	public RobotDistanceAction asAction()
	{
		return new RobotDistanceAction(RobotAction.values()[comboBox.getSelectedIndex()], (int)spnValue.getValue());
	}

	@Override
	public void itemStateChanged(ItemEvent e) 
	{
		boolean show = ((String)comboBox.getSelectedItem()).contains("BY");
		spnValue.setVisible(show);
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		removeRequested(this);
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		comboBox.setEnabled(enabled);
		spnValue.setEnabled(enabled);
		btnRemove.setEnabled(enabled);
	}
	
	public abstract void removeRequested(JActionItem toRemove);
}
