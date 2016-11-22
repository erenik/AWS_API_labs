package ec2;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import ui.Graph;

public class EC2gui extends JFrame 
{
	public JPanel contentPane;
	public JTextField securityGroupName, ip, port,
		content, period;

	public JButton createButton, startButton, stopButton, terminateButton,
		refreshListButton;
	public JComboBox instancesListDropDown;
	public JLabel selectedInstanceLabel;
	public JLabel instanceState;
	public String selectedInstanceId = "";
	public JTextField textFieldSecurityGroupName;

	private JLabel instanceImageId, instanceSecurityGroup;
	
	public Graph graphCpu, graphDataIn, graphMemoryUsage;
	
	/**
	 * Create the frame.
	 */

	/// For dynamic creation.
	Rectangle bounds = null;
	List<JComponent> comps = new ArrayList<JComponent>();

	public EC2gui() 
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 650, 500);
		
		// Inherit bounds.
		bounds = getBounds();
		bounds.x = bounds.y = 0;
		bounds.width -= 20; // Windows Ui takes some space.
		bounds.height -= 20;
		
		setTitle("EC2 Control panel");
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
				
		int defaultButtonHeight = 32;
		
		comps.add(new JLabel("Security Group: "));
		textFieldSecurityGroupName = new JTextField("VerySecureAwesomeYes");
		comps.add(textFieldSecurityGroupName);
		createButton = new JButton("Create instance");
		comps.add(createButton);
		MakeListHorizontalPixels(comps, defaultButtonHeight);

		// Drop-down menu
		comps.add(new JLabel("Select instance: "));
		instancesListDropDown = new JComboBox();
		comps.add(instancesListDropDown);
		refreshListButton = new JButton("Refresh");
		comps.add(refreshListButton);
		MakeListHorizontalPixels(comps, defaultButtonHeight);
		
		/// Selected instance
		JLabel j = new JLabel("Instance ID: ");
		comps.add(j);
		selectedInstanceLabel = new JLabel("Selected instance");
		comps.add(selectedInstanceLabel);
		comps.add(new JLabel("State: "));
		instanceState = new JLabel("state");
		comps.add(instanceState);
		
		MakeListHorizontalPixels(comps, defaultButtonHeight);
		
		comps.add(new JLabel("Image ID: "));
		instanceImageId = new JLabel("");
		comps.add(instanceImageId);
		comps.add(new JLabel("Security group: "));
		instanceSecurityGroup = new JLabel("Securit");
		comps.add(instanceSecurityGroup);
		
		MakeListHorizontalPixels(comps, defaultButtonHeight);
		
		// State
		startButton = new JButton("Start");
		comps.add(startButton);
		stopButton = new JButton("Stop");
		comps.add(stopButton);
		terminateButton = new JButton("Terminate");
		comps.add(terminateButton);

		MakeListHorizontalPixels(comps, defaultButtonHeight);
		
		
		graphCpu = new Graph("CPU", "Time", "%");
		comps.add(graphCpu);
		graphCpu.SetMinMaxY(0, 100);
		
		graphMemoryUsage = new Graph("Memory usage", "Time", "%");
		graphMemoryUsage.SetMinMaxY(0, 100);
		comps.add(graphMemoryUsage);
		
		graphDataIn = new Graph("Data in", "Time", "MB/s");
		comps.add(graphDataIn);
		MakeListHorizontalPixels(comps, 256);
		
		/// List of instances (drop-down list).
		/// - On select, show IP, state
		/// - Buttons to Create/Stop/Terminate selected 
		/// Cloud-watch details below.
		
	}
	private void MakeListHorizontalPixels(List<JComponent> comps2, int heightPixelsToUse) 
	{
		int width = bounds.width;
		int height = heightPixelsToUse;
		int cellHeight = height;
		int cellWidth = width / (comps.size());
		int heightUsed = 0, widthUsed = 0;
		for (int i = 0; i < comps.size(); ++i)
		{
			JComponent comp = comps.get(i);
			int x = bounds.x + i * cellWidth, 
				y = bounds.y;
			System.out.println("x: "+x+" y: "+y+" cellWidth: "+cellWidth+" cellHeight: "+cellHeight);
			comp.setBounds(x, y, cellWidth, cellHeight);
			contentPane.add(comp);
		}
		heightUsed = cellHeight;
		bounds.x += widthUsed;
		bounds.y += heightUsed;
		comps.clear();		
	}
	void MakeListHorizontal(List<JComponent> comps, float ratioHeightToUse)
	{
		int height = (int) (bounds.height * ratioHeightToUse);
		MakeListHorizontalPixels(comps, height);
	}
	void MakeListVertical(List<JComponent> comps, float ratioHeightToUse)
	{
		int height = (int) (bounds.height * ratioHeightToUse);
		MakeListVerticalPixels(comps, height);
	}
	void MakeListVerticalPixels(List<JComponent> comps, int pixelHeightToUse)
	{
		int width = bounds.width;
		int height = pixelHeightToUse;
		int cellHeight = height / (comps.size());
		int cellWidth = width;
		int heightUsed = 0, widthUsed = 0;
		for (int i = 0; i < comps.size(); ++i)
		{
			JComponent comp = comps.get(i);
			int x = bounds.x, y = bounds.y + i * cellHeight;
			System.out.println("x: "+x+" y: "+y+" cellWidth: "+cellWidth+" cellHeight: "+cellHeight);
			comp.setBounds(x, y, cellWidth, cellHeight);
			heightUsed += cellHeight;
			widthUsed += 0; // Vertical list, ignore width
			contentPane.add(comp);
		}
		bounds.x += widthUsed;
		bounds.y += heightUsed;
		comps.clear();
	}
	public void SetInstanceState(String instanceStatusStr) {
		// TODO Auto-generated method stub
		instanceState.setText(instanceStatusStr);
		
	}
	public void SetInstanceImageID(String imageId) 
	{
		instanceImageId.setText(imageId);
	}
	public void SetInstanceSecurityGroup(String s) 
	{
		instanceSecurityGroup.setText(s);
	}
}
