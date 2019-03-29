package util;

import java.util.Random;

public class RandomGen {

	/**
	 * Returns a pseudo-random number between min and max, inclusive. The difference
	 * between min and max can be at most <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min Minimum value
	 * @param max Maximum value. Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	public static int randInt(int min, int max) {
		Random rand = new Random();
		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}
	
	/**
	 * Returns a random number between 0 and max, inclusive.
	 * @param max
	 * @return
	 */
	public static int getIndex(int max) {
		return randInt(0, max);
	}
	
	public static int getProba() {
		return randInt(1,100);
	}
	
	/**
	 * Returns a random boolean based on a uniform distribution
	 *  
	 */
	public static boolean randomBoolean() {
		double random = Math.random();
		if(random <0.5) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns a random double between min incl  and max excl
	 */
	public static double randomDouble(double min, double max) {
		Random rand = new Random();
		return min + (max - min) * rand.nextDouble();
	}
}
