package sk.fiit.dp.refactor.helper;

import java.math.BigInteger;
import java.security.SecureRandom;

public class IdGenerator {
	public static String generateId() {
		SecureRandom generator = new SecureRandom();
		
		return new BigInteger(64, generator).toString();
	}
}
