package main;

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
		
		/*
		test_CAF caftest = new test_CAF();
		caftest.load_CAF_from_file(path+file_name);
		caftest.solve_with_hardest_completion();
		*/
		
		/**
		 * Test PCAFs
		 */
		
		
		test_PCAF pcaftest = new test_PCAF();
		
		pcaftest.load_PCAF_from_file(path+file_name);
		//pcaftest.printMaxCompletionProba();
		//pcaftest.printCompletionsOverProbability(0.1);
		//pcaftest.printMostProbabelCompletionsCSP();
		//pcaftest.printMostProbableCompletion();
		//pcaftest.printRandomCompletionProba(5);
		pcaftest.printRandomCompletions(6);
	}

	public static void printHelp() {
		System.err.println("Usage:");
		System.err.println("java -jar ControllabilitySolver.jar path file_name");
	}
	
	
}
