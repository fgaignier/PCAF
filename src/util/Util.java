package util;

import solvers.StableControlConfiguration;

import java.util.Set;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;

public class Util {

	public static double CONFIDENCE_INT = 1.96;
	public static double STANDARD_ERROR = 0.01;
	public static int MINIMUM_SIMULATION = 100;
	
	public static Set<StableControlConfiguration> intersect(Set<StableControlConfiguration> set1, Set<StableControlConfiguration> set2) {
		Iterator<StableControlConfiguration> iter1 = set1.iterator();
		Iterator<StableControlConfiguration> iter2 = null;
		Set<StableControlConfiguration> result = new HashSet<StableControlConfiguration>();
		
		while(iter1.hasNext()) {
			StableControlConfiguration scc1 = iter1.next();
			iter2 = set2.iterator();
			while(iter2.hasNext()) {
				StableControlConfiguration scc2 = iter2.next();
				if(scc2.equals(scc1)) {
					result.add(scc1);
					break;
				}
			}
		}
		return result;
	}
	
	public static void saveToFile(String content, String file) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(file);
		out.println(content);
		out.close();
	}
}
