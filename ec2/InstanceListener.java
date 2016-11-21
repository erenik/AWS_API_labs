package ec2;

import java.util.List;

import com.amazonaws.services.ec2.model.Instance;

public interface InstanceListener {	
	abstract void OnInstanceListUpdated(List<Instance> instances);
	abstract void OnInstanceStateUpdated(String instanceId);
	
}
