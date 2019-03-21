package main;

import parser.*;
import model.*;
import solvers.*;
import generators.*;
import util.*;
import generators.StrongQBFEncoder;
import logic.qbf.QBFFormula;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


public class Main {
	
	public static void main(String[] args) {
		//long startTime = System.currentTimeMillis();
		if (args.length == 0) {
			printHelp();
			System.exit(1);
		}
		
		Timer timer = new Timer();
		
		/**
		 * TEST ARGUMENT FRAMEWORK FROM FILE AND CAF GENERATOR
		 */
		/* 
		System.out.println(args[1]);
		AFParser af_parser = new AFParser(args[1]);
		
		ArgumentFramework af = af_parser.parse();
		System.out.println(af.toString());
		CSP_AF_Solver af_solver = new CSP_AF_Solver(af);
		Set<StableSet> stables = af_solver.getStableSets();
		printStableSets(stables);
		CAFGenerator gen = new CAFGenerator(af, 33, 33,33,33);
		ControlAF caf = gen.generate();
		System.out.println(caf.toString());
		try {
			Util.saveToFile(caf.toString(), "C:\\Users\\Fabrice\\eclipse-workspace\\PCAF\\examples\\testCAFGEN.caf");
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		
		
		/**
		 * TEST QCIR, QDIMACS generators versus Hardest Completion method
		 */
		/*
		System.out.println(args[0]);
		CAFParser parser = new CAFParser(args[0]);
		ControlAF caf = parser.parse();
		
		StrongQBFEncoder encoder = new StrongQBFEncoder(caf);
	
		System.out.println(caf.toString());
		
		QBFFormula qcir_cred = encoder.encode(ControllabilityEncoder.CREDULOUS);
		QDIMACSConverter converter = new QDIMACSConverter(qcir_cred);
		System.out.println(converter.toQDimacs());
//		QBFFormula qcir_ske = encoder.encode(ControllabilityEncoder.SKEPTICAL);
		
		try {
			Util.saveToFile(qcir_cred.toString(), "C:\\Users\\Fabrice\\eclipse-workspace\\PCAF\\examples\\basic.qcir");
			Util.saveToFile(converter.toQDimacs(), "C:\\Users\\Fabrice\\eclipse-workspace\\PCAF\\examples\\basic.qdimacs");
			//Util.saveToFile(qcir_ske.toString(), "C:\\Users\\Fabrice\\eclipse-workspace\\PCAF\\examples\\passkeSKE.qcir");
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println(caf.toString());
		
		CSP_CAF_Solver solver = new CSP_CAF_Solver(caf);
		timer.start();
		Set<StableControlConfiguration> credulous = solver.getCredulousControlConfigurations();
		Set<StableControlConfiguration> skeptical = solver.getSkepticalControlConfigurations();
		System.out.println("Duration = " + timer.stop() + "ms");
		
		System.out.println("---------------------- CREDULOUS SOLUTIONS----------------");
		printSolutions(credulous);
		System.out.println("---------------------- SKEPTICAL SOLUTIONS----------------");
		printSolutions(skeptical);
		*/
		
		/**
		 * TEST PCAF Load et export
		 */
		System.out.println(args[0]);
		PCAFParser parser = new PCAFParser(args[0]);
		PControlAF pcaf = parser.parse();
		System.out.println(pcaf.toString());
		
		
	}

	public static void printHelp() {
		System.err.println("Usage:");
		System.err.println("java -jar ControllabilitySolver.jar file");
	}
	
	public static void printSolutions(Set<StableControlConfiguration> solutions) {
		int i = 1;
		Iterator<StableControlConfiguration> iter = solutions.iterator();
		while(iter.hasNext()) {
			System.out.println("--------- printing solution " + i + "-----------");
			System.out.println(iter.next().toString());
			i++;
		}
	}
	
	public static void printStableSets(Set<StableSet> solutions) {
		int i = 1;
		Iterator<StableSet> iter = solutions.iterator();
		while(iter.hasNext()) {
			System.out.println("--------- printing solution " + i + "-----------");
			System.out.println(iter.next().toString());
			i++;
		}
	}
}
