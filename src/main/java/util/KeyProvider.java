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

	private String userKeysPath;

	public KeyProvider(String userKeysPath) {
		this.userKeysPath = userKeysPath;
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}


	public PublicKey getPublicUserKey(String username) throws IOException {
		String pathToPublicKey = userKeysPath + "/" + username + ".pub.pem";
		PEMReader in = new PEMReader(new FileReader(pathToPublicKey)); 
		PublicKey publicKey = (PublicKey) in.readObject();
		in.close();
		return publicKey;
	}

	public PrivateKey getPrivateUserKey(String name, final String password) throws IOException {
		String pathToPrivateKey = userKeysPath + "/" + name + ".pem";
		PEMReader in = new PEMReader(new FileReader(pathToPrivateKey), new PasswordFinder() {
			@Override
			public char[] getPassword() {
				return password.toCharArray();
			}
		});
		KeyPair keyPair = (KeyPair) in.readObject(); 
		PrivateKey privateKey = keyPair.getPrivate();
		in.close();
		return privateKey;
	}
	
	public PublicKey getPublicKey(String location) throws IOException {
		String pathToPublicKey = location;
		PEMReader in = new PEMReader(new FileReader(pathToPublicKey)); 
		PublicKey publicKey = (PublicKey) in.readObject();
		in.close();
		return publicKey;
	}
	
	public PrivateKey getPrivateKey(String location, final String password) throws IOException {
		String pathToPrivateKey = location;
		PEMReader in = new PEMReader(new FileReader(pathToPrivateKey), new PasswordFinder() {
			@Override
			public char[] getPassword() {
				return password.toCharArray();
			}
		});
		KeyPair keyPair = (KeyPair) in.readObject(); 
		PrivateKey privateKey = keyPair.getPrivate();
		in.close();
		return privateKey;
	}
	
}
