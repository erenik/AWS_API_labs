package ec2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupResult;
import com.amazonaws.services.ec2.model.DescribeHostsResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.Host;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class EC2Handler {
	
	AmazonEC2 ec2;

	public String defaultSecurityGroupName = "muchsecurityverywow";
	
	/**
	 * Create a EC2Handler object that will connect to the Amazon EC2 API, from uS WEST 2.
	 */
	public EC2Handler() {
		this.ec2 =  AmazonEC2ClientBuilder.standard()
				.withRegion(Regions.US_WEST_2) // using west region first
				.build();
	}	
	
	/**
	 * display the hosts (EC2 instances) present
	 */
	public void displayHosts() {
		DescribeHostsResult result = this.ec2.describeHosts();
		List<Host> hosts = result.getHosts();
		for(Host h : hosts) {
			System.out.println(h.getHostId());
		}
	}
	
	/**
	 * Get an instance by its ID
	 * @param id the string ID of the desired instance
	 * @return the com.amazonaws.services.ec2.model.Instance
	 */
	Instance GetInstanceByID(String id)
	{
//		System.out.println("GetInstanceByID: "+id);
		ArrayList<Instance> finalRes = new ArrayList<Instance>();
		DescribeInstancesRequest dir = new DescribeInstancesRequest();
		List<String> ids = new ArrayList<>();
		ids.add(id);
		dir.setInstanceIds(ids);
		
		DescribeInstancesResult result = this.ec2.describeInstances(dir);
		String token = null;
		Instance instance = null;
		do {
			List<Reservation> resList = result.getReservations();
			for(Reservation res : resList) {
				List<Instance> instanceList = res.getInstances();
				for(Instance inst : instanceList) 
				{
//					System.out.println("Found an instance");
					instance = inst;
				}
			}
		token = result.getNextToken();
		} while (token != null);
		return instance;
	}
	
	/**
	 * Get the list of all instances available
	 * @return an Array list of com.amazonaws.services.ec2.model.Instance
	 */
	public ArrayList<Instance> getInstances() 
	{	
		ArrayList<Instance> finalRes = new ArrayList<Instance>();	
		DescribeInstancesResult result = this.ec2.describeInstances();
		String token = null;
		do {
			List<Reservation> resList = result.getReservations();
			for(Reservation res : resList) {
				List<Instance> instanceList = res.getInstances();
				for(Instance inst : instanceList) {
					finalRes.add(inst);
				}
			}
		token = result.getNextToken();
		} while (token != null);

		/// Inform listeners on the update.
		for (int i = 0; i < instanceListeners.size(); ++i)
		{
			InstanceListener il = instanceListeners.get(i);
			il.OnInstanceListUpdated(finalRes);
		}		
		return finalRes;
	}
	
	/**
	 * Display the list of all instances
	 */
	public void displayInstances() {
		ArrayList<Instance> instances = this.getInstances();
		for (Instance inst : instances) {
			System.out.println(inst.getInstanceId()+" - "+inst.getInstanceType()+ " - "+ inst.getState() +" @ "+inst.getPublicIpAddress());
		}
	}
	
	/**
	 * Get a list of all the regions from Amazon
	 * @return a list of com.amazonaws.services.ec2.model.Region
	 */
	public List<Region> getRegions() {
		DescribeRegionsResult res = this.ec2.describeRegions();
		return res.getRegions();
	}
	
	/**
	 * Display the endpoints of each region
	 */
	public void displayEndpoints() {
		List<Region> regions = this.getRegions();
		for (Region region : regions) {
			System.out.println(region.getRegionName()+" - "+region.getEndpoint());
		}
	}
	
	/**
	 * Create a default instance (Ubuntu free tier, micro, key: "ddd", default security group)
	 * @return the cerated instance
	 */
	public Instance createInstance() 
	{
		List<Instance> instancesCreated = this.createInstances("ami-5ec1673e",1,"t2.micro","ddd", defaultSecurityGroupName);
		return instancesCreated.size() > 0? instancesCreated.get(0) : null;
	}
	
	/**
	 * Create a specific number of the same instances
	 * @param imageId the id for the image to use
	 * @param nb the number of instances to create
	 * @param instanceType the type (size) of the instances
	 * @param keyName the name of the SSH key to use
	 * @param secName the security group name
	 * @return the list of instances created
	 */
	public List<Instance> createInstances(String imageId, int nb, String instanceType, String keyName, String secName) {
		RunInstancesRequest req = new RunInstancesRequest(imageId,nb,nb);
		req.setInstanceType(instanceType);
		req.setKeyName(keyName);

		String secId = this.createSecurityGroup(secName);
		System.out.println("security group: "+secName);
		
		ArrayList<String> securityGroupIds = new ArrayList<String>();
		securityGroupIds.add(secId);
		req.setSecurityGroupIds(securityGroupIds);
		RunInstancesResult res = this.ec2.runInstances(req);
		return res.getReservation().getInstances();
	}
	
	/**
	 * Create an empty security group with the specified name
	 * @param name the name of the security group
	 * @return the security group id if it works, the name if it is alreay taken or an error string if it is impossible
	 */
	public String createSecurityGroup(String name) 
	{
		CreateSecurityGroupRequest req = new CreateSecurityGroupRequest();
		req.setGroupName(name);
		req.setDescription("Very secure, from the code");
		// Try to create security group
		try {
			CreateSecurityGroupResult res = this.ec2.createSecurityGroup(req);
			return res.getGroupId();
		} catch (AmazonEC2Exception e)
		{
			System.out.println("Unable to create security group "+name+": "+e.toString());
			if (e.toString().contains("already exists"))
			{
				System.out.println("- Defaulting to using existing security group.");
				return name;
			}
//			deleteSecurityGroup(name);
		}
		return "Unable to create security group";
	}
	
	/**
	 * Get an instance by its ID (redundant)
	 * @pram str the ID of the insatnce
	 */
	public Instance GetInstance(String str)
	{
		return GetInstanceByID(str);
		/*
		List<Instance> inst = this.getInstances();
		for (int i = 0; i < inst.size(); ++i)
		{
			Instance in = inst.get(i);
			in.getInstanceId().equals(str);
			return in;
		}
		return null;*/
	}
	
	/**
	 * Get the instance status (running, shutting down, stopped, terminating, terminating, etc.)
	 * @param inst the instance
	 * @return the status of the requested instance, null otherwise
	 */
	public String getInstanceStatusStr(Instance inst) 
	{
		return State.GetState(inst).text;
	}
	
	/**
	 * Get the instance status (running, shutting down, stopped, terminating, terminating, etc.)
	 * @param instanceId the id of the requested instance
	 * @return the status of the requested instance, null otherwise
	 */
	public String getInstanceStatusStr(String instanceId) 
	{
		Instance inst = this.GetInstance(instanceId);
		if (inst != null){
			return State.GetState(inst).text;
		}
		return null;		
	}
	
	/**
	 * Stop a list of instances
	 * @param instanceIds the IDs of the instances that need to be stopped
	 */
	public void stopInstances(List<String> instanceIds) 
	{
		this.ec2.stopInstances(new StopInstancesRequest(instanceIds));	
		// Fetch new state of this one?
		InformListeners(instanceIds);
	}
	
	/**
	 * MVC model - inform listeners
	 * @pram instanceIds the IDs of the instances
	 */
	public void InformListeners(List<String> instanceIds)
	{
		for (int i = 0; i < instanceIds.size(); ++i)
		{
			String id = instanceIds.get(i);
			for (int j = 0; j < instanceListeners.size(); ++j)
			{
				InstanceListener il = instanceListeners.get(j);
				il.OnInstanceStateUpdated(id);
			}
		}
	}
	
	/**
	 * Stop an instance
	 * @param the instance that needs to be stopped
	 */
	public void StopInstance(Instance inst) {
		this.StopInstance(inst.getInstanceId());
	}
	
	/**
	 * Stop an instance
	 * @param the id of the instance that needs to be stopped
	 */
	public void StopInstance(String instanceId) {
		ArrayList<String> ids = new ArrayList<String>();
		ids.add(instanceId);
		this.stopInstances(ids);
	}
	
	/**
	 * Terminate a list of instances
	 * @param instanceIds the IDs of the instances to terminate
	 */
	public void terminateInstances(List<String> instanceIds) {
		this.ec2.terminateInstances(new TerminateInstancesRequest(instanceIds));
		InformListeners(instanceIds);
	}
	
	/**
	 * Terminate an instance
	 * @param the instance that needs to be terminated
	 */
	public void TerminateInstance(Instance inst) {
		TerminateInstance(inst.getInstanceId());
	}
	
	/**
	 * Terminate an instance
	 * @param the id of the instance that needs to be terminated
	 */
	public void TerminateInstance(String instanceId) {
		ArrayList<String> ids = new ArrayList<String>();
		ids.add(instanceId);
		this.terminateInstances(ids);	
	}

	/**
	 * Delete a specific security group
	 * @param id the id of the security group
	 * @return true if the security group was deleted, false otherwise
	 */
	public boolean deleteSecurityGroup(String id) {
		try 
		{
			DeleteSecurityGroupResult result = this.ec2.deleteSecurityGroup(new DeleteSecurityGroupRequest(id));
			System.out.println("Delete result: "+result.toString());
		} catch (AmazonEC2Exception e)
		{
			System.out.println("Unable to delete security group "+id+": "+e.toString());
			return false;
		}
		return true;
	}
	
	/**
	 * Get the state of an instance
	 * @param inst the Instance
	 * @return the state
	 */
	State GetState(Instance inst)
	{
		String stateStr = getInstanceStatusStr(inst);
		State s = State.GetState(stateStr);
		if (s == State.UnknownState)
			System.out.println("Unknown state: "+stateStr);
		return s;
	}
	
	/**
	 * Sleep until a specified state has been achieved by an instance
	 * @param inst the Instance
	 * @param desiredState the state the instance need to achieved
	 */
	public void SleepUntilInstance(Instance inst, State desiredState) 
	{
		while (GetState(inst) != desiredState)
		{
			try {
				Thread.sleep(500);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Print the state of an instance
	 * @param inst the Instance
	 */
	public void PrintState(Instance inst) 
	{
		System.out.println("Instance "+inst.getInstanceId()+" state: "+getInstanceStatusStr(inst));
	}
	
	/**
	 * Listeners List created here
	 */
	List<InstanceListener> instanceListeners = new ArrayList<>();
	
	/**
	 * MVC Model - add a listener
	 */
	public void addInstanceListener(InstanceListener instanceListener) 
	{
		instanceListeners.add(instanceListener);
	}
	
	/**
	 * @TODO refactor name
	 * Get the image ID of an instance
	 * @param selectedInstanceId the id of the instance
	 * @return the image ID
	 */
	public String GetInstanceName(String selectedInstanceId) 
	{
		Instance inst = GetInstanceByID(selectedInstanceId);
		if (inst == null)
		{
			return "Unable to fetch instance";
		}
		return inst.getImageId();		
	}
	
	/**
	 * Get the security group of an instance
	 * @param selectedInstanceId the id of the requested instance
	 * @return the name of the security group
	 */
	public String GetInstanceSecurityGroup(String selectedInstanceId) {
		Instance inst = GetInstanceByID(selectedInstanceId);
		if (inst == null)
		{
			return "Unable to fetch instance";
		}
		List<GroupIdentifier> security = inst.getSecurityGroups();
		String s = "";
		for (int i = 0; i < security.size(); ++i)
		{
			s += security.get(i).getGroupName();
		}
		return s;
	}
	
	/**
	 * Start an instance
	 * @prams electedInstanceId the id of the instance
	 */
	public void StartInstance(String selectedInstanceId) 
	{
		List<String> instanceIDs = new ArrayList<>();
		instanceIDs.add(selectedInstanceId);
		this.ec2.startInstances(new StartInstancesRequest(instanceIDs));	
		// Fetch new state of this one?
		InformListeners(instanceIDs);
	}
}
