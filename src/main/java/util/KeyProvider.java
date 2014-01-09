package util;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

import org.bouncycastle.openssl.PEMWriter;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.openssl.PEMReader; 
import org.bouncycastle.util.encoders.Hex;

public class KeyProvider {

	private String keysPath;

	public KeyProvider(String keysPath) {
		this.keysPath = keysPath;
		addProvider();
	}


	public PublicKey getPublicKey(String name) throws IOException {
		String pathToPublicKey = keysPath + "/" + name + ".pub.pem";
		PEMReader in = new PEMReader(new FileReader(pathToPublicKey)); 
		PublicKey publicKey = (PublicKey) in.readObject();
		in.close();
		return publicKey;
	}

	public PrivateKey getPrivateKey(String name, final String password) throws IOException {
		String pathToPrivateKey = keysPath + "/" + name + ".pem";
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

	public void savePublicKey(PublicKey key, String name) throws IOException {
		String pathToPublicKey = keysPath + "/" + name + ".pub.pem";
		PEMWriter out = new PEMWriter(new FileWriter(pathToPublicKey));
		out.writeObject(key);
		out.close();
	}	



	/**
	 * Statische Methoden
	 */

	public static PublicKey getPublicKeyFrom(String location) throws IOException {
		addProvider();
		String pathToPublicKey = location;
		PEMReader in = new PEMReader(new FileReader(pathToPublicKey)); 
		PublicKey publicKey = (PublicKey) in.readObject();
		in.close();
		return publicKey;
	}

	public static PrivateKey getPrivateKeyFrom(String location, final String password) throws IOException {
		addProvider();
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

	public static Key getSharedSecretKeyFrom(String location) throws IOException {
		addProvider();
		byte[] keyBytes = new byte[1024];
		String pathToSecretKey = location;
		FileInputStream fis = new FileInputStream(pathToSecretKey);
		fis.read(keyBytes);
		fis.close();
		byte[] input = Hex.decode(keyBytes);

		Key key = new SecretKeySpec(input, "HmacSHA256");
		return key;
	}

	private static boolean providerAdded = false;
	private static void addProvider() {
		if (!providerAdded) {
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			providerAdded = true;
		}
	}
}
