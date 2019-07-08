package main;

import generators.ControllabilityEncoder;
import model.ControlAF;
import model.PControlAF;
//import tests.test_AF;
import tests.test_CAF;
import tests.test_PCAF;


public class Main {

	
	public static int QDIMACS = 0;
	public static int QCIR = 1;
	
	public static String ext_qcir = ".qcir";
	public static String ext_qdimacs = ".qdimacs";
	
	public static String changeFileExt(String fileName, int type) {
		String[] splt = fileName.split("\\.");
		StringBuffer result = new StringBuffer();
		result.append(splt[0]);
		if(type == QDIMACS) {
			result.append(ext_qdimacs);
		} else {
			result.append(ext_qcir);
		}
		System.out.println(result.toString());
		return result.toString();
	}
	
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("usage: path, file name");
			System.exit(1);
		}
		
		String path = args[0];
		String file_name = args[1];
		
		/**
		 * Test AF
		 */
		/*
		test_AF aftest = new test_AF();
		aftest.load_af_from_file(path+"test.txt");
		System.out.println("##########################################");
		System.out.println("AF stable sets");
		System.out.println("##########################################");
		aftest.print_stable_extensions();
		*/
		
		/**
		 * Test CAF
		 */
		
		
		test_CAF caftest = new test_CAF();
		caftest.load_CAF_from_file(path+file_name);
		
	/*	
		caftest.saveQCIRToFile(path + changeFileExt(file_name, QCIR), ControllabilityEncoder.SKEPTICAL);
		caftest.saveQDIMACSToFile(path + changeFileExt(file_name, QDIMACS), ControllabilityEncoder.SKEPTICAL);
		*/
		
		ControlAF CAF = caftest.getCAF();
		
		/*
		System.out.println("##########################################");
		System.out.println("fixed number of simulations");
		System.out.println("##########################################");
		caftest.solve_with_monte_carlo(100, ControllabilityEncoder.SKEPTICAL);
		caftest.solve_with_monte_carlo(100, ControllabilityEncoder.CREDULOUS);
*/
		
		System.out.println("##########################################");
		System.out.println("error level");
		System.out.println("##########################################");
		caftest.solve_with_monte_carlo(0.01, ControllabilityEncoder.SKEPTICAL);
		caftest.solve_with_monte_carlo(0.01, ControllabilityEncoder.CREDULOUS);

		
		/*
		System.out.println("##########################################");
		System.out.println("heuristic");
		System.out.println("##########################################");
		caftest.solve_with_heuristic(0.01, ControllabilityEncoder.SKEPTICAL);
		caftest .solve_with_heuristic(0.01, ControllabilityEncoder.CREDULOUS);
		*/
		
		/**
		 * Test PCAFs
		 */
		
		
		PControlAF PCAF = new PControlAF(CAF);
		test_PCAF pcaftest = new test_PCAF(PCAF);
		
//		System.out.println(PCAF.toString());
		/*
		test_PCAF pcaftest = new test_PCAF();
		pcaftest.load_PCAF_from_file(path+file_name);
		*/
		
		
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("##########################################");
		System.out.println("transforming the CAF into PCAF");
		System.out.println("##########################################");
		System.out.println();
		System.out.println();
		System.out.println();
		
		/*
		System.out.println("##########################################");
		System.out.println("fixed number of simulations");
		System.out.println("##########################################");
		pcaftest.printMostProbableControllingEntities(100, ControllabilityEncoder.SKEPTICAL);
		pcaftest.printMostProbableControllingEntities(100, ControllabilityEncoder.CREDULOUS);
		*/
		
		System.out.println("##########################################");
		System.out.println("error level");
		System.out.println("##########################################");
		pcaftest.printMostProbableControllingEntities(0.01, ControllabilityEncoder.SKEPTICAL);
		pcaftest.printMostProbableControllingEntities(0.01, ControllabilityEncoder.CREDULOUS);
		
		
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
	
}
