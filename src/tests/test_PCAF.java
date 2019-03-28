package tests;

import java.util.Set;

import generators.MostProbableRootCompletionGenerator;
import generators.RandomProbaRootCompletionGenerator;
import generators.RandomRootCompletionGenerator;
import model.ArgumentFramework;
import model.PControlAF;
import parser.PCAFParser;
import solvers.CSP_PCAF_Proba_Solver;
import solvers.Completion_Proba_Calculator;

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

}
