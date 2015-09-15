package ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JList;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JComboBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;

import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ev3.BluetoothRobot;
import ev3.BluetoothRobot.BeliefSet;
import ev3.BluetoothRobot.BeliefStates;
import ev3.BluetoothRobot.RobotRule;

import javax.swing.JProgressBar;

public class Main 
{
	class JimgPanel extends JPanel
	{
		private static final long serialVersionUID = 8818722395067165003L;
		BufferedImage img = new BufferedImage(BluetoothRobot.WIDTH, BluetoothRobot.HEIGHT, BufferedImage.TYPE_INT_RGB);
		int y1;
		int y2;
		int u;
		int v;
		int rgb1;
		int rgb2;
		int x;
		int y;
		int x2;
		
		private int convertYUVtoARGB(int y, int u, int v)
		{
	        int c = y - 16;
	        int d = u - 128;
	        int e = v - 128;
	        int r = (298*c+409*e+128)/256;
	        int g = (298*c-100*d-208*e+128)/256;
	        int b = (298*c+516*d+128)/256;
	        r = r>255? 255 : r<0 ? 0 : r;
	        g = g>255? 255 : g<0 ? 0 : g;
	        b = b>255? 255 : b<0 ? 0 : b;
	        return 0xff000000 | (r<<16) | (g<<8) | b;
	    }
		
		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			g.drawImage(img.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH), 0, 0, null);
		}
		public void updateFrame(byte[] newFrame)
		{
			for(int i=0; i< BluetoothRobot.BUFFER_SIZE; i+=4) 
	    	{
	            y1 = newFrame[i] & 0xFF;
	            y2 = newFrame[i+2] & 0xFF;
	            u = newFrame[i+1] & 0xFF;
	            v = newFrame[i+3] & 0xFF;
	            rgb1 = convertYUVtoARGB(y1,u,v);
	            rgb2 = convertYUVtoARGB(y2,u,v);
	            //g.setColor(new Color(rgb1));
	            x = (i % (BluetoothRobot.WIDTH * 2)) / 2;
	            x2 = (i % (BluetoothRobot.WIDTH * 2)) / 2 + 1;
	            y = i / (BluetoothRobot.WIDTH * 2);
	            //g.drawLine(x, y, x, y);
	            //g.setColor(new Color(rgb2));
	            //g.drawLine(x + 1, y, x + 1, y);
	            img.setRGB(x, y, rgb1);
	            img.setRGB(x2, y, rgb2);
	    	}
			
			revalidate();
			repaint();
		}
	}
	
	private static BluetoothRobot btRobot;
	private static Thread btThread;

	private JFrame frame;
	
	private final JPanel pnlRules = new JPanel();
	private final JPanel pnlMain = new JPanel();
	private JPanel pnlConnect = new JPanel();
	private JPanel pnlStatus = new JPanel();
	private Settings pnlSettings;
	
	private JButton btnConnect = new JButton("Connect");
	private JProgressBar progressBar = new JProgressBar();
	private JTextField txtBTAddress = new JTextField();
	
	private JLabel lblRules = new JLabel("Rules");
	private JScrollPane scrRules = new JScrollPane();
	private DefaultListModel<String> lstRules = new DefaultListModel<String>();
	private JList<String> jlstRules = new JList<String>(lstRules);
	private JButton btnAddRule = new JButton("Add");
	private JButton btnDelRule = new JButton("Delete");
	private JButton btnRuleUp = new JButton("Up");
	private JButton btnRuleDown = new JButton("Down");
	
	private JLabel lblRuleName = new JLabel("Rule Name");
	private JTextField txtRuleName = new JTextField();
	private JLabel lblFollowing = new JLabel("When the following have ");
	private JComboBox<String> cboAppeared = new JComboBox<String>();
	private JPanel pnlBeliefs = new JPanel();
	private JCheckBox chkObstacle = new JCheckBox("Obstacle");
	private JCheckBox chkWater = new JCheckBox("Water");
	private	JCheckBox chkPath = new JCheckBox("Path");
	private JButton btnAddAction = new JButton("Add Action");
	private JScrollPane scrActions = new JScrollPane();
	private JActionPanel pnlActions = new JActionPanel();
	private JButton btnConfirm = new JButton("Set Change");
	
	private JimgPanel pnlVideo = new JimgPanel();
	private JButton btnSettings = new JButton("Settings");
	private JButton btnDoRules = new JButton("Start Robot");
	private JLabel lblBeliefs = new JLabel("New label");

	public static void main(String[] args) {
		
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() 
			{
				if (btThread != null)
				{
					btRobot.disconnect();
					while (btThread.isAlive()){}
				}
			}
		});
	}
	
	public Main() 
	{
		btRobot = new BluetoothRobot()
		{
			private String beliefs = "Distance - %f Colour - %d %d %d Beliefs - ";
			
			@Override
			public void update(BeliefSet state) 
			{
				lblBeliefs.setText(String.format(beliefs, state.distance, state.colour.getRed(), state.colour.getGreen(), state.colour.getBlue()) + state.states.toString());
			}

			@Override
			public void connected() 
			{
				btnDoRules.setEnabled(true);
				progressBar.setVisible(false);
				btnConnect.setEnabled(true);
				btnConnect.setText("Disconnect");
			}

			@Override
			public void disconnected() 
			{	
				btnDoRules.setEnabled(false);
				progressBar.setVisible(false);
				btnConnect.setEnabled(true);
				btnConnect.setText("Connect");
			}

			@Override
			public void errorConnecting(Exception e) 
			{
				System.out.println(e);
				progressBar.setVisible(false);
				btnConnect.setEnabled(true);
			}
			
			@Override
			public void newFrame(byte[] frame) 
			{
				pnlVideo.updateFrame(frame);
			}
		};
		initialize();
	}

	private void initialize() 
	{
		frame = new JFrame();
		frame.setMinimumSize(new Dimension(800, 700));
		frame.setBounds(100, 100, 800, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		pnlSettings = new Settings(frame);
		
		frame.getContentPane().add(setUpConnectPanel(), BorderLayout.NORTH);
		frame.getContentPane().add(setUpMainPanel(), BorderLayout.CENTER);
		frame.getContentPane().add(setUpRulesPanel(), BorderLayout.WEST);
		frame.getContentPane().add(setUpStatusPanel(), BorderLayout.SOUTH);
		
		setUpResizeEvents();
		setUpRuleActions();
		
		lstRules.addElement("NEW RULE");
		btRobot.addNewRule(new RobotRule("NEW RULE"));
		jlstRules.setSelectedIndex(0);
		btnDoRules.setEnabled(false);
		
		btRobot.update(new BeliefSet());
		
		btnConnect.addActionListener(new ActionListener()
		{			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				progressBar.setVisible(true);
				if (btnConnect.getText().equals("Connect"))
				{
					btRobot.setBTAddress(txtBTAddress.getText());
					btThread = new Thread(btRobot);
					btThread.start();
				}
				else
				{
					btRobot.disconnect();
				}
				btnConnect.setEnabled(false);
			}
		});
		
		btnAddAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				pnlActions.addAction();
			}
	
		});
		
		btnConfirm.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				lstRules.setElementAt(txtRuleName.getText(), jlstRules.getSelectedIndex());
				RobotRule r = new RobotRule(txtRuleName.getText(), true, cboAppeared.getSelectedIndex() == 0);
				if (chkObstacle.isSelected())
				{
					r.addBelief(BeliefStates.OBSTACLE);
				}
				if (chkWater.isSelected())
				{
					r.addBelief(BeliefStates.WATER);
				}
				if (chkPath.isSelected())
				{
					r.addBelief(BeliefStates.PATH);
				}
				r.setActions(pnlActions.getActions());
				btRobot.changeRule(r, jlstRules.getSelectedIndex());
			}
		});
		
		btnSettings.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				pnlSettings.setValues(btRobot.getObsDistance(), btRobot.getPathMax(), btRobot.getWaterMax());
				pnlSettings.makeVisible();
				while (pnlSettings.isVisible())
				{
					
				}
				if(pnlSettings.okClicked())
				{
					btRobot.changeSettings(pnlSettings.getObsValue(), pnlSettings.getPathValue(), pnlSettings.getWater());
				}
			}
	
		});
		
		btnDoRules.addActionListener(new ActionListener()
		{
			boolean curEnabled = true;
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				curEnabled = !curEnabled;
				jlstRules.setEnabled(curEnabled);
				btnAddRule.setEnabled(curEnabled);
				btnDelRule.setEnabled(curEnabled);
				btnRuleUp.setEnabled(curEnabled);
				btnRuleDown.setEnabled(curEnabled);
				
				btnAddAction.setEnabled(curEnabled);
				txtRuleName.setEnabled(curEnabled);
				cboAppeared.setEnabled(curEnabled);
				chkObstacle.setEnabled(curEnabled);
				chkWater.setEnabled(curEnabled);
				chkPath.setEnabled(curEnabled);
				pnlActions.setEnabled(!pnlActions.isEnabled());
				
				btnConfirm.setEnabled(curEnabled);
				btnSettings.setEnabled(curEnabled);
				if(curEnabled)
				{
					btnDoRules.setText("Start Robot");
					btRobot.stop();
				}
				else
				{
					btnDoRules.setText("Stop Robot");
					btRobot.start();
				}				
			}
			
		});
	}
	
	private void swapRules(int i, int j)
	{
		String tmp = lstRules.getElementAt(i);
		lstRules.setElementAt(lstRules.getElementAt(j), i);
		lstRules.setElementAt(tmp, j);
	}
	
	private void setRuleButtons()
	{
		btnDelRule.setEnabled(lstRules.size() > 1);
		btnRuleUp.setEnabled(lstRules.size() > 1 && jlstRules.getSelectedIndex() != 0);
		btnRuleDown.setEnabled(lstRules.size() > 1 && jlstRules.getSelectedIndex() != lstRules.size() - 1);
	}
	
	private void setUpRuleActions() 
	{
		btnAddRule.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				lstRules.addElement("NEW RULE");
				btRobot.addNewRule(new RobotRule("NEW RULE"));
				setRuleButtons();
			}
			
		});
		
		btnDelRule.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				int i = jlstRules.getSelectedIndex();
				btRobot.removeRule(i);
				lstRules.remove(i);
				if (i != 0)
				{
					jlstRules.setSelectedIndex(i - 1);
				}
				else
				{
					jlstRules.setSelectedIndex(i);
				}
				setRuleButtons();
			}
			
		});
		
		btnRuleUp.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				btRobot.moveRuleUp(jlstRules.getSelectedIndex());
				swapRules(jlstRules.getSelectedIndex(), jlstRules.getSelectedIndex() - 1);
				jlstRules.setSelectedIndex(jlstRules.getSelectedIndex() - 1);
				setRuleButtons();
			}
			
		});
		
		btnRuleDown.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				btRobot.moveRuleDown(jlstRules.getSelectedIndex());
				swapRules(jlstRules.getSelectedIndex(), jlstRules.getSelectedIndex() + 1);
				jlstRules.setSelectedIndex(jlstRules.getSelectedIndex() + 1);
				setRuleButtons();
			}
			
		});
		
		jlstRules.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e) 
			{
				setRuleButtons();		
				updateView();
			}
		});
		
	}

	private void updateView() 
	{
		if (jlstRules.getSelectedIndex() != -1)
		{
			RobotRule rule = btRobot.getRule(jlstRules.getSelectedIndex());
			txtRuleName.setText(rule.getTitle());
			cboAppeared.setSelectedIndex(rule.getOnAppeared() ? 0 : 1);
			
			chkObstacle.setSelected(rule.hasBelief(BeliefStates.OBSTACLE));
			chkPath.setSelected(rule.hasBelief(BeliefStates.PATH));
			chkWater.setSelected(rule.hasBelief(BeliefStates.WATER));
			
			pnlActions.clear();
			pnlActions.addActions(rule.getActions());
		}
	}

	private JPanel setUpMainPanel()
	{
		pnlMain.setLayout(null);
		
		lblRuleName.setBounds(109, 6, 67, 16);
		pnlMain.add(lblRuleName);
		
		txtRuleName.setBounds(180, 0, 150, 28);
		pnlMain.add(txtRuleName);
		txtRuleName.setColumns(10);
		
		lblFollowing.setBounds(241, 16, 173, 16);
		pnlMain.add(lblFollowing);
		
		cboAppeared.addItem("Appeared");
		cboAppeared.addItem("Disappeared");
		cboAppeared.setBounds(241, 16, 150, 27);
		pnlMain.add(cboAppeared);
		
		pnlBeliefs.setBounds(0, 46, 550, 33);
		pnlBeliefs.setBorder(null);
		pnlMain.add(pnlBeliefs);
		pnlBeliefs.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		pnlBeliefs.add(chkObstacle);
		
		pnlBeliefs.add(chkWater);
		
		pnlBeliefs.add(chkPath);
		
		btnAddAction.setBounds(0, 0, 114, 29);
		pnlMain.add(btnAddAction);
		
		scrActions.setBounds(0, 79, 550, 386);
		scrActions.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrActions.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		pnlMain.add(scrActions);		
		
		scrActions.setViewportView(pnlActions);
		pnlActions.setLayout(null);
		
		btnConfirm.setBounds(241, 470, 114, 29);
		pnlMain.add(btnConfirm);
		return pnlMain;
	}
	
	private JPanel setUpRulesPanel()
	{
		
		pnlRules.setPreferredSize(new Dimension(150, 10));
		pnlRules.setLayout(null);		
		
		lblRules.setBounds(0, 0, 150, 16);
		pnlRules.add(lblRules);
		lblRules.setHorizontalAlignment(SwingConstants.CENTER);
		
		scrRules.setBounds(0, 16, 150, 400);
		pnlRules.add(scrRules);
		
		scrRules.add(jlstRules);
		scrRules.setViewportView(jlstRules);
		jlstRules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		btnAddRule.setBounds(0, 416, 75, 29);
		pnlRules.add(btnAddRule);
		
		btnDelRule.setBounds(75, 416, 75, 29);
		btnDelRule.setEnabled(false);
		pnlRules.add(btnDelRule);
		
		btnRuleUp.setBounds(0, 445, 75, 29);
		btnRuleUp.setEnabled(false);
		pnlRules.add(btnRuleUp);
		
		btnRuleDown.setBounds(75, 445, 75, 29);
		btnRuleDown.setEnabled(false);
		pnlRules.add(btnRuleDown);
		return pnlRules;
	}
	
	private JPanel setUpConnectPanel()
	{
		
		JLabel lblBluetoothAddress = new JLabel("Bluetooth Address");
		pnlConnect.add(lblBluetoothAddress);
		
		
		txtBTAddress.setPreferredSize(new Dimension(150, 28));
		txtBTAddress.setText("10.0.1.1");
		txtBTAddress.addFocusListener(new FocusListener()
		{
			private String prev;
			@Override
			public void focusGained(FocusEvent e) 
			{
				prev = ((JTextField)e.getSource()).getText();
				
			}

			@Override
			public void focusLost(FocusEvent e) 
			{
				if (!((JTextField)e.getSource()).getText().matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))
				{
					((JTextField)e.getSource()).setText(prev);
				}
			}
		});
		pnlConnect.add(txtBTAddress);
		
		pnlConnect.add(btnConnect);
		
		progressBar.setIndeterminate(true);
		pnlConnect.add(progressBar);
		progressBar.setVisible(false);
		
		return pnlConnect;
	}
	
	private JPanel setUpStatusPanel()
	{
		FlowLayout flowLayout = (FlowLayout) pnlStatus.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		
		pnlVideo = new JimgPanel();
		pnlVideo.setPreferredSize(new Dimension(BluetoothRobot.WIDTH, BluetoothRobot.HEIGHT));
		pnlStatus.add(pnlVideo);
		
		pnlStatus.add(btnSettings);
		pnlStatus.add(btnDoRules);
		
		pnlStatus.add(lblBeliefs);
		return pnlStatus;
	}
	
	private void setUpResizeEvents()
	{
		pnlRules.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				int width = pnlRules.getWidth();
				int height = pnlRules.getHeight();
				
				lblRules.setBounds(0, 0, width, 16);
				scrRules.setBounds(0, 16, width, height - 74);
				btnAddRule.setBounds(0, height - 58, 75, 29);
				btnDelRule.setBounds(75, height - 58, 75, 29);
				btnRuleUp.setBounds(0, height - 29, 75, 29);
				btnRuleDown.setBounds(75, height - 29, 75, 29);
				
			}
		});
		
		pnlMain.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				int width = pnlMain.getWidth();
				int height = pnlMain.getHeight();
				
				int x = width / 2;
				lblRuleName.setBounds(x - 110, 6, 67, 16);
				txtRuleName.setBounds(x - 40, 0, 150, 28);
				
				lblFollowing.setBounds(x - 163, 33, 173, 16);
				cboAppeared.setBounds(x + 3, 30, 150, 27);
				
				pnlBeliefs.setBounds(0, 49, width, 33);
				
				btnAddAction.setBounds(x - 58, 90, 117, 29);
				
				scrActions.setBounds(0, 122, width, height - 151);
				btnConfirm.setBounds(x - 56, height - 29, 114, 29);
			}
		});
	
	}
}
