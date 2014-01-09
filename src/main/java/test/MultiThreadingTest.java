package test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import proxy.IProxyCli;
import cli.Shell;
import cli.TestInputStream;
import cli.TestOutputStream;
import client.IClientCli;
import server.IFileServerCli;
import util.ComponentFactory;
import util.Config;
import util.Util;

public class MultiThreadingTest {

	private int clients;
	private int uploadsPerMin;
	private int downloadsPerMin;
	private int fileSizeKB;
	private double overwriteRatio;
	private int testDuration;
	private String testFileName;
	private File downloadFile;
	private String downloadDir;

	private int successfulDownloads;
	private int successfulUploads;

	private List<TestInputStream> inputStreams;
	//TODO CLose

	private IProxyCli proxy;
	//TODO CLose

	private IClientCli subscribeClient;
	//TODO CLose

	private List<IFileServerCli> fileservers;
	//TODO CLose

	private List<IClientCli> downloadClients;
	//TODO CLose

	public static void main(String[] args) {

		try {
			new MultiThreadingTest();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public MultiThreadingTest() throws Exception {

		readConfig();
		this.testDuration = 300;

		Config config = new Config("client");
		this.downloadDir = config.getString("download.dir");

		this.testFileName = "test.txt";
		
		createFile(this.testFileName);


		ComponentFactory componentFactory = new ComponentFactory();
		this.inputStreams = new ArrayList<TestInputStream>();

		// start proxy
		TestInputStream proxyInputStream = new TestInputStream();
		this.inputStreams.add(proxyInputStream);
		//this.inputStreams.add(proxyInputStream);String password = "12345";
		//byte[] bytes = password.getBytes();
		//proxyInputStream.read(bytes);
		this.proxy = componentFactory.startProxy(new Config("proxy"), new Shell("proxy", new TestOutputStream(System.out), proxyInputStream));
		Thread.sleep(Util.WAIT_FOR_COMPONENT_STARTUP);
		System.out.println("proxy started");


		// start fileservers
		this.fileservers = new ArrayList<IFileServerCli>();
		int numberOfFileservers = 4;

		for(int i = 1; i <= numberOfFileservers; i++) {
			TestInputStream inputStream = new TestInputStream();
			this.inputStreams.add(inputStream);
			IFileServerCli server = componentFactory.startFileServer(new Config("fs" + i ), new Shell("fs" + i, new TestOutputStream(System.out), inputStream));
			System.out.println("fileserver "+i+" started");
			this.fileservers.add(server);
		}
		Thread.sleep(Util.WAIT_FOR_COMPONENT_STARTUP);


		//start subscribeClient

		TestInputStream inputStream = new TestInputStream();
		this.inputStreams.add(inputStream);
		this.subscribeClient = componentFactory.startClient(new Config("client"), new Shell("client", new TestOutputStream(System.out), inputStream));
		Thread.sleep(Util.WAIT_FOR_COMPONENT_STARTUP);
		System.out.println("suscribeClient started");
		subscribeClient.login("alice", "12345");                
		subscribeClient.upload(testFileName);
		subscribeClient.subscribe(testFileName, 2);
		subscribeClient.subscribe(testFileName, 3);
		subscribeClient.subscribe(testFileName, 7);
		subscribeClient.subscribe(testFileName, 13);


		// start downloadClients

		downloadClients = new ArrayList<IClientCli>();
		for(int i = 1; i <= clients; i++) {
			inputStream = new TestInputStream();
			this.inputStreams.add(inputStream);
			IClientCli client = componentFactory.startClient(new Config("client"), new Shell("client", new TestOutputStream(System.out), inputStream));
			downloadClients.add(client);
		}     
		Thread.sleep(Util.WAIT_FOR_COMPONENT_STARTUP);
		System.out.println(clients + " clients started");
		
		// log in clients and increase credits
		for(IClientCli client : downloadClients) {
            client.login("alice", "12345");
            client.buy(Long.MAX_VALUE);
	    }
	    
	    Timer taskTimer = new Timer();
	    for(IClientCli client : downloadClients) {
	            if(downloadsPerMin > 0)
	                    taskTimer.schedule(new DownloadTask(client), 0, (long) 60000 / downloadsPerMin);
	            if(uploadsPerMin > 0)
	                    taskTimer.schedule(new UploadTask(client), 0, (long) 60000 / uploadsPerMin);
	    }  

	}

	private void readConfig() {

		Config config = new Config("loadtest");

		this.clients = config.getInt("clients");
		this.uploadsPerMin = config.getInt("uploadsPerMin");
		this.downloadsPerMin = config.getInt("downloadsPerMin");
		this.fileSizeKB = config.getInt("fileSizeKB");
		this.overwriteRatio = Double.parseDouble(config.getString("overwriteRatio"));
		System.out.println("Clients: " + clients);
		System.out.println("UploadsPerMin: " + uploadsPerMin);
		System.out.println("DownloadsPerMin: " + downloadsPerMin);
		System.out.println("FileSizeKB: " + fileSizeKB);
		System.out.println("OverwriteRatio: " + overwriteRatio);
	}

	private void createFile(String filename) {

		File f = new File(this.downloadDir + "/" + filename);
		f.delete();

		try {
			FileOutputStream out = new FileOutputStream(f);
			out.write(generateRandomContent());
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private byte[] generateRandomContent() {

		byte[] randomData = new byte[(1024 * this.fileSizeKB)];
		new Random().nextBytes(randomData);
		return randomData;

	}

	private class DownloadTask extends TimerTask {

		IClientCli client;

		public DownloadTask(IClientCli client) {
			this.client = client;
		}

		public void run() {
			try {

				String actual = client.download("testFile.txt").toString();
				String expected = "!data";
				assertTrue(String.format("Response must start with '%s' but was '%s'", expected, actual), actual.startsWith(expected));

				if(actual.startsWith(expected)) MultiThreadingTest.this.successfulDownloads++;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}


	private class UploadTask extends TimerTask {

		IClientCli client;

		public UploadTask(IClientCli client) {
			this.client = client;
		}

		public void run() {
			try {
			
				String actual = null;

				if (new Random().nextDouble() > MultiThreadingTest.this.overwriteRatio) {
					//Create new File
					int random = new Random().nextInt(8);
					String randomFilename = "file" +random+ ".txt";
					MultiThreadingTest.this.createFile(randomFilename);
					actual = client.upload(randomFilename).toString();
				} else {
					//Override File
					actual = client.upload(MultiThreadingTest.this.testFileName).toString();
				}

				String expected = "success";
				assertTrue(String.format("Response must contain '%s' but was '%s'", expected, actual), actual.contains(expected));

				if(actual.contains(expected)) MultiThreadingTest.this.successfulUploads++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
