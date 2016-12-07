package ec2;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
import com.amazonaws.services.cloudwatch.model.Metric;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.Statistic;
import com.amazonaws.services.ec2.model.Instance;

public class CloudWatchHandler {
	
	private AmazonCloudWatch cloudWatch;
	
	/**
	 * Create a CloudWatchHandler object that will connect to the Amazon CloudWatch API, from uS WEST 2.
	 */
	public CloudWatchHandler() {
		this.cloudWatch =  AmazonCloudWatchClientBuilder.standard()
				.withRegion(Regions.US_WEST_2) // using west region first
				.build();
	}
	
	/**
	 * Display all the metrics available with Cloudwatch
	 */
	public void displayMetrics() {
		List<Metric> res = this.getMetrics();
		for(Metric metric : res) {
			System.out.println(metric.getMetricName());
		}
	}
	
	/**
	 * Get all the metrics available with Cloudwatch
	 * @return a list of com.amazonaws.services.cloudwatch.model.Metric
	 */
	public List<Metric> getMetrics() {
		return this.cloudWatch.listMetrics().getMetrics();
	}
	
	/**
	 * Get a specific Metric from its name
	 * @param metricName the name of the metric
	 * @return the requested metric
	 */
	public Metric getMetric(String metricName) {
		ListMetricsRequest req = new ListMetricsRequest();
		req.setMetricName(metricName);
		return (this.cloudWatch.listMetrics(req)).getMetrics().get(0);
	}
	
	/**
	 * @deprecated
	 * Create an alarm in CloudWatch
	 * @param alarmname the name for the alarm
	 * @param alarmDescription the description of the alarm
	 * @param metric the metric to be used for the alarm
	 * @param threshold the threshold to trigger the alarm
	 * @param period the period
	 * @param operator operator to test against threshold (>=, >, <=,<)
	 * @param evalutionPeriods
	 * @param statistic
	 */
	public void addAlarm(String alarmName, String alarmDescription, Metric metric, double threshold, int period, String operator, int evaluationPeriods, String statistic) {
		PutMetricAlarmRequest req = new PutMetricAlarmRequest();
		req.setAlarmName(alarmName);
		req.setAlarmDescription(alarmDescription);
		req.setMetricName(metric.getMetricName());
		req.setThreshold(threshold);
		req.setPeriod(period);
		ComparisonOperator op = ComparisonOperator.GreaterThanOrEqualToThreshold;
		switch (operator) {
		case ">=":
			op = ComparisonOperator.GreaterThanOrEqualToThreshold;
			break;
		case ">":
			op = ComparisonOperator.GreaterThanOrEqualToThreshold;
			break;	
		case "<=":
			op = ComparisonOperator.GreaterThanOrEqualToThreshold;
			break;		
		case "<":
			op = ComparisonOperator.GreaterThanOrEqualToThreshold;
			break;
		}
		req.setComparisonOperator(op);
		req.setEvaluationPeriods(evaluationPeriods);
		req.setNamespace(metric.getNamespace());
		Statistic stat = Statistic.Average;
		switch (statistic) {
		case "avg":
			stat = Statistic.Average;
			break;
		case "sum":
			stat = Statistic.Sum;
			break;	
		case "max":
			stat = Statistic.Maximum;
			break;		
		case "min":
			stat = Statistic.Minimum;
			break;
		}
		req.setStatistic(stat);
		
		
		this.cloudWatch.putMetricAlarm(req);
	}
	
	/* Fetches metrics, available (test 2016-11-22):
		VolumeWriteBytes, VolumeReadBytes
		VolumeReadOps, VolumeWriteOps
		VolumeIdleTime
		VolumeTotalReadTime, VolumeTotalWriteTime
		VolumeQueueLength
		
		BurstBalance
		
		DiskReadOps, DiskWriteOps
		DiskWriteBytes, DiskReadBytes
		NetworkIn
		CPUUtilization

		NetworkPacketsOut, NetworkPacketsIn
		
		StatusCheckFailed_System
		StatusCheckFailed
		StatusCheckFailed_Instance
		VolumeQueueLength
		VolumeTotalReadTime		
	*/
	
	/**
	 * Get the statistics of a specified metric for a specific instance
	 * @param instance the instance
	 * @param durationHour the duration in hours
	 * @param metric the needed metric, from this list:VolumeWriteBytes, VolumeReadBytes
		VolumeReadOps, VolumeWriteOps
		VolumeIdleTime
		VolumeTotalReadTime, VolumeTotalWriteTime
		VolumeQueueLength
		
		BurstBalance
		
		DiskReadOps, DiskWriteOps
		DiskWriteBytes, DiskReadBytes
		NetworkIn
		CPUUtilization

		NetworkPacketsOut, NetworkPacketsIn
		
		StatusCheckFailed_System
		StatusCheckFailed
		StatusCheckFailed_Instance
		VolumeQueueLength
		VolumeTotalReadTime
	 * @param periodInMinutes the interval between each point, in minutes
	 * @return a list of com.amazonaws.services.cloudwatch.model.Datapoint
	 */
	public List<Datapoint> getMetricStatistics(Instance instance, int durationHour, String metric, int periodInMinutes) 
	{	
		long duration = durationHour * 3600 * 1000;
//		System.out.println(duration);
		
		Dimension instanceDimension = new Dimension();
	    instanceDimension.setName("InstanceId");
	    instanceDimension.setValue(instance.getInstanceId());
		
	    GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
	            .withStartTime(new Date(new Date().getTime() - duration))
	            .withNamespace("AWS/EC2")
	            .withPeriod(periodInMinutes * 60) // Must be multiple of 60
	            .withMetricName(metric)
	            .withStatistics("Average")
	            .withDimensions(Arrays.asList(instanceDimension))
	            .withEndTime(new Date());

	    GetMetricStatisticsResult getMetricStatisticsResult = cloudWatch.getMetricStatistics(request);
//	    System.out.print(".. Datapoints received: "+getMetricStatisticsResult.getDatapoints().size()+"\n");
	    return getMetricStatisticsResult.getDatapoints();
	}

}
