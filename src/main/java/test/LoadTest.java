package test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class LoadTest {

	private int clients;
	private int uploadsPerMin;
	private int downloadsPerMin;
	private int fileSizeKB;
	private double overwriteRatio;
	private int testDuration;
	private String testFileName;
	private String downloadDir;
	private Timer taskTimer;
	private ComponentFactory componentFactory;

	private int successfulDownloads;
	private int successfulUploadsNew;
	private int successfulUploadsOverwrite;

	private List<TestInputStream> inputStreams;
	private IProxyCli proxy;
	private IClientCli subscribeClient;
	private List<IFileServerCli> fileservers;
	private List<IClientCli> downloadClients;

	public static void main(String[] args) {
		try {
			LoadTest t = new LoadTest();
			t.run();

		} catch (Exception e) {
			System.err.println(e);
		}
	}

	
	public LoadTest() {
		// read config
		Config config = new Config("loadtest");
		this.clients = config.getInt("clients");
		this.uploadsPerMin = config.getInt("uploadsPerMin");
		this.downloadsPerMin = config.getInt("downloadsPerMin");
		this.fileSizeKB = config.getInt("fileSizeKB");
		this.overwriteRatio = Double.parseDouble(config.getString("overwriteRatio"));
		// standard duration = 30
		this.testDuration = 30; //sec
		try {
			testDuration = config.getInt("duration");
		} catch (Exception e) {
		}
	}


	public void run() throws Exception {
		Config config = new Config("client");
		this.downloadDir = config.getString("download.dir");

		this.testFileName = "test.txt";
		createFile(this.testFileName);

		componentFactory = new ComponentFactory();
		this.inputStreams = new ArrayList<TestInputStream>();

		System.out.println("----------------------------------------");
		System.out.println("STARTING COMPONENTS");
		System.out.println("----------------------------------------");
		Thread.sleep(1000);

		// start proxy
		TestInputStream proxyInputStream = new TestInputStream();
		this.inputStreams.add(proxyInputStream);
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
			IClientCli client = componentFactory.startClient(new Config("client"), new Shell("client" + i, new TestOutputStream(System.out), inputStream));
			downloadClients.add(client);
		}     
		Thread.sleep(Util.WAIT_FOR_COMPONENT_STARTUP);
		System.out.println(clients + " clients started");

		// log in clients and increase credits
		for(IClientCli client : downloadClients) {
			System.out.println("logging in client");
			client.login("alice", "12345");
			client.buy(999999999);
		}


		System.out.println("----------------------------------------");
		System.out.println("STARTING LOAD TEST");
		System.out.println("----------------------------------------");
		Thread.sleep(2000);

		this.taskTimer = new Timer();
		for(IClientCli client : downloadClients) {
			if(downloadsPerMin > 0)
				taskTimer.schedule(new DownloadTask(client), 0, (long) 60000 / downloadsPerMin);
			if(uploadsPerMin > 0)
				taskTimer.schedule(new UploadTask(client), 0, (long) 60000 / uploadsPerMin);
		}  

		Thread.sleep(testDuration * 1000);
		exit();
	}



	private void createFile(String filename) {
		File f = new File(this.downloadDir + "/" + filename);
		f.delete();

		try {
			FileOutputStream out = new FileOutputStream(f);
			out.write(generateRandomContent());
			out.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private byte[] generateRandomContent() {
		byte[] randomData = new byte[(1024 * this.fileSizeKB)];
		new Random().nextBytes(randomData);
		return randomData;
	}

	private void printStat() {
		System.out.println("Successful Downloads: " + this.successfulDownloads);
		System.out.println("Successful New Uploads: " + this.successfulUploadsNew);
		System.out.println("Successful Overwrite Uploads: " + this.successfulUploadsOverwrite);
	}
	
	private void exit() {
		taskTimer.cancel();
		try {	
			for(TestInputStream tis : inputStreams) {
				tis.close();
			}

			subscribeClient.exit();

			for(IClientCli client : downloadClients) {
				client.exit();
			}

			for(IFileServerCli server : fileservers) {
				server.exit();
			}

			proxy.exit();
		} catch (IOException e) {
			System.err.println(e);
		}

		componentFactory.shutdown();
		
		System.out.println("----------------------------------------");
		System.out.println("TEST FINISHED");
		System.out.println("----------------------------------------");
		printStat();
	}

	private class DownloadTask extends TimerTask {

		IClientCli client;

		public DownloadTask(IClientCli client) {
			this.client = client;
		}

		@Override
		public void run() {
			try {

				String actual = client.download("testFile.txt").toString();
				String expected = "!data";
				assertTrue(String.format("Response must start with '%s' but was '%s'", expected, actual), actual.startsWith(expected));

				if(actual.startsWith(expected)) LoadTest.this.successfulDownloads++;

				printStat();

			} catch (Exception e) {
				System.err.println(e);
			}
		}

	}


	private class UploadTask extends TimerTask {

		IClientCli client;

		public UploadTask(IClientCli client) {
			this.client = client;
		}

		@Override
		public void run() {
			try {

				String actual = null;

				Boolean overwrite = false;

				if (new Random().nextDouble() > LoadTest.this.overwriteRatio) {
					//Create new File
					int random = new Random().nextInt(8);
					String randomFilename = "file" +random+ ".txt";
					LoadTest.this.createFile(randomFilename);
					actual = client.upload(randomFilename).toString();
				} else {
					//Overwrite File
					overwrite = true;
					actual = client.upload(LoadTest.this.testFileName).toString();
				}

				String expected = "success";
				assertTrue(String.format("Response must contain '%s' but was '%s'", expected, actual), actual.contains(expected));

				if(actual.contains(expected)) {
					if(overwrite) {
						successfulUploadsOverwrite++;
					} else {
						successfulUploadsNew++;
					}
				}

				printStat();

			} catch (Exception e) {
				System.err.println(e);
			}
		}
	}
}