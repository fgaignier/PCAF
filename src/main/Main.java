package main;

import generators.ControllabilityEncoder;
import model.ControlAF;
import model.PControlAF;
//import tests.test_AF;
import tests.test_CAF;
import tests.test_PCAF;


public class Main {

	public static double ERROR = 0.05;
	
	public static void main(String[] args) {
		if (args.length < 5) {
			System.out.println("usage: path, file name, type (CAF=0,PCAF=1), acceptance(SKE=0,CRE=1), solver (CSP=0, SAT=1)");
			System.exit(1);
		}

		// parsing arguments
		String path = args[0];
		String file_name = args[1];
		int type = Integer.parseInt(args[2]);
		int acceptance = Integer.parseInt(args[3]);
		int solver_type = Integer.parseInt(args[4]);
		
		//creates timer
		util.Timer timer = new util.Timer();

		test_PCAF pcaftest;

		if(type==0) {
			test_CAF caftest = new test_CAF();
			caftest.load_CAF_from_file(path+file_name);
			ControlAF CAF = caftest.getCAF();
			PControlAF PCAF = new PControlAF(CAF);
			pcaftest = new test_PCAF(PCAF);
			
		} else {
			pcaftest = new test_PCAF();
			pcaftest.load_PCAF_from_file(path+file_name);
		}
		
		if(acceptance == 0) {
			timer.start();
			pcaftest.printMostProbableControllingEntities(Main.ERROR, ControllabilityEncoder.SKEPTICAL, solver_type);
			System.out.println("total time: " + timer.stop() + " milliseconds");
		} else {
			timer.start();
			pcaftest.printMostProbableControllingEntities(Main.ERROR, ControllabilityEncoder.CREDULOUS, solver_type);
			System.out.println("total time: " + timer.stop() + " milliseconds");
		}

	}

}
