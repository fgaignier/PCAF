package tests;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import generators.RandomPCAFRootCompletionGenerator;
import generators.ControllabilityEncoder;
import generators.RandomCAFRootCompletionGenerator;

import model.ArgumentFramework;
import model.CArgument;
import model.PControlAF;
import model.StableControlConfiguration;
import model.SupportingPowerRecorder;
import parser.PCAFParser;
import solvers.CSP_PCAF_Proba_Solver;
import solvers.Completion_Proba_Calculator;
import solvers.Most_Probable_Controlling_Entities_Solver;
import solvers.Prefered_Controlling_Entities_Solver;
import util.Util;

public class test_PCAF {

	private PControlAF PCAF;
	private Completion_Proba_Calculator cpc;
	
	public test_PCAF(PControlAF PCAF) {
		this.PCAF = PCAF;
		this.cpc = new Completion_Proba_Calculator(this.PCAF);
	}
	
	public test_PCAF() {
		this.PCAF = null;
		this.cpc = null;
	}
	
	public PControlAF getPCAF() {
		return this.PCAF;
	}
	
	/**
	 * load a PCAF from a file
	 * @param file
	 */
	public void load_PCAF_from_file(String file) {
		PCAFParser parser = new PCAFParser(file);
		this.PCAF = parser.parse();
		this.cpc = new Completion_Proba_Calculator(PCAF);
	}
	
	/**
	 * Calculates proba of max root completion
	 */
	public void printMaxCompletionProba() {
		RandomCAFRootCompletionGenerator rrg = new RandomCAFRootCompletionGenerator(PCAF);

		ArgumentFramework af = rrg.getMaxRootCompletion();
		System.out.println("----------printMaxCompletionProba-----------------");
		System.out.println("max root completion");
		System.out.println(af.toString());
		System.out.println("for max root completion proba is: " + cpc.getProbability(af));
		System.out.println("--------------------------------");
	}
	
	/**
	 * generates "nbSimulations" random root completions and prints the associated probability
	 * @param nbSimulations
	 */
	public void printRandomCompletionProba(int nbSimulations) {
		RandomCAFRootCompletionGenerator rrg = new RandomCAFRootCompletionGenerator(PCAF);
		System.out.println("----------printRandomCompletionProba-----------------");
		
		for(int i=0; i<10; i++) {
			ArgumentFramework af = rrg.getRandomRootCompletion();
			System.out.println("RANDOM AF GENERATED:");
			System.out.println(af.toString());
			System.out.println("for random root completion " + i + " proba is: " + cpc.getProbability(af));
		}
		
		System.out.println("--------------------------------");
	}
		
	public void printMostProbableCompletionsCSP() {
		CSP_PCAF_Proba_Solver pcaf_solver = new CSP_PCAF_Proba_Solver(this.PCAF);
		System.out.println("---------most probable completions CSP-----------------------");
		System.out.println("most probable completions");
		Set<ArgumentFramework> result = pcaf_solver.getMostProbableRootCompletion();
		printCompletions(result);
		System.out.println("--------------------------------");
	}
	
	public void printCompletionsOverProbability(double threshold) {
		CSP_PCAF_Proba_Solver pcaf_solver = new CSP_PCAF_Proba_Solver(this.PCAF);
		Set<ArgumentFramework> result = pcaf_solver.getCompletionsOverLimit(threshold);
		System.out.println("---------completions over threshold " + threshold + " -----------------------");
		printCompletions(result);
		System.out.println("--------------------------------");
	}
	
	private void printCompletions(Set<ArgumentFramework> completions) {
		for(ArgumentFramework c : completions) {
			System.out.println(c.toString());
			System.out.println("with probability = " + cpc.getProbability(c));
		}
	}
	
	public void printRandomCompletions(int nb) {
		RandomPCAFRootCompletionGenerator gen = new RandomPCAFRootCompletionGenerator(this.PCAF);
		for(int i =0; i<nb; i++) {
			ArgumentFramework af = gen.getRandomRootCompletion();
			System.out.println("-------- random completion number " + i + "--------");
			System.out.println(af.toString());
		}
	}

	public void printMostProbableControllingEntities(int N, int type) {
		Most_Probable_Controlling_Entities_Solver solver = new Most_Probable_Controlling_Entities_Solver(this.PCAF);

		Set<StableControlConfiguration> result = null;

		if(type == ControllabilityEncoder.CREDULOUS) {
			result = solver.getCredulousControlConfigurations(N);
			System.out.println("---------------------- CREDULOUS SOLUTIONS----------------");
			System.out.println("controlling power = " + solver.getControllingPower());
			printSolutions(result, solver);
			System.out.println("---------------------- SUPPORTING POWER----------------");
			printSupportingPower(solver.getSupportingPowerRecorders());

		} else {
			result = solver.getSkepticalControlConfigurations(N);
			System.out.println("---------------------- SKEPTICAL SOLUTIONS----------------");
			System.out.println("controlling power = " + solver.getControllingPower());
			printSolutions(result, solver);
			System.out.println("---------------------- SUPPORTING POWER----------------");
			printSupportingPower(solver.getSupportingPowerRecorders());
		}
	}
	
