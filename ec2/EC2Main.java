package ec2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import com.amazonaws.services.ec2.model.Instance;

public class EC2Main {

	static void Sleep(int ms)
	{
		try{
		Thread.sleep(ms);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	};
	static EC2Handler ec2;
	static EC2gui gui;

	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub	
		ec2 = new EC2Handler();
		gui = new EC2gui();

		gui.setVisible(true);
		/// When drop-down is clicked, fetch state and data for given instance.
		gui.instancesListDropDown.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) 
			{	
				String cmd = e.getActionCommand();
		//		e.
				System.out.println("Action performed: "+cmd);
				System.out.println("drop-down list uhhh: "+gui.selectedInstanceId);
			}}
		);
		gui.instancesListDropDown.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
				if (arg0.getStateChange() == ItemEvent.SELECTED)
				{
					gui.selectedInstanceId = gui.instancesListDropDown.getSelectedItem().toString();
					OnSelectedInstanceUpdated();
				}				
			}}
		);
		

		gui.refreshListButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				ec2.getInstances(); // Fetch instances, listener should automatically update the list.
			}}
		);
		gui.createButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				// Create security group first if needed.
				String secGroup = gui.textFieldSecurityGroupName.getText();
				ec2.createSecurityGroup(secGroup);
				ec2.defaultSecurityGroupName = secGroup;
				Instance inst = ec2.createInstance();
				/// Update the main list?
				ec2.getInstances();
				// Set it as the current one?
				gui.selectedInstanceId = inst.getInstanceId();
				OnSelectedInstanceUpdated();
				
			}}
		);
		gui.startButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				ec2.StartInstance(gui.selectedInstanceId);
			}}
		);
		gui.stopButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				ec2.StopInstance(gui.selectedInstanceId);
			}}
		);
		gui.terminateButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				ec2.TerminateInstance(gui.selectedInstanceId);
			}}
		);
		
		ec2.addInstanceListener(new InstanceListener()
		{
			@Override
			public void OnInstanceListUpdated(List<Instance> instances) 
			{
				System.out.println("instances: "+instances);
				// Save list for updating it later..?
				System.out.println("OnInstanceListUpdated");
				Object selectedObj = gui.instancesListDropDown.getSelectedItem();
				System.out.println("selectedObj "+selectedObj);
				String selected = selectedObj != null? selectedObj.toString() : "";

				System.out.println("selectedStr "+selected);
				gui.instancesListDropDown.removeAllItems();
				System.out.println("Remove all items ");
				System.out.println("instances: "+instances);
				// Add new ones.
				for (int i = 0; i < instances.size(); ++i)
				{
					System.out.println("Adding item");
					String id = instances.get(i).getInstanceId();
					gui.instancesListDropDown.addItem(id);
					if (id.equals(selected)){ // Found old one? Re-select it.
						gui.instancesListDropDown.setSelectedItem(id);
					}
				}
			}

			@Override
			public void OnInstanceStateUpdated(String instanceId) {
				// TODO Auto-generated method stub
				if (instanceId.equals(gui.selectedInstanceId))
				{
					gui.SetInstanceState(ec2.getInstanceStatusStr(instanceId)); // Update state string?
				}
			}	
		});
		
		/// Fetch initial list?
		ec2.getInstances();
		
	}
	protected static void OnSelectedInstanceUpdated() 
	{
		gui.selectedInstanceLabel.setText(gui.selectedInstanceId);
		gui.SetInstanceState(ec2.getInstanceStatusStr(gui.selectedInstanceId));
		gui.SetInstanceImageID(ec2.GetInstanceName(gui.selectedInstanceId));
		gui.SetInstanceSecurityGroup(ec2.GetInstanceSecurityGroup(gui.selectedInstanceId));
	}
	private static void TestCreateAndDeleteInstance() {
		// TODO Auto-generated method stub
		Instance inst = ec2.createInstance();

		ec2.displayInstances();
		ec2.displayEndpoints();

		// Create & Start
		ec2.PrintState(inst);
		ec2.SleepUntilInstance(inst, State.Running);

		// Stop it.
		ec2.StopInstance(inst);
		ec2.PrintState(inst);
		ec2.SleepUntilInstance(inst, State.Stopped);
		
		/// Terminate it?
		ec2.TerminateInstance(inst);
		ec2.PrintState(inst);
		ec2.SleepUntilInstance(inst, State.Terminated);
		
		// TODO need to wait
		ec2.deleteSecurityGroup("muchsecurityverywow");		
	}

}
