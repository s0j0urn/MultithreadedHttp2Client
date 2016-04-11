package multithreadedHttp2Client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.api.Stream.Listener;
import org.eclipse.jetty.http2.api.server.ServerSessionListener;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.GoAwayFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.http2.frames.PingFrame;
import org.eclipse.jetty.http2.frames.PushPromiseFrame;
import org.eclipse.jetty.http2.frames.ResetFrame;
import org.eclipse.jetty.http2.frames.SettingsFrame;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.FuturePromise;
import org.eclipse.jetty.util.Promise;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class ClientLogicHttp2 implements Runnable  {
	
	private int id;
	private int urlsListSize;
	private int numConcurrentStreams;
	private List<String> urlsList;
	public ClientLogicHttp2(int id, int urlsListSize, List<String> urlsList, int numConcurrentStreams)
	{	
		this.id = id;
		this.urlsListSize = urlsListSize;
		this.urlsList = urlsList;
		this.numConcurrentStreams = numConcurrentStreams;

	}
	
	/*public ClientLogicHttp2(int id, int urlsListSize, List<String> urlsList)
	{	
		this.id = id;
		this.urlsListSize = urlsListSize;
		this.urlsList = urlsList;
		//this.numConcurrentStreams = numConcurrentStreams;

	}*/

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		HTTP2Client lowLevelClient = new HTTP2Client();
		SslContextFactory sslContextFactory = new SslContextFactory(true);
		//SslContextFactory sslContextFactory = getSSLContextFactory ();
		lowLevelClient.addBean(sslContextFactory);
		try {
			lowLevelClient.start();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//System.out.println("Client Started");

		//String host =  "webtide.com";
		String host =  urlsList.get(0).substring(8, urlsList.get(0).indexOf(':', 8));
		//int port =  443;
		int port =  Integer.parseInt(urlsList.get(0).substring(urlsList.get(0).indexOf(':', 8)+1, urlsList.get(0).indexOf('/', 8)));
		
		final AtomicInteger success_status_count = new AtomicInteger(0);
		//System.out.println("Host is " + host);
		//System.out.println("Port is" + port);
		// Connect to host
		
		FuturePromise<Session> sessionPromise = new FuturePromise<>();
		lowLevelClient.connect(sslContextFactory, new InetSocketAddress(host, port), new ServerSessionListener() {
			
			@Override
			public void onSettings(Session arg0, SettingsFrame arg1) {
				// TODO Auto-generated method stub
				
				
						
					//	message[0] = arg1.getSettings().get(3);
					//	System.out.println(message[0]);
			}
			
			@Override
			public void onReset(Session arg0, ResetFrame arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Map<Integer, Integer> onPreface(Session arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void onPing(Session arg0, PingFrame arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Listener onNewStream(Stream arg0, HeadersFrame arg1) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean onIdleTimeout(Session arg0) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void onFailure(Session arg0, Throwable arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onClose(Session arg0, GoAwayFrame arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAccept(Session arg0) {
				// TODO Auto-generated method stub
				
			}
		}, sessionPromise);
		//Obtain Client Session
		Session session = null;
		try {
			session = sessionPromise.get();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//System.out.println(session.getStreams().size());
		
		HttpFields requestFields = new HttpFields();
		requestFields.put(HttpHeader.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/48.0.2564.82 Chrome/48.0.2564.82 Safari/537.36");
		
		// Hardcoded to 100 to respect h2o settings frame. Can be easily generalized as well by reading onSetting frame in a class overriding Session.listener
		//Thread.sleep(1000);
		//System.out.println("max = " + message[0]);
		Semaphore no_of_concurrent_streams = new Semaphore(numConcurrentStreams);
		//final CountDownLatch latch = new CountDownLatch(it should be  = urlsListSize +  no_of_pushed_resources);
		//final CountDownLatch latch = new CountDownLatch(220);
		final Phaser phaser = new Phaser();
		long start =  System.currentTimeMillis();
		phaser.register();
		for(int i=0;i<urlsListSize;i++)
		{

			try {
				no_of_concurrent_streams.acquire();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//requestFields.put(HttpHeader.C_AUTHORITY,host+":"+port);
			//requestFields.put(HttpHeader.C_METHOD, "GET");
			//requestFields.put(HttpHeader.C_SCHEME, "https");
			requestFields.put(HttpHeader.ACCEPT_ENCODING, "gzip, deflate, sdch");
			requestFields.put(HttpHeader.ACCEPT_LANGUAGE, "en-GB,en-US;q=0.8,en;q=0.6");
			requestFields.put(HttpHeader.CACHE_CONTROL, "no-cache");
			requestFields.put(HttpHeader.PRAGMA, "no-cache");
			String extension = urlsList.get(i).substring(urlsList.get(i).indexOf('.')+1);
			if(extension.equalsIgnoreCase("html"))
			{
				requestFields.put(HttpHeader.ACCEPT,"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				requestFields.put(HttpHeader.C_PATH, urlsList.get(i).substring(urlsList.get(i).indexOf('/',8)));
			}
			else if(extension.equalsIgnoreCase("js"))
			{
				
				requestFields.put(HttpHeader.ACCEPT, "*/*");
				
				requestFields.put(HttpHeader.C_PATH, urlsList.get(i).substring(urlsList.get(i).indexOf('/',8)));
				requestFields.put(HttpHeader.REFERER, "https://" + host + ":" + port + urlsList.get(i).substring(urlsList.get(i).indexOf('/',8)));
			}
			else if(extension.equalsIgnoreCase("css"))
			{
				
				requestFields.put(HttpHeader.ACCEPT, "text/css, */*;q=0.1");
				
				requestFields.put(HttpHeader.C_PATH, urlsList.get(i).substring(urlsList.get(i).indexOf('/',8)));
				requestFields.put(HttpHeader.REFERER, "https://" + host + ":" + port + "/my-gallery/index.html");
			}
			else if(extension.equalsIgnoreCase("JSON"))
			{
				
				requestFields.put(HttpHeader.ACCEPT, "application/json");
				
				requestFields.put(HttpHeader.C_PATH, urlsList.get(i).substring(urlsList.get(i).indexOf('/',8)));
				requestFields.put(HttpHeader.REFERER, "https://" + host + ":" + port + "/my-gallery/index.html");
			}
			else if(extension.equalsIgnoreCase("gif")||extension.equalsIgnoreCase("jpg")||extension.equalsIgnoreCase("png"))
			{
				
				requestFields.put(HttpHeader.ACCEPT, "image/webp,image/*,*/*;q=0.8");

				requestFields.put(HttpHeader.C_PATH, urlsList.get(i).substring(urlsList.get(i).indexOf('/',8)));
				requestFields.put(HttpHeader.REFERER, "https://" + host + ":" + port + "/my-gallery/index.html");
			} 
			//Request Object
			//MetaData.Request metaData = new MetaData.Request("GET", new HttpURI("https://" + host + ":" + port + "/"), HttpVersion.HTTP_2, requestFields);
			//MetaData.Request metaData = new MetaData.Request("GET", new HttpURI("https://webtide.com:443/"), HttpVersion.HTTP_2, requestFields);
			//System.out.println(new HttpURI("https://" + host + ":" + port + urlsList.get(i).substring(urlsList.get(i).indexOf('/',8))));
			//String resource = urlsList.get(i);
			
			MetaData.Request metaDataRequest = new MetaData.Request("GET", new HttpURI("https://" + host + ":" + port + urlsList.get(i).substring(urlsList.get(i).indexOf('/',8))), HttpVersion.HTTP_2, requestFields);
			//Create Headers frame
			HeadersFrame headersFrame = new HeadersFrame(metaDataRequest,null,true);

			//Listen to response frames.
			//System.out.println("Going to start streams");
			phaser.register();
			session.newStream(headersFrame, new Promise.Adapter<Stream>(), new Stream.Listener.Adapter()
			{
			
				@Override
				public void onHeaders(Stream stream, HeadersFrame frame)
				{
					//System.out.println("Header frame " + frame);
					//System.out.println("[" + frame.getStreamId() + "] HEADERS " + frame.getMetaData().toString());
					//frame.getMetaData().getFields().forEach(field -> System.out.println("[" + stream.getId() + "]     " + field.getName() + ": " + field.getValue()));
					if (frame.isEndStream()) {
						//System.out.println("on Headers frame end" + frame.getStreamId());
						phaser.arrive();
						//latch.countDown();
						success_status_count.getAndIncrement();
						no_of_concurrent_streams.release();
					}
				}

				@Override
				public void onData(Stream stream, DataFrame frame, Callback callback)
				{
					//System.out.println("Data frame" + frame);
					//byte[] bytes = new byte[frame.getData().remaining()];
					//frame.getData().get(bytes);
					//System.out.println("[" + frame.getStreamId() + "] DATA " + new String(bytes));
					//callback.succeeded();
					//System.out.println(frame.toString());
					callback.succeeded();
					if (frame.isEndStream()) {
						//System.out.println("on data frame end" + frame.getStreamId());
						phaser.arrive();
						//latch.countDown();
						success_status_count.getAndIncrement();
						no_of_concurrent_streams.release();
					}
				}

				@Override
				public Stream.Listener onPush(Stream stream, PushPromiseFrame frame)
				{
					//System.err.println(frame);
					//System.out.println("[" + frame.getStreamId() + "] PUSH_PROMISE " + frame.getMetaData().toString());
					/*try {
						no_of_concurrent_streams.acquire();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
					
					//System.out.println("Push promise frame received with promised stream id " + frame.getPromisedStreamId() + " Current stream id " +  frame.getStreamId());
					phaser.register();
					return this;
				}
			});
		}

		//phaser.arriveAndAwaitAdvance();
		 try {
			phaser.awaitAdvanceInterruptibly(phaser.arrive(), 5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 System.out.println("Client " + id + " completed " + success_status_count.get() + " successful requests in " + (System.currentTimeMillis() - start) + "ms" );
        //latch.await();
		//Thread.sleep(20000);

		try {
			lowLevelClient.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
