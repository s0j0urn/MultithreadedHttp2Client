package multithreadedHttp2Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MultiThreadedHttp2Client {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		long startExecutionTime =  System.currentTimeMillis();

		String fileName = args[0];
		List<String> urlsListOneTimeRead = new ArrayList<String>();
		int urlsListSize=0;

		try {
			urlsListOneTimeRead = Files.readAllLines(Paths.get(fileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		urlsListSize = urlsListOneTimeRead.size();
		
		int noOfConcurrentStreams = Integer.parseInt(args[2]);
		int noOfClients = Integer.parseInt(args[1]);
		
		ExecutorService execService = Executors.newFixedThreadPool(noOfClients);
		
		for(int i=0; i<noOfClients; i++){
			execService.execute(new ClientLogicHttp2(i, urlsListSize, urlsListOneTimeRead, noOfConcurrentStreams));
			//execService.execute(new ClientLogicHttp2(i, urlsListSize, urlsListOneTimeRead));
		}
		
		execService.shutdown();
		
		 try {
	            execService.awaitTermination(1, TimeUnit.DAYS);
	        } catch (InterruptedException ignored) {
	        	ignored.printStackTrace();
	        }
	        System.out.println("All tasks completed in " + (System.currentTimeMillis() - startExecutionTime) + "ms");
	        

	}

}
