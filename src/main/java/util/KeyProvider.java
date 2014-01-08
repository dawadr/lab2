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
	
	public static PublicKey getPublicKey(String location) throws IOException {
		String pathToPublicKey = location;
		PEMReader in = new PEMReader(new FileReader(pathToPublicKey)); 
		PublicKey publicKey = (PublicKey) in.readObject();
		in.close();
		return publicKey;
	}
	
	public void writeKeyTo(PublicKey key, String location) throws IOException {
		PEMWriter out = new PEMWriter(new FileWriter(location));
		out.writeObject(key);
		out.close();
	}	
	
	public static PrivateKey getPrivateKey(String location, final String password) throws IOException {
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
	
	public static Key getSharedSecretKey(String location) throws IOException {
		byte[] keyBytes = new byte[1024];
		String pathToSecretKey = location;
		FileInputStream fis = new FileInputStream(pathToSecretKey);
		fis.read(keyBytes);
		fis.close();
		byte[] input = Hex.decode(keyBytes);
		
		Key key = new SecretKeySpec(input, "HmacSHA256");
		return key;
	}
	
}
