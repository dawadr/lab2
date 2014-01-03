package net.channel;


import java.io.IOException;

import message.response.FailedResponse;

import org.bouncycastle.util.encoders.Base64;

import util.Serialization;

public class _SecureServerChannel extends ChannelDecorator {

	private boolean initialized = false;

	public _SecureServerChannel(IChannel decoratedChannel) {
		super(decoratedChannel);
	}

	
	@Override
	public void writeBytes(byte[] data) throws IOException {
		if (!initialized) {
			throw new IOException("Secure channel not yet established.");
		}
		super.writeBytes(data);
	}

	@Override
	public byte[] readBytes() throws IOException {
		while (!initialized) {
			initialize();	
		}
		return super.readBytes();
	}


	private boolean initialize() throws IOException {
		System.out.println("Initializing secure channel");

		Object o1;
		o1 = super.readBytes();
		String request1 = null;
		if (o1 instanceof byte[]) {
			request1 = new String((byte[])o1);
		} else {
			super.writeBytes("Authentication failed".getBytes());
			return false;
		}
		System.out.println("Receiving: " + request1);
		if (request1.equals("!login")) {
			String s1 = "!ok";
			//DataMessage r = new DataMessage(s1.getBytes());
			super.writeBytes(s1.getBytes());
			System.out.println("Sent !ok");
		} else {
			super.writeBytes("Authentication failed".getBytes());
			return false;
		}


		Object o2;
		o2 = super.readBytes();
		String request2 = null;
		if (o2 instanceof byte[]) {
			request2 = new String((byte[])o2);
		} else {
			super.writeBytes("Authentication failed".getBytes());
			return false;
		}
		System.out.println("Receiving: " + request2);
		if (!request2.equals("!finalize")) {
			super.writeBytes("Authentication failed".getBytes());
			return false;
		} 

		System.out.println("Initialized");
		initialized = true;
		return true;
	}

}
