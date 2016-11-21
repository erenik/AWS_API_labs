package s3; 

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.s3.model.Region;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;

public class AmazonS3Java {

	static String RegionName(Region r)
	{
		String name = r.name().toLowerCase().replace('_', '-');
		return name;
	}
	public static void main(String[] args) 
	{
		S3Handler s3 = new S3Handler();
//		s3.printBuckets();	
		try {
			// we want to create 3 buckets in 3 different regions
			List<String> bucketNames = new ArrayList<String>();
			int toTest = Region.values().length;
			for (int i = 0; i < toTest; ++i)
			{
				bucketNames.add("emil-"+RegionName(Region.values()[i])+"-bucket");
			}
			/*
			bucketNames.add("emil-eu-frankfurt-bucket");
			bucketNames.add("emil-us-west-2-bucket");
			bucketNames.add("emil-ap-sydney-bucket");
	*/
			System.out.println("Buckets before creation: ");
			printBuckets(s3, "emil");
			
			for (int i = 0; i < bucketNames.size(); ++i)
			{				
				String name = bucketNames.get(i);
				if (s3.bucketExists(name))
				{
					System.out.println("Bucket already exists, skipping.");
					continue;
				}
				Region region = null;
				for (int j = 0; j < Region.values().length; ++j)
				{
					Region r = Region.values()[j];
					if (name.contains(RegionName(r)))
						region = r;
				}
				if (region == null)
					continue;
				try {
					System.out.println("Creating bucket in region "+region.name());
					s3.createBucket(name, region);
				}catch(Exception e)
				{
					System.out.println("Failed to create bucket: "+e.toString());
				}
			}
			System.out.println("Buckets after creation: ");
			printBuckets(s3, "emil");
			for (int i=0;i< bucketNames.size();++i) 
			{
				final int NUM_ITERATION_TESTS = 3;
				String bucketName = bucketNames.get(i);
				int averageDLTime = 0, averageULTime = 0;
				float testSize = 0.1f;
				s3.SetTestSizeMB(testSize);
				System.out.println("\nTests for bucket "+bucketName+" file size "+testSize+"MB");
				for (int tests = 0; tests < NUM_ITERATION_TESTS; ++tests)
				{
					String fileName = s3.RandomFileName();
					int ulTimeMs = s3.uploadRandomdata(bucketName, fileName);
					averageULTime += ulTimeMs;
					System.out.print("upload time "+(ulTimeMs)+" ms, ");	
					int dlTimeMs = s3.getDLTimeMs(bucketName, fileName);			
					averageDLTime += dlTimeMs;
					System.out.print(" dl time "+dlTimeMs+" ms, ");
					s3.deleteFile(bucketName, fileName);
				}
				averageDLTime /= NUM_ITERATION_TESTS;
				averageULTime /= NUM_ITERATION_TESTS;
				System.out.println("\nAverage dlTime: "+averageDLTime+"ms uploadTime: "+averageULTime+"ms");
			}
			// we delete a bucket

			boolean deleteAll = false;
			if (deleteAll)
			{
				System.out.println("Deleting buckets:");
				for (int i = 0; i < bucketNames.size(); ++i)
				{
					System.out.println("- "+bucketNames.get(i));
					s3.deleteBucket(bucketNames.get(i));
				}
				System.out.println("Buckets after deletion: ");
				printBuckets(s3, "emil");
			}
			//s3.deleteBucket("valentinbucket1");
			//s3.printBuckets();
		} catch(AmazonServiceException e) {
			System.out.println(e.getMessage());
		}
				
		// we use our s3 handler
		//S3Handler s3 = new S3Handler();
		//s3.printBuckets(Region.getRegion(Regions.US_WEST_2));
	}
	
	public void printBuckets(S3Handler s3) {
		System.out.println("Retrieving all the buckets");
		// we retrieve all the buckets from this region
		List<Bucket> buckets = s3.getBuckets();
		// we want the name of each bucket
		for (Bucket bucket : buckets) {
			System.out.println(bucket.getName());
		}
	}
	public static void printBuckets(S3Handler s3, String nameContains) 
	{
		// we retrieve all the buckets from this region
		List<Bucket> buckets = s3.getBuckets();
		// we want the name of each bucket
		boolean printedAnything = false;
		for (Bucket bucket : buckets) {
			String name = bucket.getName();
			if (name.contains(nameContains) == false)
				continue;
			printedAnything = true;
			System.out.println("- "+bucket.getName());
		}
		if (!printedAnything)
			System.out.println("- No buckets found with given filter.");
	}
}
