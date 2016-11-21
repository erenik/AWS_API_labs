package ec2;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;

public class CloudWatchHandler {
	
	private AmazonCloudWatch cloudWatch;
	
	public CloudWatchHandler() {
		this.cloudWatch =  AmazonCloudWatchClientBuilder.standard()
				.withRegion(Regions.US_WEST_2) // using west region first
				.build();
	}
	
	public void putAlarm()
	{
		
	}

}
