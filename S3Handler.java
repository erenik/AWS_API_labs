import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.S3Object;


public class S3Handler {
	
	private AmazonS3 s3;
	private float testSizeMB = 0;
	
	public S3Handler() 
	{
		// We create a S3 object to access the amazon storage service
		this.s3 = AmazonS3ClientBuilder.standard()
				.withRegion(Regions.US_WEST_2) // using west region first
				.build();
	}
	
	public List<Bucket> getBuckets() {
		// we retrieve all the buckets from this region
		List<Bucket> buckets = s3.listBuckets();
		return buckets;
	}
	
	public void printBuckets() {
		List<Bucket> buckets = this.getBuckets();
		for (Bucket bucket : buckets) {
			System.out.println(bucket.getName());
		}
	}
	
	public void createBucket(String name, Region region) throws AmazonServiceException {
		this.s3.createBucket(name,region);
	}
	
	public void deleteBucket(String name) throws AmazonServiceException {
		this.s3.deleteBucket(name);
	}
	
	/// Returns milliseconds required.
	public int uploadRandomdata(String bucketName, String fileName) throws AmazonServiceException 
	{
		String key = fileName;
		File file = new File(fileName);
		// creating random file
		FileWriter fw;
		int bytes = (int) (testSizeMB  * Math.pow(1000, 2));
		int megaBytes = bytes / 1000000;
		try {
			fw = new FileWriter(file);
			for (int i=0; i < bytes;++i) 
			{
				fw.write((char) (Math.random()*26+'a'));
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		// we want to measure time when uploading
		long startTime = System.currentTimeMillis();		
		this.s3.putObject(new PutObjectRequest(bucketName,key,file));
		long stopTime = System.currentTimeMillis();
		int totalTime = (int) (stopTime - startTime);
		return totalTime;
	}
	
	public void deleteFile(String bucketName, String fileName) throws AmazonServiceException {
		this.s3.deleteObject(new DeleteObjectRequest(bucketName, fileName));	
	}
	
	public S3Object getFile(String bucketName, String fileName) throws AmazonServiceException 
	{
		// we want to measure time when uploading
		long startTime = System.currentTimeMillis();
		System.out.print("Downloading file file \""+fileName+"\" from bucket: "+bucketName);
		S3Object obj = this.s3.getObject(new GetObjectRequest(bucketName, fileName));
		long contentLength = obj.getObjectMetadata().getContentLength();
		int megaBytes = (int) (contentLength / 1000000);
		long stopTime = System.currentTimeMillis();
		System.out.println("... "+(stopTime-startTime)+" ms for "+megaBytes+" MB");
		return obj;
	}

	public int getDLTimeMs(String bucketName, String fileName) throws AmazonServiceException 
	{
		// we want to measure time when uploading
		long startTime = System.currentTimeMillis();
		S3Object obj = this.s3.getObject(new GetObjectRequest(bucketName, fileName));
		long contentLength = obj.getObjectMetadata().getContentLength();
		int megaBytes = (int) (contentLength / 1000000);
		long stopTime = System.currentTimeMillis();
		return (int) (stopTime - startTime);
	}

	public boolean bucketExists(String name) {
		List<Bucket> buckets = getBuckets();
		for (int i = 0; i < buckets.size(); ++i)
		{
			if (buckets.get(i).getName().equals(name))
				return true;
		}
		return false;
	}

	public String RandomFileName() 
	{
		// TODO Auto-generated method stub
		String fileName = "RandomFile"+Integer.toString((int) (Math.random()*1000))+".txt";
		return fileName;
	}

	public void SetTestSizeMB(float testSize) {
		// TODO Auto-generated method stub
		this.testSizeMB  = testSize;
	}
}
