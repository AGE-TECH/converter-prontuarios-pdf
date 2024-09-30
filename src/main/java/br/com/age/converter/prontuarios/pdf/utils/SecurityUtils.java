package br.com.age.converter.prontuarios.pdf.utils;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class SecurityUtils {

	private static final String SECURE_RENDOM_ALG = "SHA1PRNG";
	private static final String SECRETE_KEY_ALG = "PBKDF2WithHmacSHA512";
	private int iterations;

	private SecurityUtils(int iterations) {
		this.iterations = iterations;
	}

	public static SecurityUtils defaults() {
		return new SecurityUtils(2000);
	}

	public String hash(String text) {
		try {
			char[] chars = text.toCharArray();
			byte[] salt = getSalt();
			PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance(SECRETE_KEY_ALG);
			byte[] hash = skf.generateSecret(spec).getEncoded();
			String hexHash = toHex(hash);
			return hexHash;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	private byte[] getSalt() throws NoSuchAlgorithmException {
		SecureRandom sr = SecureRandom.getInstance(SECURE_RENDOM_ALG);
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt;
	}

	private String toHex(byte[] array) {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0) {
			return String.format("%0" + paddingLength + "d", 0) + hex;
		}
		return hex;
	}
	
}
