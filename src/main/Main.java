package main;

import generators.ControllabilityEncoder;
import model.ControlAF;
import model.PControlAF;
//import tests.test_AF;
import tests.test_CAF;
import tests.test_PCAF;


public class Main {


	public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println("usage: path, file name, type (CAF=0,PCAF=1), acceptance(SKE=0,CRE=1)");
			System.exit(1);
		}

		// parsing arguments
		String path = args[0];
		String file_name = args[1];
		int type = Integer.parseInt(args[2]);
		int acceptance = Integer.parseInt(args[3]);
		
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
			pcaftest.printMostProbableControllingEntities(0.05, ControllabilityEncoder.SKEPTICAL);
			System.out.println("total time: " + timer.stop() + " milliseconds");
		} else {
			timer.start();
			pcaftest.printMostProbableControllingEntities(0.05, ControllabilityEncoder.CREDULOUS);
			System.out.println("total time: " + timer.stop() + " milliseconds");
		}

	}

}
