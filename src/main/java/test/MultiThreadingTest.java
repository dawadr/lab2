package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
		
		createDownloadFile();
		
		this.testFileName = "test.txt";

		
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
        System.out.println(clients + " clients started");
		
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
    
    private void createDownloadFile() {
    	Config config = new Config("client");
    	String downloadDir = config.getString("download.dir");
    	
    	//generate random Content
    	byte[] randomData = new byte[(1024 * this.fileSizeKB)];
        new Random().nextBytes(randomData);
        
        System.out.println(downloadDir);
        
        File file = new File(downloadDir + testFileName);
        file.delete();
        
        try {
        	FileOutputStream out = new FileOutputStream(file);
            out.write(randomData);
            out.close();
	    } catch (Exception e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	    }
    }

}
