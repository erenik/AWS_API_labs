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
	
	public EC2Handler() {
		this.ec2 =  AmazonEC2ClientBuilder.standard()
				.withRegion(Regions.US_WEST_2) // using west region first
				.build();
	}	
	
	public void displayHosts() {
		DescribeHostsResult result = this.ec2.describeHosts();
		List<Host> hosts = result.getHosts();
		for(Host h : hosts) {
			System.out.println(h.getHostId());
		}
	}
	
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
	
	public void displayInstances() {
		ArrayList<Instance> instances = this.getInstances();
		for (Instance inst : instances) {
			System.out.println(inst.getInstanceId()+" - "+inst.getInstanceType()+ " - "+ inst.getState() +" @ "+inst.getPublicIpAddress());
		}
	}
	
	public List<Region> getRegions() {
		DescribeRegionsResult res = this.ec2.describeRegions();
		return res.getRegions();
	}
	
	public void displayEndpoints() {
		List<Region> regions = this.getRegions();
		for (Region region : regions) {
			System.out.println(region.getRegionName()+" - "+region.getEndpoint());
		}
	}
	
	/// Creates an instance using default security group.
	public Instance createInstance() 
	{
		List<Instance> instancesCreated = this.createInstances("ami-5ec1673e",1,"t2.micro","ddd", defaultSecurityGroupName);
		return instancesCreated.size() > 0? instancesCreated.get(0) : null;
	}
	
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
	
	// Default get by id?
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
	public String getInstanceStatusStr(Instance inst) 
	{
		return State.GetState(inst).text;
	}
	public String getInstanceStatusStr(String instanceId) 
	{
		Instance inst = this.GetInstance(instanceId);
		if (inst != null){
			return State.GetState(inst).text;
		}
		return null;		
	}
	
	public void stopInstances(List<String> instanceIds) 
	{
		this.ec2.stopInstances(new StopInstancesRequest(instanceIds));	
		// Fetch new state of this one?
		InformListeners(instanceIds);
	}

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

	public void StopInstance(Instance inst) {
		this.StopInstance(inst.getInstanceId());
	}
	public void StopInstance(String instanceId) {
		ArrayList<String> ids = new ArrayList<String>();
		ids.add(instanceId);
		this.stopInstances(ids);
	}
	
	public void terminateInstances(List<String> instanceIds) {
		this.ec2.terminateInstances(new TerminateInstancesRequest(instanceIds));
		InformListeners(instanceIds);
	}
	
	public void TerminateInstance(Instance inst) {
		TerminateInstance(inst.getInstanceId());
	}
	public void TerminateInstance(String instanceId) {
		ArrayList<String> ids = new ArrayList<String>();
		ids.add(instanceId);
		this.terminateInstances(ids);	
	}

	/// Returns true if it succeeded.
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

	State GetState(Instance inst)
	{
		String stateStr = getInstanceStatusStr(inst);
		State s = State.GetState(stateStr);
		if (s == State.UnknownState)
			System.out.println("Unknown state: "+stateStr);
		return s;
	}
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

	public void PrintState(Instance inst) 
	{
		System.out.println("Instance "+inst.getInstanceId()+" state: "+getInstanceStatusStr(inst));
	}

	List<InstanceListener> instanceListeners = new ArrayList<>();
	public void addInstanceListener(InstanceListener instanceListener) 
	{
		instanceListeners.add(instanceListener);
	}

	public String GetInstanceName(String selectedInstanceId) 
	{
		Instance inst = GetInstanceByID(selectedInstanceId);
		if (inst == null)
		{
			return "Unable to fetch instance";
		}
		return inst.getImageId();		
	}

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

	public void StartInstance(String selectedInstanceId) 
	{
		List<String> instanceIDs = new ArrayList<>();
		instanceIDs.add(selectedInstanceId);
		this.ec2.startInstances(new StartInstancesRequest(instanceIDs));	
		// Fetch new state of this one?
		InformListeners(instanceIDs);
	}
}
