package main;

import parser.CAFParser;
import model.*;
import solvers.*;
import generators.*;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


public class Main {
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		if (args.length == 0) {
			printHelp();
			System.exit(1);
		}
		System.out.println(args[0]);
		CAFParser parser = new CAFParser(args[0]);
		ControlAF caf = parser.parse();
		Set<CArgument> toProtect = caf.getArgumentsToProtect();
		CArgument tp = toProtect.iterator().next();
		
		HardestRootCompletionGenerator hrcg = new HardestRootCompletionGenerator(caf);
		WeightedArgumentFramework waf = hrcg.getHardestRootCompletionWRT(tp);
		System.out.println("printout of the hardest af to control wrt " + tp.getName());
		System.out.println(waf.toString());
		
		CSP_ControlConfiguration_Solver csp = new CSP_ControlConfiguration_Solver(caf, waf);
		
		Set<StableControlConfiguration> credulous = new HashSet<StableControlConfiguration>();
		Set<StableControlConfiguration> skeptical = new HashSet<StableControlConfiguration>();
		
		System.out.println("---------------------- CREDULOUS SOLUTIONS----------------");
		credulous = csp.getCredulousControlConfigurations();
		printSolutions(credulous);
		System.out.println("---------------------- SKEPTICAL SOLUTIONS----------------");
		skeptical = csp.getSkepticalControlConfigurations();
		printSolutions(skeptical);
		
		System.out.println("Duration = " + (System.currentTimeMillis() - startTime) + "ms");
		
		/*
		RandomRootCompletionGenerator compGen = new RandomRootCompletionGenerator(caf);
		WeightedArgumentFramework waf = null; 
		Map<Argument, Double> result = null; 
		for(int i = 0; i<10; i++) {
			waf = new WeightedArgumentFramework(compGen.getRandomRootCompletion());
			result = waf.h_categorizer();
			Iterator<Argument> iter = result.keySet().iterator();
			
			System.out.println("------ af number " + i + "------------");
			System.out.println(waf.toString());
			while(iter.hasNext()) {
				Argument arg = iter.next();
				Double strength = result.get(arg);
				System.out.println("argument: " + arg.getName() + " has value: " + strength.toString());
			}

		}
				
	
		System.out.println(caf.toString());
		
		System.out.println("----------------------");
	
		ArgumentFramework maxRoot = compGen.getRandomMaxRootCompletion();
		System.out.println(maxRoot.toString());
		
		for(int i = 0; i<5; i++) {
			ArgumentFramework af = compGen.getRandomRootCompletion();
			if(af.isIncludedIn(maxRoot)) {
				System.out.println("af is included in the max root");
			} else {
				System.out.println("af is not included in the max root");
			}
		}

		
		System.out.println("----------------------");
		 
		CSP_ControlConfiguration_Solver csp = new CSP_ControlConfiguration_Solver(caf, maxRoot);

		Set<StableControlConfiguration> credulous = new HashSet<StableControlConfiguration>();
		Set<StableControlConfiguration> skeptical = new HashSet<StableControlConfiguration>();
		
		System.out.println("---------------------- CREDULOUS SOLUTIONS----------------");
		credulous = csp.getCredulousControlConfigurations();
		printSolutions(credulous);
		System.out.println("---------------------- SKEPTICAL SOLUTIONS----------------");
		skeptical = csp.getSkepticalControlConfigurations();
		printSolutions(skeptical);
		
		System.out.println("Duration = " + (System.currentTimeMillis() - startTime) + "ms");
		*/
		
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
}
