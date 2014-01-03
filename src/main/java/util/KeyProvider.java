package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.openssl.PEMReader; 

public class KeyProvider {

	private String directory;

	public KeyProvider(String directory) {
		this.directory = directory;
	}


	public PublicKey getPublicKey(String name) throws IOException {
		String pathToPublicKey = directory + "/" + name + ".pem";
		PEMReader in = new PEMReader(new FileReader(pathToPublicKey)); 
		PublicKey publicKey = (PublicKey) in.readObject();
		in.close();
		return publicKey;
	}

	public PrivateKey getPrivateKey(String name, final String password) throws IOException {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		String pathToPrivateKey = directory + "/" + name + ".pem";
		PEMReader in = new PEMReader(new FileReader(pathToPrivateKey), new PasswordFinder() {
			@Override
			public char[] getPassword() {
				return "12345".toCharArray();
			}
		});
		KeyPair keyPair = (KeyPair) in.readObject(); 
		PrivateKey privateKey = keyPair.getPrivate();
		in.close();
		return privateKey;
	}

}
