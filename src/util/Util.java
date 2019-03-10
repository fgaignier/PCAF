package util;

public class Util {

	public static boolean randomDirectionGenerator() {
		double random = Math.random();
		if(random <0.5) {
			return true;
		} else {
			return false;
		}
	}
}
