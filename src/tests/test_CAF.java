package tests;

import java.util.Iterator;
import java.util.Set;

import generators.QDIMACSConverter;
import generators.StrongQBFEncoder;
import logic.qbf.QBFFormula;
import model.ControlAF;
import parser.CAFParser;
import solvers.CSP_CAF_Solver;
import solvers.StableControlConfiguration;
import util.Util;

public class test_CAF {

	private ControlAF CAF;
	
	public test_CAF(ControlAF CAF) {
		this.CAF = CAF;
	}
	
	public test_CAF() {
		this.CAF = null;
	}
	
	public void load_CAF_from_file(String file) {
		CAFParser parser = new CAFParser(file);
		this.CAF = parser.parse();
	}
	
	public void saveQDIMACSToFile(String file, int type) {
		StrongQBFEncoder encoder = new StrongQBFEncoder(this.CAF);
		
		QBFFormula qbf = encoder.encode(type);
		QDIMACSConverter converter = new QDIMACSConverter(qbf);
		
		try {
			Util.saveToFile(converter.toQDimacs(), file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveQCIRToFile(String file, int type) {
		StrongQBFEncoder encoder = new StrongQBFEncoder(this.CAF);
		
		QBFFormula qbf = encoder.encode(type);
		
		try {
			Util.saveToFile(qbf.toString(), file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void solve_with_hardest_completion() {
		CSP_CAF_Solver solver = new CSP_CAF_Solver(this.CAF);

		Set<StableControlConfiguration> credulous = solver.getCredulousControlConfigurations();
		Set<StableControlConfiguration> skeptical = solver.getSkepticalControlConfigurations();
		
		System.out.println("---------------------- CREDULOUS SOLUTIONS----------------");
		printSolutions(credulous);
		System.out.println("---------------------- SKEPTICAL SOLUTIONS----------------");
		printSolutions(skeptical);
	}
	
	
	
	private static void printSolutions(Set<StableControlConfiguration> solutions) {
		int i = 1;
		Iterator<StableControlConfiguration> iter = solutions.iterator();
		while(iter.hasNext()) {
			System.out.println("--------- printing solution " + i + "-----------");
			System.out.println(iter.next().toString());
			i++;
		}
	}
	
}
