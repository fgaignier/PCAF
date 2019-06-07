package util;

import java.util.Set;

import model.CArgument;
import model.StableControlConfiguration;
import model.StableExtension;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;

public class Util {

	public static double CONFIDENCE_INT = 1.96;
	public static double STANDARD_ERROR = 0.01;
	public static int MINIMUM_SIMULATION = 100;
	
	public static Set<StableControlConfiguration> intersect(Set<StableControlConfiguration> set1, Set<StableControlConfiguration> set2) {
		Set<StableControlConfiguration> result = new HashSet<StableControlConfiguration>();
		for(StableControlConfiguration scc1 : set1) {
			for(StableControlConfiguration scc2 : set2) {
				if(scc2.equals(scc1)) {
					result.add(scc1);
					break;
				}
			}
		}
		return result;
	}

	public static StableControlConfiguration find(Set<StableControlConfiguration> list, StableControlConfiguration scc) {
		for(StableControlConfiguration elem : list) {
			if(scc.equals(elem)) {
				return elem;
			}
		}
		return null;
	}
	
	public static void saveToFile(String content, String file) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(file);
		out.println(content);
		out.close();
	}
	
	 /**
	  * Agresti-Coull interval method to avoid problems when p=0 or p=1
	  * regular method is N=p*(1-p)*alpha2/epsilon2
	  * @param success number of success
	  * @param nbSimu number of trials
	  * @param error error level
	  * @return
	  */
	 public static double getNewSimulationNumber(double success, double nbSimu, double error) {
		 double epsilon2 = Math.pow(error,2);
		 double alpha2 = Math.pow(Util.CONFIDENCE_INT,2);
		 double p = (success + alpha2/2)/(nbSimu + alpha2);
		 double result = p*(1-p)*alpha2/epsilon2;
		 return result;
	 }
	
}
