package tests;

import java.util.Iterator;
import java.util.Set;

import generators.CAFGenerator;
import model.ArgumentFramework;
import model.ControlAF;
import parser.AFParser;
import solvers.CSP_AF_Solver;
import solvers.StableSet;
import util.Util;

/**
 * This class provides methods to test the framework for:
 * Argument Frameworks
 * CAF generator
 * @author Fabrice
 *
 */
public class test_AF {
	protected ArgumentFramework af;
	
	public test_AF(ArgumentFramework af) {
		this.af = af;
	}
	
	public test_AF() {
		this.af = null;
	}
	
	public void load_af_from_file(String file) {
		AFParser af_parser = new AFParser(file);		
		ArgumentFramework af = af_parser.parse();
		System.out.println(af.toString());
		this.af = af;
	}
	

	public void print_stable_extensions() {
		if(af == null) {
			throw new UnsupportedOperationException("Please load an Argument Framework first");
		}
		CSP_AF_Solver af_solver = new CSP_AF_Solver(af);
		Set<StableSet> stables = af_solver.getStableSets();
		printStableSets(stables);
	}
	
	private static void printStableSets(Set<StableSet> solutions) {
		int i = 1;
		Iterator<StableSet> iter = solutions.iterator();
		while(iter.hasNext()) {
			System.out.println("--------- printing solution " + i + "-----------");
			System.out.println(iter.next().toString());
			i++;
		}
	}
	
	/**
	 * ALL IN % INTEGER VALUES
	 * @param pArgF proportion of fixed argument
	 * @param pArgU proportion of uncertain argument
	 * @param pAttF proportion of fixed attacks
	 * @param pAttU proportion of uncertain attacks
	 * @param file output file
	 */
	public void generateCAFToFile(int pArgF, int pArgU, int pAttF, int pAttU, String file) {
		CAFGenerator gen = new CAFGenerator(this.af, pArgF, pArgU, pAttF, pAttU);
		ControlAF caf = gen.generate();
		try {
			Util.saveToFile(caf.toString(), file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
