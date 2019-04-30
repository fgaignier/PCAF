package main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.CArgument;
import model.ControlAF;
import model.PControlAF;
import tests.test_CAF;
import tests.test_PCAF;

public class Main {
	
	public static void main(String[] args) {
		//long startTime = System.currentTimeMillis();
		if (args.length < 2) {
			printHelp();
			System.exit(1);
		}
		
		String path = args[0];
		String file_name = args[1];
		
		/**
		 * Test AF
		 */
		
		/**
		 * Test CAF
		 */
		
		
		test_CAF caftest = new test_CAF();
		caftest.load_CAF_from_file(path+file_name);
		ControlAF CAF = caftest.getCAF();
	//	System.out.println(CAF.toString());
		//caftest.solve_with_hardest_completion();
		//caftest.test_solution();
		caftest.solve_with_monte_carlo(1000);
		
		
		/**
		 * Test PCAFs
		 */
		
		
		PControlAF PCAF = new PControlAF(CAF);
		test_PCAF pcaftest = new test_PCAF(PCAF);
		//System.out.println(PCAF.toString());
		pcaftest.printMostProbableControllingEntities(1000);
		
		//pcaftest.load_PCAF_from_file(path+file_name);
		//pcaftest.printMaxCompletionProba();
		//pcaftest.printCompletionsOverProbability(0.1);
		//pcaftest.printMostProbableCompletionsCSP();
		//pcaftest.printMostProbableCompletion();
		//pcaftest.printRandomCompletionProba(5);
		//System.out.println("---------------------------");
		//pcaftest.printMostProbableControllingEntities(0.01);
		//pcaftest.testRandomThresholds(1000);
		

		/*
		Set<CArgument> target = pcaftest.getPCAF().getTarget();
		List<Set<CArgument>> preference = new ArrayList<Set<CArgument>>(); 
		for(CArgument arg : target) {
			if(arg.getName().equals("a")) {
				Set<CArgument> l1 = new HashSet<CArgument>();
				l1.add(arg);
				preference.add(l1);
			} else {
				Set<CArgument> l2 = new HashSet<CArgument>();
				l2.add(arg);
				preference.add(l2);
			}
		}
		pcaftest.printPreferedCE(100, preference);
		*/
		
	}

	public static void printHelp() {
		System.err.println("Usage:");
		System.err.println("java -jar ControllabilitySolver.jar path file_name");
	}
	
	
}