	public void printMostProbableControllingEntities(double error, int type) {
		Most_Probable_Controlling_Entities_Solver solver = new Most_Probable_Controlling_Entities_Solver(this.PCAF);

		Set<StableControlConfiguration> result = null;

		if(type == ControllabilityEncoder.CREDULOUS) {
			result = solver.getCredulousControlConfigurations(error);
			System.out.println("---------------------- CREDULOUS SOLUTIONS----------------");
			System.out.println("controlling power = " + solver.getControllingPower());
			printSolutions(result, solver);
			System.out.println("---------------------- SUPPORTING POWER----------------");
			printSupportingPower(solver.getSupportingPowerRecorders());
		} else {
			result = solver.getSkepticalControlConfigurations(error);
			System.out.println("---------------------- SKEPTICAL SOLUTIONS----------------");
			System.out.println("controlling power = " + solver.getControllingPower());
			printSolutions(result, solver);
			System.out.println("---------------------- SUPPORTING POWER----------------");
			printSupportingPower(solver.getSupportingPowerRecorders());
		}
	}
	
	public void printPreferedCE(int nbSimu, List<Set<CArgument>> preference, int type) {

		System.out.println("prefered controlling entities");
		Prefered_Controlling_Entities_Solver solver = null;
		Set<StableControlConfiguration> result = null;

		solver = new Prefered_Controlling_Entities_Solver(this.PCAF, preference);
		if(type == ControllabilityEncoder.CREDULOUS) {
			result = solver.getPreferedCredulousCE(nbSimu);
			System.out.println("---------------------- CREDULOUS SOLUTIONS----------------");
			for(StableControlConfiguration scc : result) {
				System.out.println(scc.toString());
			}
		} else {		
			System.out.println("---------------------- SKEPTICAL SOLUTIONS----------------");
			solver = new Prefered_Controlling_Entities_Solver(this.PCAF, preference);
			result = solver.getPreferedSkepticalCE(nbSimu);
			for(StableControlConfiguration scc : result) {
				System.out.println(scc.toString());
			}
		}
	}
	
	
	/**
	 * just a test function to check the distribution generated
	 * by RandomProbaRootCompletionGenerator
	 * it must be uniform between 0 and 1 even for t3 and t4
	 * @param nb
	 */
	public void testRandomThresholds(int nb) {
		RandomPCAFRootCompletionGenerator generator = new RandomPCAFRootCompletionGenerator(this.PCAF);
		TreeSet<Double> t1 = new TreeSet<Double>();
		TreeSet<Double> t2 = new TreeSet<Double>();
		TreeSet<Double> t3 = new TreeSet<Double>();
		TreeSet<Double> t4 = new TreeSet<Double>();
		for(int i = 0; i<nb;i++) {
			double[] t = generator.getRandomThresholds();
			t1.add(new Double(t[0]));
			t2.add(new Double(t[1]));
			t3.add(new Double(t[2]));
			t4.add(new Double(t[3]));
		}
		StringBuilder result = new StringBuilder();
		
		for(Double d1 : t1) {
			result.append(d1.doubleValue());
			result.append(";");
		}
		result.append("\n");
		for(Double d2 : t2) {
			result.append(d2.doubleValue());
			result.append(";");
		}
		result.append("\n");
		for(Double d3 : t3) {
			result.append(d3.doubleValue());
			result.append(";");
		}
		result.append("\n");
		for(Double d4 : t4) {
			result.append(d4.doubleValue());
			result.append(";");
		}
		result.append("\n");
		System.out.println(result.toString());
		try {
			Util.saveToFile(result.toString(), "C:\\Users\\Fabrice\\eclipse-workspace\\PCAF\\examples\\distribution.csv");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void printSolutions(Set<StableControlConfiguration> solutions, Most_Probable_Controlling_Entities_Solver solver) {
		int i = 1;
		if(solutions == null) {
			return;
		}
		Iterator<StableControlConfiguration> iter = solutions.iterator();
		while(iter.hasNext()) {
			System.out.println("--------- printing solution " + i + "-----------");
			System.out.println(iter.next().toString());
			System.out.println("controlling power = " + solver.getControllingPower()*100 + "%");
			System.out.println("confidence interval 95%: [" + solver.getLowInterval() + " , " + solver.getHighInterval() + "]");
			i++;
		}
	}
	
	private static void printSupportingPower(Map<StableControlConfiguration, SupportingPowerRecorder> recorders) {
		if(recorders == null) {
			return;
		}
		for(StableControlConfiguration scc : recorders.keySet()) {
			System.out.println("for control configuration: ");
			System.out.println(scc.toString());
			System.out.println("supporting power: ");
			System.out.println(recorders.get(scc).toString());
		}
	}
}
