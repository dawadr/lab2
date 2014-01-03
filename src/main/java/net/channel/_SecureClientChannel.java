package net.channel;


import java.io.IOException;

import message.DataMessage;

import org.bouncycastle.util.encoders.Base64;

import util.Serialization;

public class _SecureClientChannel extends ChannelDecorator {

	private boolean initialized = false;

	public _SecureClientChannel(IChannel decoratedChannel) {
		super(decoratedChannel);
	}


	@Override
	public void writeBytes(byte[] data) throws IOException {
		if (!initialized) {
			try {
				if (initialize() == false) {
					throw new IOException("Secure channel authenitaction failed.");
				}
			} catch (ClassNotFoundException e) {
				throw new IOException("Secure channel authenitaction failed.", e);
			}
		}
		super.writeBytes(data);
	}

	@Override
	public byte[] readBytes() throws IOException {
		if (!initialized) {
			throw new IOException("Secure channel not yet established.");
		}
		return super.readBytes();
	}


	private boolean initialize() throws IOException, ClassNotFoundException {
		System.out.println("Initializing secure channel");
		String s1 = "!login";
		//DataMessage r = new DataMessage(s1.getBytes());
		super.writeBytes(s1.getBytes());
		System.out.println("Sent !login");

		Object o = super.readBytes();
		String response = null;
		if (o instanceof byte[]) {
			response = new String((byte[])o);
		} else return false;

		System.out.println("Server returns " + response);

		if (!response.equals("!ok")) return false;

		String s2 = "!finalize";
		//		r = new DataMessage(s2.getBytes());
		super.writeBytes(s2.getBytes());
		System.out.println("Sent !finalize");
		initialized = true;
		return true;
	}
}


