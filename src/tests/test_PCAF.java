package tests;

import java.util.Set;
import java.util.TreeSet;

import generators.ControllabilityEncoder;
import generators.MostProbableRootCompletionGenerator;
import generators.RandomProbaRootCompletionGenerator;
import generators.RandomRootCompletionGenerator;

import model.ArgumentFramework;
import model.PControlAF;
import parser.PCAFParser;
import solvers.CSP_PCAF_Proba_Solver;
import solvers.Completion_Proba_Calculator;
import solvers.Most_Probable_Controlling_Entities_Solver;
import solvers.StableControlConfiguration;
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
		RandomRootCompletionGenerator rrg = new RandomRootCompletionGenerator(PCAF);

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
		RandomRootCompletionGenerator rrg = new RandomRootCompletionGenerator(PCAF);
		System.out.println("----------printRandomCompletionProba-----------------");
		
		for(int i=0; i<10; i++) {
			ArgumentFramework af = rrg.getRandomRootCompletion();
			System.out.println("RANDOM AF GENERATED:");
			System.out.println(af.toString());
			System.out.println("for random root completion " + i + " proba is: " + cpc.getProbability(af));
		}
		
		System.out.println("--------------------------------");
	}
	
	public void printMostProbableCompletion() {
		MostProbableRootCompletionGenerator mprcg = new MostProbableRootCompletionGenerator(this.PCAF);
		ArgumentFramework af = mprcg.getMostProbableRootCompletion();
		System.out.println("-----------most probable completion ---------------------");
		System.out.println("most probable completion:");
		System.out.println(af.toString());
		System.out.println("proba is: " + mprcg.getProbability());
		System.out.println("--------------------------------");
	}
	
	public void printMostProbabelCompletionsCSP() {
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
		RandomProbaRootCompletionGenerator gen = new RandomProbaRootCompletionGenerator(this.PCAF);
		for(int i =0; i<nb; i++) {
			ArgumentFramework af = gen.getRandomRootCompletion();
			System.out.println("-------- random completion number " + i + "--------");
			System.out.println(af.toString());
		}
	}

	public void printMostProbableControllingEntities(int nbSimu) {
		Most_Probable_Controlling_Entities_Solver solver = new Most_Probable_Controlling_Entities_Solver(this.PCAF);
		Set<StableControlConfiguration> result = solver.getCredulousControlConfigurations(nbSimu);
		System.out.println("---------------------- CREDULOUS SOLUTIONS----------------");
		for(StableControlConfiguration scc : result) {
			System.out.println(scc.toString());
			System.out.println("controlling power = " + solver.getControllingPower()*100 + "%");
		}
		
		result = solver.getCredulousControlConfigurations(nbSimu);
		System.out.println("---------------------- SKEPTICAL SOLUTIONS----------------");
		for(StableControlConfiguration scc : result) {
			System.out.println(scc.toString());
			System.out.println("controlling power = " + solver.getControllingPower()*100 + "%");
		}
	}
	/**
	 * just a test function to check the distribution generated
	 * by RandomProbaRootCompletionGenerator
	 * it must be uniform between 0 and 1 even for t3 and t4
	 * @param nb
	 */
	public void testRandomThresholds(int nb) {
		RandomProbaRootCompletionGenerator generator = new RandomProbaRootCompletionGenerator(this.PCAF);
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
	
}
