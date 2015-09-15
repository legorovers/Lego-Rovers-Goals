package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;

public class Settings extends JDialog implements ActionListener
{

	private static final long serialVersionUID = 2334249660132607475L;
	private final JPanel contentPanel = new JPanel();
	private JSpinner spnObstacle;
	private JSpinner spnPath;
	private JSpinner spnWater; 
	private boolean okClicked;
	private JButton okButton = new JButton("OK");
	
	/**
	 * Create the dialog.
	 */
	public Settings(Window win) {
		super(win, "Settings", ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setBounds(100, 100, 339, 158);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {0, 0, 0};
		gbl_contentPanel.rowHeights = new int[] {0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, 0.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblObstacleDetection = new JLabel("Obstacle Detection");
			GridBagConstraints gbc_lblObstacleDetection = new GridBagConstraints();
			gbc_lblObstacleDetection.anchor = GridBagConstraints.WEST;
			gbc_lblObstacleDetection.insets = new Insets(0, 0, 5, 5);
			gbc_lblObstacleDetection.gridx = 1;
			gbc_lblObstacleDetection.gridy = 0;
			contentPanel.add(lblObstacleDetection, gbc_lblObstacleDetection);
		}
		{
			spnObstacle = new JSpinner(new SpinnerNumberModel(0.1, 0.0, 2.5, 0.1));
			spnObstacle.setMinimumSize(new Dimension(100, 28));
			spnObstacle.setPreferredSize(new Dimension(100, 28));
			GridBagConstraints gbc_spinner = new GridBagConstraints();
			gbc_spinner.insets = new Insets(0, 0, 5, 0);
			gbc_spinner.anchor = GridBagConstraints.NORTHWEST;
			gbc_spinner.gridx = 2;
			gbc_spinner.gridy = 0;
			contentPanel.add(spnObstacle, gbc_spinner);
		}
		{
			JLabel lblPathMaximum = new JLabel("Path Maximum");
			GridBagConstraints gbc_lblPathMaximum = new GridBagConstraints();
			gbc_lblPathMaximum.insets = new Insets(0, 0, 5, 5);
			gbc_lblPathMaximum.gridx = 1;
			gbc_lblPathMaximum.gridy = 1;
			contentPanel.add(lblPathMaximum, gbc_lblPathMaximum);
		}
		{
			spnPath = new JSpinner(new SpinnerNumberModel(50, 0, 255, 1));
			spnPath.setMinimumSize(new Dimension(100, 28));
			GridBagConstraints gbc_spinner = new GridBagConstraints();
			gbc_spinner.insets = new Insets(0, 0, 5, 0);
			gbc_spinner.gridx = 2;
			gbc_spinner.gridy = 1;
			contentPanel.add(spnPath, gbc_spinner);
		}
		{
			JLabel lblWaterMaximum = new JLabel("Water Maximum");
			GridBagConstraints gbc_lblWaterMaximum = new GridBagConstraints();
			gbc_lblWaterMaximum.insets = new Insets(0, 0, 0, 5);
			gbc_lblWaterMaximum.gridx = 1;
			gbc_lblWaterMaximum.gridy = 2;
			contentPanel.add(lblWaterMaximum, gbc_lblWaterMaximum);
		}
		{
			spnWater = new JSpinner(new SpinnerNumberModel(100, 0, 255, 1));
			spnWater.setMinimumSize(new Dimension(100, 28));
			GridBagConstraints gbc_spinner = new GridBagConstraints();
			gbc_spinner.gridx = 2;
			gbc_spinner.gridy = 2;
			contentPanel.add(spnWater, gbc_spinner);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				
				okButton.addActionListener(this);
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(this);
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public void setValues(float obs, int path, int water)
	{
		spnObstacle.setValue(obs);
		spnPath.setValue(path);
		spnWater.setValue(water);
	}
	
	public float getObsValue()
	{
		return ((Double) spnObstacle.getValue()).floatValue();
	}
	
	public int getPathValue()
	{
		return (int) spnPath.getValue();
	}
	
	public int getWater()
	{
		return (int) spnWater.getValue();
	}
	
	public void makeVisible()
	{
		setOk(false);
		setVisible(true);	
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		setOk(e.getSource().equals(okButton));
		setVisible(false);	
	}
	
	public boolean okClicked()
	{
		return okClicked;
	}
	
	private void setOk(Boolean ok)
	{
		okClicked = ok;
	}

}
