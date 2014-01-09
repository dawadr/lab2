package net.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;



public class VerifiedObjectChannelFactory implements IObjectChannelFactory {

	private Key key;
	private boolean repeat;
	private int maxRepeat;
	private ITamperedMessageOutput messageOutput;
	
	/**
	 * Initialisiert die Factory
	 * @param key Shared Key von Proxy/Fileserver
	 */
	public VerifiedObjectChannelFactory(Key key, boolean repeat, int maxRepeat, ITamperedMessageOutput messageOutput) {
		this.key = key;
		this.repeat = repeat;
		this.maxRepeat = maxRepeat;
		this.messageOutput = messageOutput;
	}


	@Override
	public IObjectChannel create(OutputStream out, InputStream in) throws IOException {
		// System.out.println("Secure TCP Channel erstellen");
		IObjectChannel c = new VerifiedObjectChannel(out, in, key, repeat, maxRepeat, messageOutput);     
		return c;
	}


}