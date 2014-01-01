package net.channel;


import java.io.IOException;

import message.DataMessage;
import message.response.FailedResponse;

import org.bouncycastle.util.encoders.Base64;

import util.Serialization;

public class SecureServerChannel extends ChannelDecorator {

	
	private boolean initialized = false;
	
	public SecureServerChannel(IChannel decoratedChannel) {
		super(decoratedChannel);
	}

	@Override
	public void writeObject(Object o) throws IOException {	
		
		if (!initialized) {
			throw new IOException("Secure channel not yet established.");
		}
		
		super.writeObject(o);
	}

	@Override
	public Object readObject() throws IOException, ClassNotFoundException {	
		
		while (!initialized) {
				initialize();	
		}
		
		
		return super.readObject();
	}
	
	
	
	private boolean initialize() throws IOException {
		System.out.println("Initializing secure channel");
		
		
		Object o1;
		try {
			o1 = super.readObject();
		} catch (ClassNotFoundException e1) {
			super.writeObject(new FailedResponse("Authentication failed."));
			return false;
		}
		String request1 = null;
		if (o1 instanceof DataMessage) {
			request1 = new String(((DataMessage)o1).getData());
		} else {
			super.writeObject(new FailedResponse("Authentication failed."));
			return false;
		}
		System.out.println("Receiving: " + request1);
		if (request1.equals("!login")) {
			String s1 = "!ok";
			DataMessage r = new DataMessage(s1.getBytes());
			super.writeObject(r);
			System.out.println("Sent !ok");
		} else {
			super.writeObject(new FailedResponse("Authentication failed."));
			return false;
		}
		
		
		Object o2;
		try {
			o2 = super.readObject();
		} catch (ClassNotFoundException e) {
			super.writeObject(new FailedResponse("Authentication failed."));
			return false;
		}
		String request2 = null;
		if (o2 instanceof DataMessage) {
			request2 = new String(((DataMessage)o2).getData());
		} else {
			super.writeObject(new FailedResponse("Authentication failed."));
			return false;
		}
		System.out.println("Receiving: " + request2);
		if (!request2.equals("!finalize")) {
			super.writeObject(new FailedResponse("Authentication failed."));
			return false;
		} 
		
		System.out.println("Initialized");
		initialized = true;
		return true;
		
	
		

	}

}
